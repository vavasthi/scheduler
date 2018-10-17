/*
 * Copyright (c) 2018 Author vinayavasthi
 *
 * This software is a property of Tesco PLC
 */

package com.tesco.utilities.scheduler.pojos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageTarget extends Target {

  public MessageTarget() {

  }
  public MessageTarget(String topic, int partition, List<String> servers, Map<String, String> properties) {
    this.topic = topic;
    this.partition = partition;
    this.servers = servers;
    this.properties = properties;
  }

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }

  public List<String> getServers() {
    return servers;
  }

  public void setServers(List<String> servers) {
    this.servers = servers;
  }

  public int getPartition() {
    return partition;
  }

  public void setPartition(int partition) {
    this.partition = partition;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  private String topic;
  private int partition;
  private List<String> servers;
  private Map<String, String> properties = new HashMap<>();
}
