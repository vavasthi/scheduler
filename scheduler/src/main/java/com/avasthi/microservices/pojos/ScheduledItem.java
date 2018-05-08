package com.avasthi.microservices.pojos;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class ScheduledItem implements Serializable {

  public ScheduledItem() {

  }
  public ScheduledItem(Date timestamp,
                       MessageTarget messageTarget,
                       RestTarget restTarget) {
    this.id = UUID.randomUUID();
    this.timestamp = timestamp;
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

  private UUID id;
  private Date timestamp;
  private MessageTarget messageTarget;
  private RestTarget restTarget;
}
