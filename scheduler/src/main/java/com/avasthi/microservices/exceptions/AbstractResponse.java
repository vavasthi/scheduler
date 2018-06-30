
package com.avasthi.microservices.exceptions;

import com.avasthi.microservices.caching.SchedulerConstants;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.net.HttpURLConnection;

@JsonInclude(Include.NON_NULL)
public abstract class AbstractResponse {

  Integer status= HttpURLConnection.HTTP_OK;
  Integer code;
  String message = SchedulerConstants.MSG_SUCCESS;
  String moreInfo;

  Object data = null;

  @JsonGetter("status")
  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  @JsonGetter("code")
  public Integer getCode() {
    return code;
  }

  public void setCode(Integer code) {
    this.code = code;
  }

  @JsonGetter("message")
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @JsonGetter("more_info")
  public String getMoreInfo() {
    return moreInfo;
  }

  public void setMoreInfo(String moreInfo) {
    this.moreInfo = moreInfo;
  }

  @JsonGetter("data")
  public Object getData() {
    return data;
  }

  public void setData(Object data) {
    this.data = data;
  }

}
