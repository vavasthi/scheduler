package com.avasthi.microservices.caching;

import com.avasthi.microservices.annotations.DefineCache;
import com.avasthi.microservices.pojos.ScheduledItem;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@DefineCache(name = SchedulerConstants.SCHEDULER_CACHE_NAME,
        prefix = SchedulerConstants.SCHEDULER_CACHE_PEFIX,
        expiry = SchedulerConstants.NEVER_EXPIRE)
@Service
public class SchedulerCacheService extends AbstractGeneralCacheService {

  public ScheduledItem scheduleItem(ScheduledItem item) {

    String key = getBucketKey(item.getTimestamp());
    addToList(key, item);
    return item;
  }
  private String getBucketKey(Date timestamp) {

    Calendar calendar = Calendar.getInstance();
    calendar.setTime(timestamp);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    SimpleDateFormat format = new SimpleDateFormat(SchedulerConstants.DATE_KEY_FORMAT);
    return format.format(calendar.getTime());
  }
}
