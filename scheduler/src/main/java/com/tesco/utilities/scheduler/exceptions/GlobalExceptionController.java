
/*
 * Copyright (c) 2018 Author vinayavasthi
 *
 * This software is a property of Tesco PLC
 */

package com.tesco.utilities.scheduler.exceptions;


import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * This class is responsible of handling all exceptions, like giving proper response back
 * to client, whether to log exception or not
 */
@ControllerAdvice
public class GlobalExceptionController extends ResponseEntityExceptionHandler {
  /**
   * Gets root cause.
   *
   * @param ex the ex
   * @return the root cause
   */
  public static String getRootCause(Throwable ex) {
    return (ex.getCause() == null ? ex.getMessage() : getRootCause(ex.getCause()));
  }


  /**
   * Handle hubble base exception response entity.
   *
   * @param request the request
   * @param hbe     the hbe
   * @return the response entity
   */
  @ExceptionHandler(value = SchedulerBaseException.class)
  @ResponseBody
  public ResponseEntity<Object> handleBaseException(WebRequest request, SchedulerBaseException hbe) {

    HttpServletRequest httpServletRequest = request instanceof ServletWebRequest
            ? ((ServletWebRequest) request).getRequest()
            : (HttpServletRequest) request;

    Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
    HttpHeaders headers  = new HttpHeaders();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      Enumeration<String> headerValues = httpServletRequest.getHeaders(headerName);
      while(headerValues.hasMoreElements()) {
        String headerValue = headerValues.nextElement();
        headers.add(headerName, headerValue);
      }
    }
    return handleExceptionInternal(hbe,
            hbe,
            headers,
            hbe.getHttpStatus(),
            request);
  }
}
