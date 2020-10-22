package com.github.gobars.rest;

import lombok.Data;
import org.apache.http.HttpResponse;

@Data
public class RestRuntime {
  /** 请求方法 */
  private String method;
  /** 请求地址 */
  private String url;
  /** 请求体。 */
  private String payload;
  /** 响应体。 */
  private String resultBody;
  /** 响应状态码。 */
  private int statusCode;
  /** 原始响应。 */
  private HttpResponse response;

  /** HTTP调用耗时毫秒 */
  private long httpCostMillis;
  /** HTTP调用异常 */
  private Exception exception;
}
