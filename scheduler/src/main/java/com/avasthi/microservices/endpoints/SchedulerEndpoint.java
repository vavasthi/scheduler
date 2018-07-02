
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
import com.avasthi.microservices.pojos.ScheduledItem;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping(SchedulerConstants.VERSION_1 + SchedulerConstants.SCHEDULER_ENDPOINT)
@Api(value = SchedulerConstants.SCHEDULER_ENDPOINT_NAME,
        description = "This endpoint provides interface for adding and removing a scheduled item.")
public class SchedulerEndpoint {

  @Autowired
  private SchedulerCacheService schedulerCacheService;


  @ApiOperation(value = "Create a new scheduled item.",
          notes = "This interface allows to add a new scheduled item with its target defined.")
  @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public @ResponseBody ScheduledItem create(@RequestBody ScheduledItem scheduledItem) {

    return schedulerCacheService.scheduleItem(scheduledItem);
  }

  @ApiOperation(value = "Get an already existing scheduledItem based on its UUID",
          notes = "This interface allows retrieve all the details of a scheduled item.")
  @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public @ResponseBody ScheduledItem create(@PathVariable(value = "id") UUID id) {

    return schedulerCacheService.getItem(id);
  }
  @ApiOperation(value = "Delete an existing scheduled item given its UUID",
          notes = "This interface allows deletion of an existing scheduled item from its UUID.")
  @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
  public @ResponseBody ScheduledItem delete(@PathVariable(value = "id") UUID id) {

    ScheduledItem scheduledItem = schedulerCacheService.remove(id);
    if (scheduledItem == null) {
      throw new NotFoundException(String.format("%s not found", id.toString()));
    }
    return scheduledItem;
  }
}
