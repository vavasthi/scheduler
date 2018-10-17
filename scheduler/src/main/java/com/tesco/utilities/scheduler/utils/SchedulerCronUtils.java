/*
 * Copyright (c) 2018 Author vinayavasthi
 *
 * This software is a property of Tesco PLC
 */

package com.tesco.utilities.scheduler.utils;

import org.springframework.scheduling.support.CronSequenceGenerator;

import java.util.Date;

public class SchedulerCronUtils {

  public static SchedulerCronUtils INSTANCE = new SchedulerCronUtils();

  public Date getNextTimestamp(String cron) {
    final CronSequenceGenerator cronSequenceGenerator = new CronSequenceGenerator(cron);
    return cronSequenceGenerator.next(new Date());
  }
}
