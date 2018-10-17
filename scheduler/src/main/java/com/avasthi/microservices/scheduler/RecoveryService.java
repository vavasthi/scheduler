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
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class RecoveryService {


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

  private static Logger logger = LoggerFactory.getLogger(RecoveryService.class.getName());

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

  @Scheduled(cron = "0 */5 * * * ?")
  public void processPerFiveMinute() {
    Set<ScheduledItem> staleScheduledItems = schedulerCacheService.getStaleScheduledItems();
    logger.info(String.format("Found %d stale Items. Rescheduling them.", staleScheduledItems.size()));
    if (staleScheduledItems.size() > 0) {

      schedulerCacheService.reschedulePendingItems(staleScheduledItems);
    }
  }
}
