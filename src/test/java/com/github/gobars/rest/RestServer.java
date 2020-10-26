package com.github.gobars.rest;

import lombok.SneakyThrows;
import lombok.val;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RestServer {
  private final String[] params;

  public RestServer(String... params) {
    this.params = params;
    this.params[0] = "src/test/resources" + getBinDir() + "/" + params[0];
  }

  private Process process;

  @SneakyThrows
  public void start() {
    this.process = new ProcessBuilder().command(this.params).redirectErrorStream(true).start();
    new Thread(() -> printOut(this.process)).start();
  }

  public void stop() {
    process.destroy();
  }

  public static String getBinDir() {
    String binDir = "";
    String os = System.getProperty("os.name", "generic").toLowerCase();
    if (os.contains("mac") || os.contains("darwin")) {
      binDir = "/mac-bin";
      //    } else if (os.indexOf("win") >= 0) {
      // ignore
    } else if (os.contains("nux")) {
      binDir = "/linux-bin";
    }

    if (binDir.isEmpty()) {
      throw new RuntimeException("Unsupported os " + os);
    }

    return binDir;
  }

  @SneakyThrows
  public static void printOut(Process p) {
    val reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

    try {
      String line;
      while ((line = reader.readLine()) != null) {
        System.out.println(line);
      }
    } catch (IOException ignore) {
    }
  }
}
