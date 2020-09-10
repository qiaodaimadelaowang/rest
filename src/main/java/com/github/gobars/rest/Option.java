package com.github.gobars.rest;

import lombok.Getter;
import lombok.SneakyThrows;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

public class Option implements Cloneable {
  @Getter private String method;
  @Getter private Type type;
  @Getter private Class clazz;
  @Getter private Object requestBody;
  @Getter private String url;
  // 下载文件输出流
  @Getter private OutputStream download;
  // 上传文件名称
  @Getter String fileName;
  // 上传文件输入流
  @Getter InputStream upload;
  @Getter private StatusCodeSuccessful stateCodeSuccessful = new StatusCodeSuccessful() {};
  @Getter private BizSuccessful bizSuccessful;

  @Override
  @SneakyThrows
  protected Option clone() {
    return (Option) super.clone();
  }

  public Option upload(String fileName, InputStream upload) {
    Option copy = this.clone();
    copy.fileName = fileName;
    copy.upload = upload;
    return copy;
  }

  public Option download(OutputStream download) {
    Option copy = this.clone();
    copy.download = download;
    return copy;
  }

  public Option method(String method) {
    Option copy = this.clone();
    copy.method = method;
    return copy;
  }

  public Option type(Type type) {
    Option copy = this.clone();
    copy.type = type;
    return copy;
  }

  public Option clazz(Class clazz) {
    Option copy = this.clone();
    copy.clazz = clazz;
    return copy;
  }

  public Option request(Object requestBody) {
    Option copy = this.clone();
    copy.requestBody = requestBody;
    return copy;
  }

  public Option url(String url) {
    Option copy = this.clone();
    copy.url = url;
    return copy;
  }

  public Option stateCodeSucc(StatusCodeSuccessful stateCodeSuccessful) {
    Option copy = this.clone();
    copy.stateCodeSuccessful = stateCodeSuccessful;
    return copy;
  }

  public Option bizSucc(BizSuccessful bizSuccessful) {
    Option copy = this.clone();
    copy.bizSuccessful = bizSuccessful;
    return copy;
  }
}