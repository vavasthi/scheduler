package com.avasthi.microservices.caching;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
  protected <K, V> V addToList(final K cacheKey,
                               final V cacheValue) {
    return addToList(redisConfiguration.redisTemplate(), cacheKey, cacheValue);
  }

  protected Set<Object> keys(String pattern){
    return keys(redisConfiguration.redisTemplate(), pattern);
  }
}
