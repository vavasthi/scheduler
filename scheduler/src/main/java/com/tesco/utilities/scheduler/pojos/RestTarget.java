/*
 * Copyright (c) 2018 Author vinayavasthi
 *
 * This software is a property of Tesco PLC
 */

package com.tesco.utilities.scheduler.pojos;

import java.util.Map;

public class RestTarget extends Target {

  public enum METHOD {
    GET,
    POST,
    DELETE
  }
  public RestTarget() {
  }

  public RestTarget(String url, METHOD method, String contentType, Map<String, String> headers) {
    this.url = url;
    this.method = method;
    this.contentType = contentType;
    this.headers = headers;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public METHOD getMethod() {
    return method;
  }

  public void setMethod(METHOD method) {
    this.method = method;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }

  private String url;
  private METHOD method;
  private String contentType;
  private Map<String, String> headers;
}
