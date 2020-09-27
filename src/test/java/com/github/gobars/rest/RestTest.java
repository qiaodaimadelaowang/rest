package com.github.gobars.rest;

import lombok.Cleanup;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.http.HttpResponse;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.util.Map;

/** 使用海草命令，weed server，启动服务端，做测试服务器 */
public class RestTest {
  static Process goRestServerProcess;
  static Process goWeedProcess;

  @BeforeClass
  @SneakyThrows
  public static void beforeClass() {
    String binDir = "";
    String os = System.getProperty("os.name", "generic").toLowerCase();
    if ((os.indexOf("mac") >= 0) || (os.indexOf("darwin") >= 0)) {
      binDir = "/mac-bin";
      //    } else if (os.indexOf("win") >= 0) {
      // ignore
    } else if (os.indexOf("nux") >= 0) {
      binDir = "/linux-bin";
    }
    if (binDir.isEmpty()) {
      throw new RuntimeException("Unsupported os " + os);
    }

    val goRestServer =
        new ProcessBuilder()
            .command("src/test/resources" + binDir + "/go-rest-server")
            .redirectErrorStream(true);
    val goWeed =
        new ProcessBuilder()
            .command("src/test/resources" + binDir + "/weed", "server")
            .redirectErrorStream(true);

    goRestServerProcess = goRestServer.start();
    goWeedProcess = goWeed.start();

    new Thread(() -> printOut(goRestServerProcess)).start();
    new Thread(() -> printOut(goWeedProcess)).start();

    Thread.sleep(6000L);
  }

  @AfterClass
  public static void afterClass() {
    goRestServerProcess.destroy();
    goWeedProcess.destroy();
  }

  @SneakyThrows
  public static void printOut(Process p) {
    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

    try {
      String line;
      while ((line = reader.readLine()) != null) {
        System.out.println(line);
      }
    } catch (IOException ignore) {
    }
  }

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

    new File("temp/").mkdirs();
    @Cleanup val fo = new FileOutputStream("temp/" + assign.getFid() + ".png");


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

    Res<DirAssign> res2 =
        rest.exec(
            req.method("POST")
                .url("http://127.0.0.1:8812/echo")
                .req(res)
                .type(new TypeRef<Res<DirAssign>>() {}.getType()));

    System.out.println(res2);
  }

  @Data
  public static class Res<T> {
    private int code;
    private String message;
    private T data;
  }
}
