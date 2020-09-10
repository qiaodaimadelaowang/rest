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
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class Rest {
  public static HttpClient client = HttpClientBuilder.create().build();
  private RequestConfig requestConfig =
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
    if (option == null) {
      option = new Option();
    }

    String method = option.getMethod();
    if (option.getRequestBody() != null || option.getUpload() != null) {
      method = "POST";
    }

    HttpRequestBase req = buildRequest(method, option.getUrl());
    req.setConfig(requestConfig);
    Result result = new Result();

    if (option.getRequestBody() != null && req instanceof HttpEntityEnclosingRequest) {
      String payload = JSON.toJSONString(option.getRequestBody());
      result.setPayload(payload);
      HttpEntityEnclosingRequest entityReq = (HttpEntityEnclosingRequest) req;
      entityReq.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
    }

    if (option.getUpload() != null) {
      MultipartEntityBuilder builder = MultipartEntityBuilder.create();
      builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
      String fn = option.getFileName();
      builder.addBinaryBody(fn, option.getUpload(), ContentType.DEFAULT_BINARY, fn);
      HttpEntityEnclosingRequest entityReq = (HttpEntityEnclosingRequest) req;
      entityReq.setEntity(builder.build());
    }

    HttpResponse response = client.execute(req);

    int code = response.getStatusLine().getStatusCode();
    StatusCodeSuccessful codeSucc = option.getStateCodeSuccessful();
    if (!codeSucc.isSuccessful(code)) {
      throw new HttpRestException(option.getUrl(), code, response);
    }

    log.info("{} {}, code:{}", req.getMethod(), option.getUrl(), code);

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

    if ("HEAD".equals(method)) {
      Header[] allHeaders = response.getAllHeaders();
      Map<String, String> headers = new HashMap<>(allHeaders.length);

      for (val header : allHeaders) {
        headers.put(header.getName(), header.getValue());
      }

      return (T) headers;
    }

    T t = parseT(option, body);
    BizSuccessful<T> succ = option.getBizSuccessful();
    if (succ != null && !succ.isSuccessful(code, body, t)) {
      throw new HttpRestException(uri, code, response, succ + "业务判断不成功");
    }

    return t;
  }

  @SuppressWarnings("unchecked")
  private <T> T parseT(Option option, String body) {
    Type type = option.getType();
    if (type != null) {
      return JSON.parseObject(body, type);
    }

    Class clazz = option.getClazz();
    if (clazz != null) {
      return (T) JSON.parseObject(body, clazz);
    }

    return (T) body;
  }

  @EqualsAndHashCode(callSuper = true)
  @Value
  public static class HttpRestException extends RuntimeException {
    String uri;
    int code;
    HttpResponse response;

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
