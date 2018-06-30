
package com.avasthi.microservices.annotations;

import com.avasthi.microservices.exceptions.SchedulerResponseCode;

import java.lang.annotation.*;

/**
 * Created by maheshsapre on 06/07/17.
 */
@Documented
@Target(ElementType.TYPE)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceResponse {
    SchedulerResponseCode defaultCode() default SchedulerResponseCode.GENERAL_UNIDENTIFIED;
}
