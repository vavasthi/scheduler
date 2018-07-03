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

import com.avasthi.microservices.caching.SchedulerCacheService;
import com.avasthi.microservices.caching.SchedulerConstants;
import com.avasthi.microservices.pojos.MessageTarget;
import com.avasthi.microservices.pojos.RestTarget;
import com.avasthi.microservices.pojos.RetrySpecification;
import com.avasthi.microservices.pojos.ScheduledItem;
import com.avasthi.microservices.utils.SchedulerKafkaProducer;
import com.avasthi.microservices.utils.SchedulerThreadFactory;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class SchedulerService {


  @Value("${scheduler.min_threads:10}")
  private int minThreads;
  @Value("${scheduler.max_threads:1000}")
  private int maxThreads;
  @Value("${scheduler.thread_pool.queue_size:10000}")
  private int threadPoolQueueSize;
  @Value("${scheduler.thread_pool.keepalive:10}")
  private int keepAliveTime;

  @Autowired
  private SchedulerCacheService schedulerCacheService;

  private  ExecutorService executorService;

  private static Logger logger = LoggerFactory.getLogger(SchedulerService.class.getName());

  private ExecutorService getExecutorService() {

    if (executorService == null) {

      ThreadFactory factory = new SchedulerThreadFactory()
              .setDaemon(false)
              .setNamePrefix("scheduler-service-pool")
              .setPriority(Thread.MAX_PRIORITY)
              .build();
      executorService = new ThreadPoolExecutor(minThreads,
              maxThreads,
              keepAliveTime,
              SchedulerConstants.THREAD_KEEPALIVE_TIMEUNIT,
              new LinkedBlockingQueue<>(threadPoolQueueSize));
    }
    return executorService;
  }

  @Scheduled(cron = "0 * * * * ?")
  public void processPerMinute() {
    Date timestamp = new Date();
    logger.info("Running scheduler @ " + timestamp.toString());
    ScheduledItem scheduledItem = null;
    while ((scheduledItem = schedulerCacheService.processNext(timestamp)) != null) {
      /**
       * Add the item into the being processed set. This is to take care of crash scenarios.
       */
      logger.info("Scheduling item " + scheduledItem.getId().toString());
      final ScheduledItem si = scheduledItem;
      getExecutorService().submit(new Runnable() {
        @Override
        public void run() {

          processItem(si); // Process the item based on the configuration provided.
        }
      });
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

      int retryCount = 1;
      if (retry != null) {
        retryCount = retry.getCount();
      }
      if (!processMessageTarget(messageTarget, scheduledItem.getBody(), retryCount)) {
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
      if (!processRestTarget(restTarget, scheduledItem.getBody())) {

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
    }
  }

  private boolean processRestTarget(RestTarget restTarget, String body) {

    try {
      OkHttpClient client = new OkHttpClient();
      Request.Builder requestBuilder = new Request.Builder().url(restTarget.getUrl());
      for (Map.Entry<String, String> e : restTarget.getHeaders().entrySet()) {
        requestBuilder.addHeader(e.getKey(), e.getValue());
      }
      if (restTarget.getMethod() == RestTarget.METHOD.POST) {
        requestBuilder.post(RequestBody.create(MediaType.parse(restTarget.getContentType()), body));
      }
      else if(restTarget.getMethod() == RestTarget.METHOD.GET) {
        requestBuilder.get();
      }
      else if(restTarget.getMethod() == RestTarget.METHOD.DELETE) {
        requestBuilder.delete(RequestBody.create(MediaType.parse(restTarget.getContentType()), body));
      }
      Response response = client.newCall(requestBuilder.build()).execute();
      return response.isSuccessful();
    } catch (Exception e) {
      // e.printStackTrace();
    }
    return false;
  }
  private boolean processMessageTarget(MessageTarget messageTarget, String body, int retryCount) {

    Producer<String, String> producer
            = SchedulerKafkaProducer.INSTANCE.getProducer(StringUtils.join(messageTarget.getServers(), ','), retryCount);
    try {

      producer.send(new ProducerRecord<>(messageTarget.getTopic(), 1, body, body));
    }
    finally {
      producer.flush();
      producer.close();
    }
    return true;
  }
}
