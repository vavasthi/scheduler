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

import java.io.Serializable;
import java.util.List;

public class MessageTarget extends Target {

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
