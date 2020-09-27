package com.github.gobars.rest;

import com.alibaba.fastjson.JSON;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class Rest {
  public static final HttpClient CLIENT = HttpClientBuilder.create().build();
  private final RequestConfig requestConfig =
      RequestConfig.custom()
          // 从连接池获取到连接的超时时间，如果是非连接池的话，该参数暂时没有发现有什么用处
          .setConnectionRequestTimeout(30 * 1000)
          // 指客户端和服务进行数据交互的时间，是指两者之间如果两个数据包之间的时间大于该时间则认为超时，而不是整个交互的整体时间，
          // 比如如果设置1秒超时，如果每隔0.8秒传输一次数据，传输10次，总共8秒，这样是不超时的。
          // 而如果任意两个数据包之间的时间超过了1秒，则超时。
          .setSocketTimeout(30 * 1000)
          // 建立连接的超时时间
          .setConnectTimeout(30 * 1000)
          .build();

  public HttpRequestBase buildRequest(String method, String url) {
    if (method == null) {
      method = "GET";
    }

    switch (method.toUpperCase()) {
      case "POST":
        return new HttpPost(url);
      case "DELETE":
        return new HttpDelete(url);
      case "OPTIONS":
        return new HttpOptions(url);
      case "PUT":
        return new HttpPut(url);
      case "HEAD":
        return new HttpHead(url);
      case "GET":
      default:
        return new HttpGet(url);
    }
  }

  @SneakyThrows
  @SuppressWarnings("unchecked")
  public <T> T exec(Option option) {
    String method = fixMethod(option);
    HttpRequestBase req = buildRequest(method, option.getUrl());
    req.setConfig(requestConfig);
    Result result = new Result();

    jsonBody(option, req, result);
    uploadBody(option, req);

    HttpResponse response = CLIENT.execute(req);

    int code = codeCheck(option, req, response);

    HttpEntity responseEntity = response.getEntity();
    if (option.getDownload() != null) {
      responseEntity.writeTo(option.getDownload());
      return null;
    }

    String body = responseEntity != null ? EntityUtils.toString(responseEntity) : null;
    String uri = req.getURI().toString();
    result.setResponse(response);
    result.setResultBody(body);
    result.setStatusCode(code);

    T t = (T) parseT(option, method, body, response, result);
    OkBiz<T> succ = option.getOkBiz();
    if (succ != null && !succ.isOk(code, body, t)) {
      throw new HttpRestException(uri, code, response, succ + "业务判断不成功");
    }

    return t;
  }

  private String fixMethod(Option option) {
    if (option.getRequestBody() != null || option.getUpload() != null) {
      return "POST";
    }

    return option.getMethod();
  }

  @NotNull
  private Map<String, String> copyHeaders(HttpResponse response) {
    Header[] allHeaders = response.getAllHeaders();
    Map<String, String> headers = new HashMap<>(allHeaders.length);

    for (val header : allHeaders) {
      headers.put(header.getName(), header.getValue());
    }
    return headers;
  }

  private int codeCheck(Option option, HttpRequestBase req, HttpResponse response) {
    int code = response.getStatusLine().getStatusCode();
    OkStatus okStatus = option.getOkStatus();
    if (!okStatus.isOk(code)) {
      throw new HttpRestException(option.getUrl(), code, response);
    }

    log.info("{} {}, code:{}", req.getMethod(), option.getUrl(), code);
    return code;
  }

  private void uploadBody(Option option, HttpRequestBase req) {
    if (option.getUpload() != null) {
      val builder = MultipartEntityBuilder.create();
      builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
      String fn = option.getFileName();
      builder.addBinaryBody(fn, option.getUpload(), ContentType.DEFAULT_BINARY, fn);
      val entityReq = (HttpEntityEnclosingRequest) req;
      entityReq.setEntity(builder.build());
    }
  }

  private void jsonBody(Option option, HttpRequestBase req, Result result) {
    if (option.getRequestBody() != null && req instanceof HttpEntityEnclosingRequest) {
      String payload = JSON.toJSONString(option.getRequestBody());
      result.setPayload(payload);
      val entityReq = (HttpEntityEnclosingRequest) req;
      entityReq.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
    }
  }

  private Object parseT(
      Option option, String method, String body, HttpResponse response, Result result) {
    if ("HEAD".equals(method)) {
      return copyHeaders(response);
    }

    Type type = option.getType();
    if (type != null) {
      return JSON.parseObject(body, type);
    }

    Class<?> clazz = option.getClazz();
    if (clazz == HttpResponse.class) {
      return response;
    }

    if (clazz == Result.class) {
      return result;
    }

    if (clazz != null) {
      return JSON.parseObject(body, clazz);
    }

    return body;
  }

  @EqualsAndHashCode(callSuper = true)
  @Value
  public static class HttpRestException extends RuntimeException {
    String uri;
    int code;
    transient HttpResponse response;

    public HttpRestException(String uri, int code, HttpResponse response) {
      this(uri, code, response, "");
    }

    public HttpRestException(String uri, int code, HttpResponse response, String message) {
      super(message);
      this.uri = uri;
      this.code = code;
      this.response = response;
    }

    @Override
    public String toString() {
      return "url [" + uri + "] failed code:[" + code + "]";
    }
  }
}
