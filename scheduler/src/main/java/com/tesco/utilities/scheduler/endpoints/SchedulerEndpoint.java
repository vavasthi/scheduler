
/*
 * Copyright (c) 2018 Author vinayavasthi
 *
 * This software is a property of Tesco PLC
 */

package com.tesco.utilities.scheduler.endpoints;

import com.tesco.utilities.scheduler.caching.SchedulerCacheService;
import com.tesco.utilities.scheduler.caching.SchedulerConstants;
import com.tesco.utilities.scheduler.exceptions.NotFoundException;
import com.tesco.utilities.scheduler.pojos.ScheduledItem;
import com.tesco.utilities.scheduler.pojos.SchedulerBasicReturnValue;
import com.tesco.utilities.scheduler.scheduler.SchedulerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.UUID;


@RestController
@RequestMapping(SchedulerConstants.VERSION_1 + SchedulerConstants.SCHEDULER_ENDPOINT)
@Api(value = SchedulerConstants.SCHEDULER_ENDPOINT_NAME,
        description = "This endpoint provides interface for adding and removing a scheduled item.")
public class SchedulerEndpoint {

  @Autowired
  private SchedulerService schedulerService;
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
  @ApiOperation(value = "Replay set of messages starting from a timestamp.",
          notes = "This interface allows replay of scheduledItems from a given time.")
  @RequestMapping(value = "/replay/{timestamp}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public @ResponseBody  SchedulerBasicReturnValue replay(@PathVariable(value = "timestamp") @DateTimeFormat(pattern="yyyy-MM-dd hh:mm") Date timestamp) {

    schedulerService.processFrom(timestamp);
    return new SchedulerBasicReturnValue(HttpStatus.OK.value(), "Success.");
  }
}
