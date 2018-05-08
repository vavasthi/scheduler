package com.avasthi.microservices.pojos;

import java.io.Serializable;
import java.util.Map;

public class RestTarget implements Serializable {

  public RestTarget() {
  }

  public RestTarget(String url, Map<String, String> headers) {
    this.url = url;
    this.headers = headers;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }

  private String url;
  private Map<String, String> headers;
}
