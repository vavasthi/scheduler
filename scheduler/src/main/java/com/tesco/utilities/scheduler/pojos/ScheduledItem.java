/*
 * Copyright (c) 2018 Author vinayavasthi
 *
 * This software is a property of Tesco PLC
 */

package com.tesco.utilities.scheduler.pojos;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class ScheduledItem implements Serializable {

  public ScheduledItem() {

  }
  public ScheduledItem(Date timestamp,
                       RetrySpecification retry,
                       MessageTarget messageTarget,
                       RestTarget restTarget) {
    this.id = UUID.randomUUID();
    this.timestamp = timestamp;
    this.retry = retry;
    this.messageTarget = messageTarget;
    this.restTarget = restTarget;
  }

  public ScheduledItem(Date timestamp,
                       MessageTarget messageTarget) {
    this.id = UUID.randomUUID();
    this.timestamp = timestamp;
    this.messageTarget = messageTarget;
  }

  public ScheduledItem(Date timestamp,
                       RestTarget restTarget) {
    this.id = UUID.randomUUID();
    this.timestamp = timestamp;
    this.restTarget = restTarget;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getResponseId() {
    return responseId;
  }

  public void setResponseId(UUID responseId) {
    this.responseId = responseId;
  }

  public UUID getRequestId() {
    return requestId;
  }

  public void setRequestId(UUID requestId) {
    this.requestId = requestId;
  }

  public String getRepeatSpecification() {
    return repeatSpecification;
  }

  public void setRepeatSpecification(String repeatSpecification) {
    this.repeatSpecification = repeatSpecification;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public MessageTarget getMessageTarget() {
    return messageTarget;
  }

  public void setMessageTarget(MessageTarget messageTarget) {
    this.messageTarget = messageTarget;
  }

  public RestTarget getRestTarget() {
    return restTarget;
  }

  public void setRestTarget(RestTarget restTarget) {
    this.restTarget = restTarget;
  }

  public RetrySpecification getRetry() {
    return retry;
  }

  public void setRetry(RetrySpecification retry) {
    this.retry = retry;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public Date getTriedAt() {
    return triedAt;
  }

  public void setTriedAt(Date triedAt) {
    this.triedAt = triedAt;
  }
  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public String getResponseBody() {
    return responseBody;
  }

  public void setResponseBody(String responseBody) {
    this.responseBody = responseBody;
  }

  public void tryingExecution() {
    ++count;
    triedAt = new Date();
  }

  public MessageTarget getMessageCallback() {
    return messageCallback;
  }

  public void setMessageCallback(MessageTarget messageCallback) {
    this.messageCallback = messageCallback;
  }

  public RestTarget getRestCallback() {
    return restCallback;
  }

  public void setRestCallback(RestTarget restCallback) {
    this.restCallback = restCallback;
  }

  private UUID id;
  private UUID responseId;
  private UUID requestId;
  private String repeatSpecification;
  private String key;
  private Date timestamp;
  private int count;
  private MessageTarget messageTarget;
  private RestTarget restTarget;
  private MessageTarget messageCallback;
  private RestTarget restCallback;
  private RetrySpecification retry;
  private Date triedAt;
  private String body;
  private String responseBody;


}
