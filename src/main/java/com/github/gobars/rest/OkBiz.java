package com.github.gobars.rest;

/**
 * 从业务上判断http调用是否成功.
 *
 * @author bingoohuang.
 */
public interface OkBiz<T> {
  /**
   * 判断业务是否成功.
   *
   * <p>实现者再判断业务不成功时，可以：
   *
   * <p>1. 返回false，由rest抛出RestBizException
   *
   * <p>2. 直接抛出自定义的异常
   *
   * @param stateCode 返回的HTTP状态码
   * @param resultPayload 返回体
   * @param resultBean 反序列化后的Bean
   * @return 是否成功
   */
  boolean isOk(int stateCode, String resultPayload, T resultBean);
}
