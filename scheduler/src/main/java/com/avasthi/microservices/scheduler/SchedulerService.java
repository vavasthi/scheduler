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
import com.avasthi.microservices.pojos.ScheduledItem;
import com.avasthi.microservices.utils.SchedulerCronUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Log4j2
public class SchedulerService extends AbstractService{


  @Value("${scheduler.max_threads:1000}")
  private int maxThreads;
  @Value("${scheduler.thread_pool.queue_size:10000}")
  private int threadPoolQueueSize;
  @Value("${scheduler.thread_pool.keepalive:10}")
  private int keepAliveTime;

  @Autowired
  private SchedulerCacheService schedulerCacheService;

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
    log.info("Running scheduler @ " + timestamp.toString());
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

        String key = SchedulerCronUtils.INSTANCE.getBucketKey(timestamp);
        Optional<ScheduledItem> optionalScheduledItem = schedulerCacheService.processNext(key);
        while (optionalScheduledItem.isPresent()) {
          ScheduledItem scheduledItem = optionalScheduledItem.get();
          /**
           * Add the item into the being processed set. This is to take care of crash scenarios.
           */
          log.info("Scheduling item " + scheduledItem.getId().toString());
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
          optionalScheduledItem = schedulerCacheService.processNext(key);
        }
        schedulerCacheService.checkPoint(key, timestamp);
      }
    });
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
