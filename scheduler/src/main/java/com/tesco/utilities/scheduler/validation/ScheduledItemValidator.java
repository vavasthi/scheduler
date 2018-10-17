/*
 * Copyright (c) 2018 Author vinayavasthi
 *
 * This software is a property of Tesco PLC
 */

package com.tesco.utilities.scheduler.validation;

import com.tesco.utilities.scheduler.pojos.ScheduledItem;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ScheduledItemValidator implements ConstraintValidator<ValidScheduledItem, ScheduledItem> {
  @Override
  public boolean isValid(ScheduledItem scheduledItem, ConstraintValidatorContext constraintValidatorContext) {

    if (scheduledItem.getTimestamp() != null || scheduledItem.getRepeatSpecification() != null) {
      return true;
    }
    return false;
  }
}
