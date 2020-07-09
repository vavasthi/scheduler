/*
 * Copyright (c) 2018 Vinay Avasthi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.avasthi.microservices.pojos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@AllArgsConstructor
public class Target implements Serializable  {


  public enum STATUS {
    CREATED,
    COMPLETED,
    WAITING_FOR_RETRY,
    FAILED
  }

  public Target() {
    status = STATUS.CREATED;
    createdAt = new Date();
    count = 0;
  }
  public Target(STATUS status) {
    this.status = status;
  }

  public STATUS getStatus() {
    return status;
  }

  public void setStatus(STATUS status) {
    this.status = status;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public Date getCompletedAt() {
    return completedAt;
  }

  public void setCompletedAt(Date completedAt) {
    this.completedAt = completedAt;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public void markFailed() {
    status = STATUS.FAILED;
    completedAt = new Date();
  }
  public void markCompleted() {
    status = STATUS.COMPLETED;
    completedAt = new Date();
  }
  public void markRetrying() {
    status = STATUS.WAITING_FOR_RETRY;
  }

  public boolean done() {
    return status == STATUS.COMPLETED || status == STATUS.FAILED;
  }
  private STATUS status;
  private int count;
  private Date createdAt;
  private Date completedAt;
}
