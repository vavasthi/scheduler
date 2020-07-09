
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

package com.avasthi.microservices.endpoints;

import com.avasthi.microservices.caching.SchedulerCacheService;
import com.avasthi.microservices.caching.SchedulerConstants;
import com.avasthi.microservices.exceptions.NotFoundException;
import com.avasthi.microservices.pojos.MessageTarget;
import com.avasthi.microservices.pojos.ScheduledItem;
import com.avasthi.microservices.pojos.SchedulerBasicReturnValue;
import com.avasthi.microservices.scheduler.SchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.*;


@RestController
@RequestMapping(SchedulerConstants.VERSION_1 + SchedulerConstants.SCHEDULER_ENDPOINT)
public class SchedulerEndpoint {

  @Autowired
  private SchedulerService schedulerService;
  @Autowired
  private SchedulerCacheService schedulerCacheService;


  @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public @ResponseBody
  Optional<ScheduledItem> create(@RequestBody ScheduledItem scheduledItem) {

    scheduledItem.setId(UUID.randomUUID());
    return schedulerCacheService.scheduleItem(scheduledItem);
  }

  @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public @ResponseBody Optional<ScheduledItem> create(@PathVariable(value = "id") UUID id) {

    return schedulerCacheService.getItem(id);
  }
  @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
  public @ResponseBody Optional<ScheduledItem> delete(@PathVariable(value = "id") UUID id) {

    Optional<ScheduledItem> optionalScheduledItem = schedulerCacheService.remove(id);
    if (optionalScheduledItem.isPresent()) {
      return optionalScheduledItem;
    }
   throw new NotFoundException(String.format("%s not found", id.toString()));
  }
  @RequestMapping(value = "/replay/{timestamp}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public @ResponseBody  SchedulerBasicReturnValue replay(@PathVariable(value = "timestamp") @DateTimeFormat(pattern="yyyy-MM-dd hh:mm") Date timestamp) {

    schedulerService.processFrom(timestamp);
    return new SchedulerBasicReturnValue(HttpStatus.OK.value(), "Success.");
  }
}
