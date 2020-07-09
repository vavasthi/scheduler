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

package com.avasthi.microservices.caching;

import com.avasthi.microservices.annotations.DefineCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class AbstractCacheService {

  private static final Logger logger = LogManager.getLogger(AbstractCacheService.class);

  protected long getExpiry() {

    return this.getClass().getAnnotation(DefineCache.class).expiry();
  }
  protected String getPrefix() {
    return getClass().getAnnotation(DefineCache.class).prefix();
  }


  protected void storeObject(RedisTemplate<Object, Object> redisTemplate,
                             final Map<Object, Object> keyValuePairs) {

    if(keyValuePairs == null){
      return;
    }

    Map<Object, Object> prefixedKeyValuePairs = new HashMap<>();
    for (Map.Entry<Object, Object> e : keyValuePairs.entrySet()) {
      if (e.getKey() != null) {

        prefixedKeyValuePairs.put(new CacheKeyPrefix(getPrefix(), e.getKey()), e.getValue());
      }
    }

    redisTemplate.opsForValue().multiSet(prefixedKeyValuePairs);
    if (getExpiry() != SchedulerConstants.NEVER_EXPIRE) {

      for (Object key : prefixedKeyValuePairs.keySet()) {

        redisTemplate.expire(key, getExpiry(), TimeUnit.SECONDS);
      }
    }
  }


  protected void deleteKey(RedisTemplate<Object, Object> redisTemplate,
                           final Object key) {

    if(key == null){
      return;
    }

    List<Object> nonNullKeys = new ArrayList<>();
    if (key != null) {
      nonNullKeys.add(new CacheKeyPrefix(getPrefix(), key));
    }
    redisTemplate.delete(nonNullKeys);
  }
  protected <T> Optional<T> get(RedisTemplate<Object, Object> redisTemplate,
                            Object key,
                            Class<T> type) {
    Object value = null;
    try {

      value = redisTemplate.opsForValue().get(new CacheKeyPrefix(getPrefix(), key));
    }
    catch(SerializationException serializationException) {
      deleteKey(redisTemplate, key);
    }
    catch(Exception ex) {

      value = redisTemplate.opsForValue().get(new CacheKeyPrefix(getPrefix(), key));
    }
    return Optional.ofNullable(type.cast(value));
  }

  protected <K, V> V addObjectToSet(RedisTemplate<Object, Object> redisTemplate,
                                    final K cacheKey,
                                    UUID valueId,
                                    final V cacheValue) {
    if (cacheKey == null) {
      return null;
    }
    CacheKeyPrefix cacheKeyPrefix = new CacheKeyPrefix(getPrefix(), cacheKey);
    CacheKeyPrefix valueIdPrefix = new CacheKeyPrefix(getPrefix(), valueId);
    List<Object> results = redisTemplate.execute(new SessionCallback<List<Object>>() {
      @Override
      public List<Object> execute(RedisOperations redisOperations) throws DataAccessException {
        redisOperations.multi();
        redisOperations.opsForValue().set(valueIdPrefix, cacheValue);
        redisOperations.opsForSet().add(cacheKeyPrefix, valueId);
        return redisOperations.exec();
      }

    });
    return cacheValue;
  }
  protected <K, V> Optional<V> getObjectFromSet(RedisTemplate<Object, Object> redisTemplate,
                                            final K cacheKey,
                                            Class<V> type) {
    if (cacheKey == null) {
      return Optional.empty();
    }
    CacheKeyPrefix cacheKeyPrefix = new CacheKeyPrefix(getPrefix(), cacheKey);
    final Object value = redisTemplate.opsForSet().randomMember(cacheKeyPrefix);
    if (value != null) {

      List<Object> results = redisTemplate.executePipelined(new SessionCallback<List<Object>>() {
        @Override
        public List<Object> execute(RedisOperations redisOperations) throws DataAccessException {
          redisOperations.opsForSet().remove(cacheKeyPrefix, value);
          redisOperations.opsForSet().add(SchedulerConstants.SCHEDULER_ITEM_BEING_PROCESSED_KEY, value);
          return null;
        }
      });
      logger.info("Results = " + results.size());
    }
    V returnValue = (value == null ? null : type.cast(redisTemplate.opsForValue().get(new CacheKeyPrefix(getPrefix(), value))));
    return Optional.ofNullable(returnValue);
  }
  protected <K, V> Set<UUID> getRandomStalePendingObjects(RedisTemplate<Object, Object> redisTemplate,
                                                  int count) {
    CacheKeyPrefix cacheKeyPrefix = new CacheKeyPrefix(getPrefix(),
            SchedulerConstants.SCHEDULER_ITEM_BEING_PROCESSED_KEY);
    Set<UUID> staleUUIDs = new HashSet<>();
    for (int i = 0; i < count; ++i) {

      final Object value = redisTemplate.opsForSet().randomMember(cacheKeyPrefix);
      if (value == null) {
        return staleUUIDs;
      }
      staleUUIDs.add((UUID)value);
    }
    return staleUUIDs;
  }
  protected <K, V> Optional<V> removeObjectFromSet(RedisTemplate<Object, Object> redisTemplate,
                                               final K cacheKey,
                                               final UUID valueKey,
                                               Class<V> type) {
    if (cacheKey == null) {
      return null;
    }
    CacheKeyPrefix cacheKeyPrefix = new CacheKeyPrefix(getPrefix(), cacheKey);
    final Object value = redisTemplate.opsForSet().remove(cacheKeyPrefix, valueKey);
    List<Object> results = redisTemplate.executePipelined(new SessionCallback<List<Object>>() {

      @Override
      public List<Object> execute(RedisOperations redisOperations) throws DataAccessException {
        redisOperations.opsForValue().get(new CacheKeyPrefix(getPrefix(), valueKey));
        redisOperations.opsForSet().remove(SchedulerConstants.SCHEDULER_ITEM_BEING_PROCESSED_KEY, valueKey);
        return null;
      }
    });
    return Optional.ofNullable(type.cast(value));
  }
  protected <K, V> void rescheduleFromBeingProcessed(RedisTemplate<Object, Object> redisTemplate,
                                                  final K cacheKey,
                                                  final UUID valueKey) {
    if (cacheKey == null) {
      return;
    }
    CacheKeyPrefix cacheKeyPrefix = new CacheKeyPrefix(getPrefix(), cacheKey);
    List<Object> results = redisTemplate.executePipelined(new SessionCallback<List<Object>>() {

      @Override
      public List<Object> execute(RedisOperations redisOperations) throws DataAccessException {
        redisOperations.opsForSet().remove(new CacheKeyPrefix(getPrefix(), SchedulerConstants.SCHEDULER_ITEM_BEING_PROCESSED_KEY),
                valueKey);
        redisOperations.opsForSet().add(cacheKeyPrefix, valueKey);
        return null;
      }
    });
  }
}
