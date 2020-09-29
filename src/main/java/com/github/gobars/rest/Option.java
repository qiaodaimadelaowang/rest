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
  @Getter private OkStatus okStatus = new OkStatus() {};
  @Getter private OkBiz okBiz;

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

  public <T> Option type(TypeRef<T> ref) {
    return type(ref.getType());
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

  public Option req(Object requestBody) {
    Option copy = this.clone();
    copy.requestBody = requestBody;
    return copy;
  }

  public Option url(String url) {
    Option copy = this.clone();
    copy.url = url;
    return copy;
  }

  public Option okStatus(OkStatus okStatus) {
    Option copy = this.clone();
    copy.okStatus = okStatus;
    return copy;
  }

  public Option okBiz(OkBiz okBiz) {
    Option copy = this.clone();
    copy.okBiz = okBiz;
    return copy;
  }
}
