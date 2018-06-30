package com.avasthi.microservices.exceptions;
import com.avasthi.microservices.annotations.ServiceResponse;

/**
 * Created by vinay on 3/30/16.
 */
@ServiceResponse(defaultCode = SchedulerResponseCode.GENERAL_UNPROCESSABLE_ENTITY)
public class LogEnableException extends SchedulerBaseException {

  public LogEnableException(String message) {
    super(message);
  }
}
