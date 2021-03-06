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

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

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

  private ScheduledExecutorService executorService;

  private static Logger logger = LoggerFactory.getLogger(SchedulerService.class.getName());

  private ScheduledExecutorService getExecutorService() {

    if (executorService == null) {

      ThreadFactory factory = new SchedulerThreadFactory()
              .setDaemon(false)
              .setNamePrefix("scheduler-service-pool")
              .setPriority(Thread.MAX_PRIORITY)
              .build();
      executorService = Executors.newScheduledThreadPool(minThreads, factory);
/*      executorService = new ThreadPoolExecutor(minThreads,
              maxThreads,
              keepAliveTime,
              SchedulerConstants.THREAD_KEEPALIVE_TIMEUNIT,
              new LinkedBlockingQueue<>(threadPoolQueueSize));*/
    }
    return executorService;
  }

  @Scheduled(cron = "0 * * * * ?")
  public void processPerMinute() {
    Date timestamp = new Date();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(timestamp);
    /**
     * It has been observed that the scheduled tasks sometime kick off little before the time, for example a task that
     * expected to kickoff at 11:30:00 is seen to kickoff at 11:29:55. This will take care of those scenarios. If the
     * number of seconds is great that 50 then we roll the time over to next minute.
     */
    if (calendar.get(Calendar.SECOND) > 50) {
      calendar.set(Calendar.SECOND, 0);
      calendar.add(Calendar.MINUTE, 1);
      timestamp = calendar.getTime();
    }
    logger.info("Running scheduler @ " + timestamp.toString());
    processTimestamp(timestamp);
  }

  @Scheduled(cron = "0 */15 * * * ?")
  public void processPerHour() {

    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    calendar.add(Calendar.HOUR, -24);
    processFrom(calendar.getTime());
  }

  private void processTimestamp(Date timestamp) {

    getExecutorService().submit(new Runnable() {
      @Override
      public void run() {

        ScheduledItem scheduledItem = null;
        while ((scheduledItem = schedulerCacheService.processNext(timestamp)) != null) {
          /**
           * Add the item into the being processed set. This is to take care of crash scenarios.
           */
          logger.info("Scheduling item " + scheduledItem.getId().toString());
          final ScheduledItem si = scheduledItem;
          Date now = new Date();
          long delay = scheduledItem.getTimestamp().getTime() - now.getTime();
          if (delay < 0) {
            delay = 0;
          }
          getExecutorService().schedule(new Runnable() {
            @Override
            public void run() {

              processItem(si); // Process the item based on the configuration provided.
            }
          }, delay, TimeUnit.MILLISECONDS);
        }
      }
    });
  }

  private void processItem(ScheduledItem scheduledItem) {
    logger.info("Processing " + scheduledItem.getId().toString() + " @ " + scheduledItem.getTimestamp().toString() + " current Time " + (new Date()).toString());

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
    if (retryItem
            && (!scheduledItem.getMessageTarget().done() || !scheduledItem.getRestTarget().done())
            && retry != null && scheduledItem.getCount() < retry.getCount()) {

      schedulerCacheService.retry(scheduledItem);
      schedulerCacheService.update(scheduledItem);
    }
    else {

      /**
       * Next two lines should always be at the end of this function. If the item was processed successfully, then
       * the item is deleted. This is required for that deletion.
       */
      schedulerCacheService.remove(scheduledItem.getId());
      if (scheduledItem.getRestCallback() != null || scheduledItem.getMessageCallback() != null) {
        ScheduledItem ns = new ScheduledItem(new Date(),
                scheduledItem.getRetry(),
                scheduledItem.getMessageCallback(),
                scheduledItem.getRestCallback());
        String body = scheduledItem.getResponseBody();
        if (scheduledItem.getMessageTarget() != null && scheduledItem.getMessageCallback() != null) {

          ns.setMessageTarget(scheduledItem.getMessageCallback());
          body = body.replaceAll("\\$messageStatus", scheduledItem.getMessageTarget().getStatus().name())
                  .replaceAll("\\$messageStatus", scheduledItem.getMessageTarget().getStatus().name())
                  .replaceAll("\\$messageCompletedAt", scheduledItem.getMessageTarget().getCompletedAt().toString());
        }
        if (scheduledItem.getRestTarget() != null && scheduledItem.getRestCallback() != null) {

          ns.setRestTarget(scheduledItem.getRestCallback());
          body = body.replaceAll("\\$id", scheduledItem.getId().toString())
                  .replaceAll("\\$id", scheduledItem.getId().toString())
                  .replaceAll("\\$restStatus", scheduledItem.getRestTarget().getStatus().name())
                  .replaceAll("\\$restCompletedAt", scheduledItem.getRestTarget().getCompletedAt().toString());
        }
        ns.setBody(body);
        ns.setRequestId(scheduledItem.getId());
        ns = schedulerCacheService.scheduleItem(ns);
        scheduledItem.setResponseId(ns.getId());

      }
      /**
       * This is the terminal state. If we have reached this state and there is a repeatSpecification on scheduledItem, then we need to reschedule it for next
       * timestamp. We set the id to null so that a new id can be generated and we set timestamp to null so that cronstring is used to generate next
       * occurence.
       */
      scheduledItem.setTimestamp(null);
      scheduledItem.setId(null);
      scheduledItem.setCount(0);
      schedulerCacheService.store(scheduledItem);
    }
  }

  /**
   * This method would process a rest target request
   * @param restTarget details of the rest target
   * @param body the body of the request passed
   * @return true if success, false if failure
   */
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
      if (response.isSuccessful()) {
        return true;
      }
      else {
        logger.info(response.body().string());
        return false;
      }
    } catch (Exception e) {
      logger.info("Exception in REST call." + e.getMessage() + " " + e.getLocalizedMessage(), e);
      // e.printStackTrace();
    }
    return false;
  }
  private boolean processMessageTarget(MessageTarget messageTarget, String body, int retryCount) {

    Producer<String, String> producer
            = SchedulerKafkaProducer.INSTANCE.getProducer(StringUtils.join(messageTarget.getServers(), ','),
            retryCount,
            messageTarget.getProperties());
    try {

      producer.send(new ProducerRecord<>(messageTarget.getTopic(), messageTarget.getPartition(), body, body));
    }
    finally {
      producer.flush();
      producer.close();
    }
    return true;
  }

  /**
   * This method will process all the buckets from the given time till now.
   * @param timestamp timestamp from which the buckets need to be processed.
   */
  public void processFrom(Date timestamp) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(timestamp);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);

    Calendar nowCalendar = Calendar.getInstance();
    while(calendar.before(nowCalendar)) {
      processTimestamp(calendar.getTime());
      calendar.add(Calendar.MINUTE, 1);
    }
  }
}
