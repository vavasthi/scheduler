package com.avasthi.microservices.caching;

import com.avasthi.microservices.pojos.ScheduledItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@DefineCache(name = SchedulerConstants.SCHEDULER_CACHE_NAME,
        prefix = SchedulerConstants.SCHEDULER_CACHE_PEFIX,
        expiry = SchedulerConstants.NEVER_EXPIRE)
@Service
public class SchedulerCacheService extends AbstractGeneralCacheService {

  @Value("${scheduler.active:900}")
  private Integer activeTasksDuration;

  public ScheduledItem scheduleItem(ScheduledItem item) {

    String key = getBucketKey(item.getTimestamp());
    addToList(key, item);
    return item;
  }
  private String getBucketKey(Date timestamp) {

    Calendar calendar = Calendar.getInstance();
    calendar.setTime(timestamp);
    int minutes = calendar.get(Calendar.MINUTE);
    int slot = minutes / activeTasksDuration;
    SimpleDateFormat format = new SimpleDateFormat(SchedulerConstants.DATE_KEY_FORMAT);
    calendar.set(Calendar.MINUTE, slot * activeTasksDuration);
    return format.format(calendar.getTime());
  }
}
