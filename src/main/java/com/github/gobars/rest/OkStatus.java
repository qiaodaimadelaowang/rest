package com.github.gobars.rest;

public interface OkStatus {
  /**
   * 判断http调用状态码是否成功.
   *
   * @param statusCode 返回状态码
   * @return 是否成功
   */
  default boolean isOk(int statusCode) {
    return statusCode >= 200 && statusCode < 300;
  }
}
