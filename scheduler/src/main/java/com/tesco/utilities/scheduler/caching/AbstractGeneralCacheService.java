/*
 * Copyright (c) 2018 Author vinayavasthi
 *
 * This software is a property of Tesco PLC
 */

package com.tesco.utilities.scheduler.caching;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class AbstractGeneralCacheService extends AbstractCacheService {

  @Autowired
  private SchedulerRedisConfiguration redisConfiguration;

  protected void storeObject(final Map<Object, Object> keyValuePairs) {

    storeObject(redisConfiguration.redisTemplate(), keyValuePairs);
  }
  public <K,V> void storeValueAsList(K key,List<V> value){
      storeObjectInList(redisConfiguration.redisTemplate(),key,value);
  }

  public <K,V> void storeValueAsHyperLogLog(K key,Set<V> value){
      storeObjectInHyperLogLog(redisConfiguration.redisTemplate(),key,value);
  }

    public <K,V> Long getCountFromHyperLogLog(K key){
        return getCountFromHyperLogLog(redisConfiguration.redisTemplate(),key);
    }

    public <K,V> Long unionKeysFromHyperLogLog(K destinationKey,List<V> sourceKeys){
        return unionKeysFromHyperLogLog(redisConfiguration.redisTemplate(),destinationKey,sourceKeys);
    }



  public <K> void incrementByDeltaValue(K key,final long delta){
        incrementStringValue(redisConfiguration.redisTemplate(),key,delta);
  }

  protected void deleteObject(final List<Object> keys) {

    deleteObject(redisConfiguration.redisTemplate(), keys);
  }

  protected void deleteKey(final Object key) {

    deleteKey(redisConfiguration.redisTemplate(), key);
  }
  protected <T> T get(Object key, Class<T> type) {
    return get(redisConfiguration.redisTemplate(), key, type);
  }

  protected Boolean setIfAbsent(Object key, Object value) {

    return setIfAbsent(redisConfiguration.redisTemplate(), key, value);
  }
  protected <K, V> V addObjectToSet(final K cacheKey,
                                    final UUID valueId,
                                    final V cacheValue) {
    return addObjectToSet(redisConfiguration.redisTemplate(), cacheKey, valueId, cacheValue);
  }
  protected <K, V> V removeObjectFromSet(final K cacheKey,
                                         final UUID valueId) {
    return removeObjectFromSet(redisConfiguration.redisTemplate(), cacheKey, valueId);
  }
  protected <K, V> V addToList(final K cacheKey,
                               final V cacheValue) {
    return addToList(redisConfiguration.redisTemplate(), cacheKey, cacheValue);
  }
  protected <K, V> V getObjectFromSet(final K cacheKey) {

    return getObjectFromSet(redisConfiguration.redisTemplate(), cacheKey);
  }
  protected <K, V> Set<UUID> getRandomStalePendingObjects(int count) {

    return getRandomStalePendingObjects(redisConfiguration.redisTemplate(), count);
  }

  protected <K, V> V getFromList(final K cacheKey) {

    return getFromList(redisConfiguration.redisTemplate(), cacheKey);
  }
  protected <K, V> V remove(final K cacheKey, final V value) {

    return remove(redisConfiguration.redisTemplate(), cacheKey, value);
  }

  protected UUID removeFromBeingProcessed(final UUID value) {
    return removeFromBeingProcessed(redisConfiguration.redisTemplate(), value);
  }
  protected void rescheduleFromBeingProcessed(final String cacheKey,
                                              final UUID valueKey) {
    rescheduleFromBeingProcessed(redisConfiguration.redisTemplate(), cacheKey, valueKey);
  }
  protected Set<Object> keys(String pattern){
    return keys(redisConfiguration.redisTemplate(), pattern);
  }
}
