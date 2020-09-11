package com.github.gobars.rest;

import lombok.Cleanup;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.http.HttpResponse;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;

/** 使用海草命令，weed server，启动服务端，做测试服务器 */
public class RestTest {
  @Test
  public void get() {
    String status = new Rest().exec(new Option().url("http://127.0.0.1:8080/status"));
    System.out.println(status);
  }

  @Data
  public static class DirAssign {
    private int count;
    private String fid;
    private String publicUrl;
  }

  @Data
  public static class UploadResult {
    // {"name":"biniki.jpeg","size":1121511,"eTag":"87aeed08"}
    private String name;
    private int size;
    private String eTag;
  }

  @Test
  @SneakyThrows
  public void upload() {
    Rest rest = new Rest();
    String assignUrl = "http://127.0.0.1:9333/dir/assign";
    DirAssign assign = rest.exec(new Option().url(assignUrl).clazz(DirAssign.class));
    System.out.println(assign);

    @Cleanup val upload = new FileInputStream("src/test/resources/bikini.png");
    String url = "http://" + assign.getPublicUrl() + "/" + assign.getFid();
    UploadResult uploadResult =
        rest.exec(new Option().url(url).upload("biniki.png", upload).clazz(UploadResult.class));
    System.out.println(uploadResult);

    @Cleanup val fo = new FileOutputStream(assign.getFid() + ".png");

    String downloadRest = rest.exec(new Option().url(url).download(fo));
    System.out.println(downloadRest);

    Map<String, String> headers = rest.exec(new Option().url(url).method("HEAD"));
    System.out.println(headers);

    Option req = new Option().url("http://127.0.0.1:8812").req(assign);
    String postResult = rest.exec(req);
    System.out.println(postResult);

    DirAssign cloneAssign =
        rest.exec(req.method("POST").url("http://127.0.0.1:8812/echo").clazz(DirAssign.class));
    System.out.println(cloneAssign);

    HttpResponse rsp =
        rest.exec(req.method("POST").url("http://127.0.0.1:8812/echo").clazz(HttpResponse.class));
    System.out.println(rsp);

    Result result =
        rest.exec(req.method("POST").url("http://127.0.0.1:8812/echo").clazz(Result.class));
    System.out.println(result);

    Res<DirAssign> res = new Res<>();
    res.setCode(200);
    res.setMessage("OK");
    res.setData(assign);

    Res<DirAssign> res2 = rest.exec(req.method("POST")
            .url("http://127.0.0.1:8812/echo")
            .req(res)
            .type(new PTypeRef<Res<DirAssign>>(){}.getType()));

    System.out.println(res2);
  }

  @Data
  public static class Res<T> {
    private int code;
    private String message;
    private T data;
  }
}
