package com.avasthi.microservices.caching;

import com.avasthi.microservices.annotations.DefineCache;
import com.avasthi.microservices.pojos.RetrySpecification;
import com.avasthi.microservices.pojos.ScheduledItem;
import com.avasthi.microservices.utils.SchedulerCronUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@DefineCache(name = SchedulerConstants.SCHEDULER_CACHE_NAME,
        prefix = SchedulerConstants.SCHEDULER_CACHE_PEFIX,
        expiry = SchedulerConstants.ONE_DAY)
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
    if (scheduledItem.getTimestamp() == null) {
      scheduledItem.setTimestamp(SchedulerCronUtils.INSTANCE.getNextTimestamp(scheduledItem.getRepeatSpecification()));
      logger.info("Reschedule specification is present and timeout is null. Rescheduling at " + (new Date()).toString() + " for " + scheduledItem.getTimestamp().toString());
    }
    if (scheduledItem.getId() == null) {
      scheduledItem.setId(UUID.randomUUID());
    }
    Date timestamp = scheduledItem.getTimestamp();
    Date now = new Date();
    if ((timestamp.getTime() - now.getTime() - 60000) <= 0) {
      timestamp.setTime(now.getTime() + 60000);
    }
    String key = getBucketKey(timestamp);
    logger.info("Scheduling the task for " + key);
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
      long t = scheduledItem.getTimestamp().getTime();
      remove(scheduledItem.getId());
      scheduledItem.setCount(scheduledItem.getCount() + 1);
      int timeout = retrySpecification.getTimeout() * Double.valueOf(Math.pow(2, scheduledItem.getCount())).intValue();
      t += (timeout * 1000);
      scheduledItem.setTimestamp(new Date(t));
      store(scheduledItem);
    }
  }

  /**
   * This method will return ScheduledItems with UUID in BEING_PROCESSED set which are older than 5 minutes beyond
   * scheduled time. These items are lying here because of some instance crash and we reschedule them to nearlest slot.
   * @return
   */
  public Set<ScheduledItem> getStaleScheduledItems() {

    Set<UUID> staleUUIDs = getRandomStalePendingObjects(SchedulerConstants.STALE_UUID_BLOCK_SIZE);
    Set<ScheduledItem> scheduledItems = new HashSet<>();
    final Date now = new Date();
    staleUUIDs.stream().filter(new Predicate<UUID>() {
      @Override
      public boolean test(UUID uuid) {
        ScheduledItem si = get(uuid, ScheduledItem.class);
        if (si != null && (now.getTime() - si.getTimestamp().getTime()) > 60 * 1000) {
          scheduledItems.add(si);
          return true;
        }
        return false;
      }
    });
    return scheduledItems;
  }
  public void reschedulePendingItems(Set<ScheduledItem> scheduledItems) {
    Date now = new Date();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(now);
    calendar.add(Calendar.MINUTE, 2);
    Date nowPlus2 = calendar.getTime();
    String key = getBucketKey(now);
    for (ScheduledItem si : scheduledItems) {
      rescheduleFromBeingProcessed(key, si.getId());
    }
  }
  public void update(ScheduledItem scheduledItem) {
    Map<Object, Object> keyValuePair = new HashMap<>();
    keyValuePair.put(scheduledItem.getId(), scheduledItem);
    storeObject(keyValuePair);
  }
}
