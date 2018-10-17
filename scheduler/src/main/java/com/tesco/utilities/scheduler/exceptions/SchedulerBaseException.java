/*
 * Copyright (c) 2018 Author vinayavasthi
 *
 * This software is a property of Tesco PLC
 */

package com.tesco.utilities.scheduler.exceptions;

import com.tesco.utilities.scheduler.annotations.ServiceResponse;
import org.springframework.http.HttpStatus;

/**
 * Created by vinay on 1/11/16.
 */
@ServiceResponse(defaultCode = SchedulerResponseCode.GENERAL_UNIDENTIFIED)
public class SchedulerBaseException extends RuntimeException {

  public static final String LOG_FORMAT = "Error Code: %s; Data: %s; Message: %s";

  private boolean shouldLog = true;


  public SchedulerBaseException() {
    super("");
  }

  public SchedulerBaseException(String message) {
    super(message);
  }


  public boolean shouldLog() {
    return shouldLog;
  }

  public void shouldLog(boolean shouldLog) {
    this.shouldLog = shouldLog;
  }

  public HttpStatus getHttpStatus() {
    return this.getClass().getAnnotation(ServiceResponse.class).defaultCode().getDefaultHttpStatus();
  }
  public int getErrorCode() {
    return this.getClass().getAnnotation(ServiceResponse.class).defaultCode().id;
  }
}
