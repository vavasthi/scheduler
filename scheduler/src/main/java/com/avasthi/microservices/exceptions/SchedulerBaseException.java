package com.avasthi.microservices.exceptions;

import com.avasthi.microservices.annotations.ServiceResponse;
import org.springframework.http.HttpStatus;

/**
 * Created by vinay on 1/11/16.
 */
@ServiceResponse(defaultCode = SchedulerResponseCode.GENERAL_UNIDENTIFIED)
public class SchedulerBaseException extends RuntimeException {

  public static final String LOG_FORMAT = "Error Code: %s; Data: %s; Message: %s";

  private int errorCode;
  private HttpStatus httpStatus;
  private boolean shouldLog = true;


  public SchedulerBaseException() {
    super("");
  }

  public SchedulerBaseException(int errorCode, HttpStatus httpStatus, String message) {
    super(message);
    this.errorCode=errorCode;
    this.httpStatus = httpStatus;
  }

  public SchedulerBaseException(String message) {
    super(message);
  }

  public SchedulerBaseException(Throwable cause) {
    super(cause);
  }

  public SchedulerBaseException(String message, Throwable cause) {
    super(message, cause);
  }

  public SchedulerBaseException(String message, Throwable cause,
                               boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public SchedulerBaseException(String errorCode, Object data, String details) {
    super(String.format(LOG_FORMAT, errorCode, data, details));
  }

  public SchedulerBaseException(String errorCode, Object data, String details, Throwable throwable) {
    super(String.format(LOG_FORMAT, errorCode, data, details), throwable);
  }

  public HttpStatus getHttpStatus() {
    return httpStatus;
  }

  public void setHttpStatus(HttpStatus httpStatus) {
    this.httpStatus = httpStatus;
  }

  public int getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(int errorCode) {
    this.errorCode = errorCode;
  }

  public boolean shouldLog() {
    return shouldLog;
  }

  public void shouldLog(boolean shouldLog) {
    this.shouldLog = shouldLog;
  }
}
