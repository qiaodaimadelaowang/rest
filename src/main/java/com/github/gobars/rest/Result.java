package com.github.gobars.rest;

import lombok.Data;
import org.apache.http.HttpResponse;

@Data
public class Result {
  private String payload;
  private String resultBody;
  private int statusCode;
  private HttpResponse response;
}
