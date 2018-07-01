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
import org.springframework.stereotype.Service;

import java.util.UUID;

@DefineCache(name = SchedulerConstants.SCHEDULER_ITEM_BEING_PROCESSED_CACHE_NAME,
        prefix = SchedulerConstants.SCHEDULER_ITEM_BEING_PROCESSED_CACHE_PEFIX,
        expiry = SchedulerConstants.NEVER_EXPIRE)
@Service
public class SchedulerItemBeingProcessedCacheService extends AbstractGeneralCacheService {

  public UUID scheduleItem(UUID id) {

    return store(id);
  }

  public UUID store(UUID id) {
    addToList(SchedulerConstants.SCHEDULER_ITEM_BEING_PROCESSED_KEY, id);
    return id;
  }
  public void recover() {
    UUID id;
    while ((id = getFromList(SchedulerConstants.SCHEDULER_ITEM_BEING_RECOVERED_KEY)) != null) {
      store(id);
    }
  }
  public UUID processNext() {

    UUID id = getFromList(SchedulerConstants.SCHEDULER_ITEM_BEING_PROCESSED_KEY);
    addToList(SchedulerConstants.SCHEDULER_ITEM_BEING_RECOVERED_KEY, id);
    return id;
  }

  /**
   * This method will remove the given value from the set.
   * @param id id of the item to be removed.
   */
  public void remove(UUID id) {
    remove(SchedulerConstants.SCHEDULER_ITEM_BEING_PROCESSED_KEY, id);
    remove(SchedulerConstants.SCHEDULER_ITEM_BEING_RECOVERED_KEY, id);
  }

  public void removeFromBeingProcessed(UUID id) {
    remove(SchedulerConstants.SCHEDULER_ITEM_BEING_PROCESSED_KEY, id);
  }
}
