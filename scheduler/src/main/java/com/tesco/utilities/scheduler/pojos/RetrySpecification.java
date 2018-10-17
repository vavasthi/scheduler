/*
 * Copyright (c) 2018 Author vinayavasthi
 *
 * This software is a property of Tesco PLC
 */

package com.tesco.utilities.scheduler.pojos;

import java.io.Serializable;

public class RetrySpecification implements Serializable {
  public RetrySpecification() {
  }

  public RetrySpecification(int count, int timeout) {
    this.count = count;
    this.timeout = timeout;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public int getTimeout() {
    return timeout;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  private int count;
  private int timeout;
}
