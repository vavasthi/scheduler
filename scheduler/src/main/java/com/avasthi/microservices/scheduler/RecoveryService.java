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
import com.avasthi.microservices.pojos.*;
import com.avasthi.microservices.utils.SchedulerCronUtils;
import com.avasthi.microservices.utils.SchedulerKafkaProducer;
import com.avasthi.microservices.utils.SchedulerThreadFactory;
import lombok.extern.log4j.Log4j2;
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

import java.util.*;
import java.util.concurrent.*;

@Service
@Log4j2
public class RecoveryService extends AbstractService{

  @Autowired
  private SchedulerCacheService schedulerCacheService;

  private static Logger logger = LoggerFactory.getLogger(RecoveryService.class.getName());

  @Scheduled(cron = "0 */5 * * * ?")
  public void processPerFiveMinute() {
    Optional<Checkpoint> lastCheckpointKey = schedulerCacheService.getLastCheckpoint();
    int pendingCount = 0;
    if (lastCheckpointKey.isPresent()) {
      Checkpoint cp = lastCheckpointKey.get();
      Date ts = cp.getTimestamp();
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(ts);;
      String key = SchedulerCronUtils.INSTANCE.getBucketKey(ts);
      Date now = new Date();
      String currentKey = SchedulerCronUtils.INSTANCE.getBucketKey(new Date());
      while(ts.before(now)) {

        ++pendingCount;
        Optional<ScheduledItem> optionalScheduledItem = schedulerCacheService.processNext(key);
        while(optionalScheduledItem.isPresent()) {

          ScheduledItem scheduledItem = optionalScheduledItem.get();
          schedulerCacheService.addToPendingQueue(scheduledItem);
          optionalScheduledItem = schedulerCacheService.processNext(key);
        }
        calendar.add(Calendar.MINUTE, 1);
        ts = calendar.getTime();
        key = SchedulerCronUtils.INSTANCE.getBucketKey(ts);
      }
      if (pendingCount > 0) {

        log.info(String.format("%d Pending jobs found. Scheduling..", pendingCount));
        getExecutorService().submit(new Runnable() {
          @Override
          public void run() {

            Optional<ScheduledItem> optionalScheduledItem
                    = schedulerCacheService.processNext(SchedulerConstants.PENDING_REQUEST_KEY);
            while (optionalScheduledItem.isPresent()) {
              ScheduledItem scheduledItem = optionalScheduledItem.get();
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

                  processItem(schedulerCacheService, si); // Process the item based on the configuration provided.
                }
              }, delay, TimeUnit.MILLISECONDS);
              optionalScheduledItem = schedulerCacheService.processNext(SchedulerConstants.PENDING_REQUEST_KEY);
            }
          }
        });
      }
    }
  }
}
