package com.avasthi.microservices.caching;

import com.avasthi.microservices.annotations.DefineCache;
import com.avasthi.microservices.pojos.RetrySpecification;
import com.avasthi.microservices.pojos.ScheduledItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@DefineCache(name = SchedulerConstants.SCHEDULER_CACHE_NAME,
        prefix = SchedulerConstants.SCHEDULER_CACHE_PEFIX,
        expiry = SchedulerConstants.NEVER_EXPIRE)
@Service
public class SchedulerCacheService extends AbstractGeneralCacheService {

  private static Logger logger = LoggerFactory.getLogger(SchedulerCacheService.class.getName());
  public ScheduledItem scheduleItem(ScheduledItem item) {

    return store(item);
  }

  /**
   * This function generates a key for redis from the timestamp
   * @param timestamp the timestamp
   * @return the key
   */
  private String getBucketKey(Date timestamp) {

    Calendar calendar = Calendar.getInstance();
    calendar.setTime(timestamp);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    SimpleDateFormat format = new SimpleDateFormat(SchedulerConstants.DATE_KEY_FORMAT);
    return format.format(calendar.getTime());
  }
  public ScheduledItem store(ScheduledItem scheduledItem) {
    if (scheduledItem.getId() == null) {
      scheduledItem.setId(UUID.randomUUID());
    }
    Date timestamp = scheduledItem.getTimestamp();
    Date now = new Date();
    if ((timestamp.getTime() - now.getTime() + 60000) <= 0) {
      timestamp.setTime(now.getTime() + 60000);
    }
    logger.info("Scheduling the task for " + timestamp);
    String key = getBucketKey(timestamp);
    store(scheduledItem, key);
    return scheduledItem;
  }
  public ScheduledItem store(ScheduledItem scheduledItem, String key) {
    scheduledItem.setKey(key);
    addObjectToSet(key, scheduledItem.getId(), scheduledItem);
    return scheduledItem;
  }
  public ScheduledItem getItem(UUID id) {

    ScheduledItem item = get(id, ScheduledItem.class);
    return item;
  }
  public ScheduledItem processNext(Date timestamp) {

    String key = getBucketKey(timestamp);
    ScheduledItem item = getObjectFromSet(key);
    return item;
  }
  public ScheduledItem remove(UUID id) {
    logger.info("Removing item with id = " + id.toString());
    ScheduledItem scheduledItem = get(id, ScheduledItem.class);
    if (scheduledItem != null) {

      String key = scheduledItem.getKey();
      return removeObjectFromSet(key, id);
    }
    else {
      return null;
    }
  }

  public void retry(ScheduledItem scheduledItem) {
    RetrySpecification retrySpecification = scheduledItem.getRetry();
    if (retrySpecification != null && scheduledItem.getCount() < retrySpecification.getCount()) {
      remove(scheduledItem.getId());
      int timeout = retrySpecification.getTimeout() * new Double(Math.pow(2, scheduledItem.getCount())).intValue();
      long t = scheduledItem.getTimestamp().getTime();
      t += (timeout * 1000);
      scheduledItem.setTimestamp(new Date(t));
      store(scheduledItem);
    }
  }
}
