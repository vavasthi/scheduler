/*
 * Copyright (c) 2018 Author vinayavasthi
 *
 * This software is a property of Tesco PLC
 */

package com.tesco.utilities.scheduler.exceptions;
import com.tesco.utilities.scheduler.annotations.ServiceResponse;

/**
 * Created by vinay on 3/30/16.
 */
@ServiceResponse(defaultCode = SchedulerResponseCode.GENERAL_NOT_FOUND)
public class NotFoundException extends SchedulerBaseException {

  public NotFoundException(String message) {
    super(message);
  }
}
