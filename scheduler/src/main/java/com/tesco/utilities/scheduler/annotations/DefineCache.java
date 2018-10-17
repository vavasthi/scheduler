/*
 * Copyright (c) 2018 Author vinayavasthi
 *
 * This software is a property of Tesco PLC
 */

package com.tesco.utilities.scheduler.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by vinay on 2/22/16.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE) //can use in class only.
public @interface DefineCache {
  String name();
  String prefix();
  long expiry();
}