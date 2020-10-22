package com.github.gobars.rest;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.*;
import org.apache.http.protocol.HttpContext;

/**
 * httpclient请求响应拦截器.
 *
 * <p>参考https://www.tutorialspoint.com/apache_httpclient/apache_httpclient_interceptors.htm
 */
@Slf4j
public class Interceptor {
  public static final String REST_OPTION_KEY = "REST_OPTION_KEY";

  public static class Req implements HttpRequestInterceptor {
    @Override
    public void process(HttpRequest r, HttpContext ctx) {
      RestOption ro = (RestOption) ctx.getAttribute(REST_OPTION_KEY);
      if (!ro.isDump()) {
        return;
      }

      log.info("请求开始 {}", r.getRequestLine());
      for (Header h : r.getAllHeaders()) {
        log.info("请求头部 {}:{}", h.getName(), h.getValue());
      }
    }
  }

  public static class Rsp implements HttpResponseInterceptor {
    @Override
    public void process(HttpResponse r, HttpContext ctx) {
      RestOption ro = (RestOption) ctx.getAttribute(REST_OPTION_KEY);
      if (!ro.isDump()) {
        return;
      }

      log.info("响应收到 {}", r.getStatusLine());
      for (Header h : r.getAllHeaders()) {
        log.info("响应头部 {}:{}", h.getName(), h.getValue());
      }
    }
  }
}
