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

package com.avasthi.microservices.scheduler;

import com.avasthi.microservices.caching.SchedulerConstants;
import com.avasthi.microservices.caching.SchedulerItemBeingProcessedCacheService;
import com.avasthi.microservices.pojos.*;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.avasthi.microservices.caching.SchedulerCacheService;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class SchedulerService {

  @Autowired
  private SchedulerCacheService schedulerCacheService;
  @Autowired
  private SchedulerItemBeingProcessedCacheService itemBeingProcessedCacheService;

  private static Logger logger = LoggerFactory.getLogger(SchedulerService.class.getName());

  @Scheduled(cron = "0 * * * * ?")
  public void processPerMinute() {
    Date timestamp = new Date();
    logger.info("Running scheduler @ " + timestamp.toString());
    ScheduledItem scheduledItem = null;
    while ((scheduledItem = schedulerCacheService.processNext(timestamp)) != null) {
      /**
       * Add the item into the being processed set. This is to take care of crash scenarios.
       */
      itemBeingProcessedCacheService.scheduleItem(scheduledItem.getId());
      logger.info("Scheduling item " + scheduledItem.getId().toString());
      processItem(scheduledItem); // Process the item based on the configuration provided.
    }
  }

  /**
   * This method is called on the start of the process. It is to take care of the items being processed
   * and a recovery after crash. It will move all the items from beingRecovered set to beingProcessedSet
   * and then will callback for all the items which are older than 5 seconds.
   */
  public void processPending() {
    itemBeingProcessedCacheService.recover();
    UUID id = null;
    ScheduledItem scheduledItem = null;
    while ((id = itemBeingProcessedCacheService.processNext()) != null) {

      Date now = new Date();
      scheduledItem = schedulerCacheService.getItem(id);
      if ((now.getTime() - scheduledItem.getTimestamp().getTime()) / (1000 * 60) > SchedulerConstants.FIVE_MINUTES) {
        /**
         * This item is five minutes old in the beingProcessedSet. It is very likely that it was an orphan because
         * of crash of an instance.
         */
        processItem(scheduledItem);
      }
    }
  }
  private void processItem(ScheduledItem scheduledItem) {
    logger.info("Processing " + scheduledItem.getId().toString() + " @ " + scheduledItem.getTimestamp().toString());

    MessageTarget messageTarget = scheduledItem.getMessageTarget();
    RestTarget restTarget = scheduledItem.getRestTarget();
    RetrySpecification retry = scheduledItem.getRetry();
    scheduledItem.tryingExecution();
    boolean retryItem = false;
    if (messageTarget != null && !messageTarget.done()) {

      if (!processMessageTarget(messageTarget)) {
        if (retry != null && retry.getCount() > messageTarget.getCount()) {
          scheduledItem.getMessageTarget().markRetrying();
          retryItem = true;
        }
        else {
          scheduledItem.getMessageTarget().markFailed();
        }
      }
      else {
        scheduledItem.getMessageTarget().markCompleted();
      }
    }
    if (restTarget != null && !restTarget.done()) {
      if (!processRestTarget(restTarget)) {

        if (retry != null && retry.getCount() > restTarget.getCount()) {
          scheduledItem.getRestTarget().markRetrying();
          retryItem = true;
        }
        else {
          scheduledItem.getRestTarget().markFailed();
        }
      }
      else {
        scheduledItem.getRestTarget().markCompleted();
      }
    }
    if (retryItem && (!scheduledItem.getMessageTarget().done() || !scheduledItem.getRestTarget().done())) {

      schedulerCacheService.retry(scheduledItem);
    }
    else {

      /**
       * Next two lines should always be at the end of this function. If the item was processed successfully, then
       * the item is deleted. This is required for that deletion.
       */
      schedulerCacheService.remove(scheduledItem.getId());
      itemBeingProcessedCacheService.remove(scheduledItem.getId());
    }
  }

  private boolean processRestTarget(RestTarget restTarget) {

    try {
      OkHttpClient client = new OkHttpClient();
      Request.Builder requestBuilder = new Request.Builder().url(restTarget.getUrl());
      for (Map.Entry<String, String> e : restTarget.getHeaders().entrySet()) {
        requestBuilder.addHeader(e.getKey(), e.getValue());
      }
      if (restTarget.getMethod() == RestTarget.METHOD.POST) {
        requestBuilder.post(RequestBody.create(MediaType.parse(restTarget.getContentType()), restTarget.getBody()));
      }
      else if(restTarget.getMethod() == RestTarget.METHOD.GET) {
        requestBuilder.get();
      }
      else if(restTarget.getMethod() == RestTarget.METHOD.DELETE) {
        requestBuilder.delete(RequestBody.create(MediaType.parse(restTarget.getContentType()), restTarget.getBody()));
      }
      Response response = client.newCall(requestBuilder.build()).execute();
      return response.isSuccessful();
    } catch (Exception e) {
      // e.printStackTrace();
    }
    return false;
  }
  private boolean processMessageTarget(MessageTarget messageTarget) {

    return false;
  }
}
