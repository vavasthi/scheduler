
/*
 * Copyright (c) 2018 Author vinayavasthi
 *
 * This software is a property of Tesco PLC
 */

package com.tesco.utilities.scheduler.annotations;

import com.tesco.utilities.scheduler.exceptions.SchedulerResponseCode;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceResponse {
    SchedulerResponseCode defaultCode() default SchedulerResponseCode.GENERAL_UNIDENTIFIED;
}
