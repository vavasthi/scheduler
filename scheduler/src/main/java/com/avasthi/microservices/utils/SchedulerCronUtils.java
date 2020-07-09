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

package com.avasthi.microservices.utils;

import com.avasthi.microservices.caching.SchedulerConstants;
import org.springframework.scheduling.support.CronSequenceGenerator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SchedulerCronUtils {

  public static SchedulerCronUtils INSTANCE = new SchedulerCronUtils();

  public Date getNextTimestamp(String cron) {
    final CronSequenceGenerator cronSequenceGenerator = new CronSequenceGenerator(cron);
    return cronSequenceGenerator.next(new Date());
  }
  /**
   * This function generates a key for redis from the timestamp
   * @param timestamp the timestamp
   * @return the key
   */
  public String getBucketKey(Date timestamp) {

    Calendar calendar = Calendar.getInstance();
    calendar.setTime(timestamp);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    SimpleDateFormat format = new SimpleDateFormat(SchedulerConstants.DATE_KEY_FORMAT);
    return format.format(calendar.getTime());
  }
  public Date getTimestamp(String key) {

    SimpleDateFormat format = new SimpleDateFormat(SchedulerConstants.DATE_KEY_FORMAT);
    try {
      return format.parse(key);
    } catch (ParseException e) {
      return new Date();
    }
  }
}
