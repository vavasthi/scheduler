package com.avasthi.microservices.pojos;

import java.io.Serializable;
import java.util.List;

public class MessageTarget implements Serializable {

  public MessageTarget() {

  }
  public MessageTarget(String topic, List<String> servers) {
    this.topic = topic;
    this.servers = servers;
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

  private String topic;
  private List<String> servers;
}
