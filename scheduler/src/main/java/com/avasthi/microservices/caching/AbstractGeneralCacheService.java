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


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class AbstractGeneralCacheService extends AbstractCacheService {

  @Autowired
  private SchedulerRedisConfiguration redisConfiguration;

  protected void storeObject(final Map<Object, Object> keyValuePairs) {

    storeObject(redisConfiguration.redisTemplate(), keyValuePairs);
  }
  protected <T> Optional<T> get(Object key, Class<T> type) {
    return get(redisConfiguration.redisTemplate(), key, type);
  }

  protected <K, V> V addObjectToSet(final K cacheKey,
                                    final UUID valueId,
                                    final V cacheValue) {
    return addObjectToSet(redisConfiguration.redisTemplate(), cacheKey, valueId, cacheValue);
  }
  protected <K, V> Optional<V> removeObjectFromSet(final K cacheKey,
                                         final UUID valueId,
                                         Class<V> type) {
    return removeObjectFromSet(redisConfiguration.redisTemplate(), cacheKey, valueId, type);
  }
  protected <K, V> Optional<V> getObjectFromSet(final K cacheKey,
                                            Class<V> type) {

    return getObjectFromSet(redisConfiguration.redisTemplate(), cacheKey, type);
  }
  protected <K, V> Set<UUID> getRandomStalePendingObjects(int count) {

    return getRandomStalePendingObjects(redisConfiguration.redisTemplate(), count);
  }

  protected void rescheduleFromBeingProcessed(final String cacheKey,
                                              final UUID valueKey) {
    rescheduleFromBeingProcessed(redisConfiguration.redisTemplate(), cacheKey, valueKey);
  }
}
