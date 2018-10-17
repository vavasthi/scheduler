/*
 * Copyright (c) 2018 Author vinayavasthi
 *
 * This software is a property of Tesco PLC
 */

package com.tesco.utilities.scheduler.scheduler;

import com.tesco.utilities.scheduler.caching.SchedulerCacheService;
import com.tesco.utilities.scheduler.caching.SchedulerConstants;
import com.tesco.utilities.scheduler.pojos.ScheduledItem;
import com.tesco.utilities.scheduler.utils.SchedulerThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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
