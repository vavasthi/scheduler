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

package com.avasthi.microservices.validation;

import com.avasthi.microservices.pojos.ScheduledItem;

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
