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

import java.util.concurrent.TimeUnit;

public class SchedulerConstants {

  public static final int S3_DELETE_RULE_BUFFER_DAYS = 1;

  public static final long HOURS = 60 * 60 * 1000;
  /**
   * The constant SIX_HOURS.
   */
  public static final long SIX_HOURS = 6 * 60 * 60;
  /**
   * The constant SIX_DAYS.
   */
  public static final long SIX_DAYS = 6 * 24 * 60 * 60;
  /**
   * The constant SIX_MONTH.
   */
  public static final long SIX_MONTH = 6 * 30 * (24 * 60 * 60);
  /**
   * The constant ONE_MONTH.
   */
  public static final int ONE_MONTH = 30 * (24 * 60 * 60);
  /**
   * The constant HALF_HOUR.
   */
  public static final long HALF_HOUR = 30 * 60;
  /**
   * The constant ONE_HOUR.
   */
  public static final long ONE_HOUR = 60 * 60;
  /**
   * The constant FIVE_MINUTES.
   */
  public static final long FIVE_MINUTES = 5 * 60;
  /**
   * The constant FIFTEEN_MINUTES.
   */
  public static final long FIFTEEN_MINUTES = 5 * 60;
  /**
   * The constant FIFTY_FIVE_MINUTES.
   */
  public static final long FIFTY_FIVE_MINUTES = 55 * 60;
  /**
   * The constant HTTP_RETRY_COUNT.
   */
  public static final int HTTP_RETRY_COUNT = 5;
  /**
   * The constant HTTP_TIMEOUT.
   */
  public static final int HTTP_TIMEOUT = 12;


  /**
   * The constant DEVICE_UPLOAD_TOKEN_EXPIRES_IN_DAYS.
   */
  public static final int DEVICE_UPLOAD_TOKEN_EXPIRES_IN_DAYS = 180; // 6 MONTHS

  /**
   * The constant FREE_TRIAL_EXPIRY_REMINDER_DAYS.
   */
  public static final int FREE_TRIAL_EXPIRY_REMINDER_DAYS = 3;

  /**
   * The constant DEFAULT_EVENT_EXPIRY_DURATION.
   */
  public static final int DEFAULT_EVENT_EXPIRY_DURATION = 259200;// 3 days
  /**
   * The constant HT1_EVENT_EXPIRY_DURATION.
   */
  public static final int HT1_EVENT_EXPIRY_DURATION = 345600;// 4 days
  /**
   * The constant HT2_EVENT_EXPIRY_DURATION.
   */
  public static final int HT2_EVENT_EXPIRY_DURATION = 864000;// 10 days
  /**
   * The constant HT3_EVENT_EXPIRY_DURATION.
   */
  public static final int HT3_EVENT_EXPIRY_DURATION = 2937600;// 34 days
  /**
   * The constant DEFAULT_TOKEN_EXPIRY.
   */
  public static final int DEFAULT_TOKEN_EXPIRY = 7 * 24 * 60 * 60;
  /**
   * The constant UNLIMITED_EVENT_EXPIRY_DURATION.
   */
  public static final int BEURER_EVENT_EXPIRY_DURATION = 86400000; //1000 days
  /**
   * The constant TEN_SEC.
   */
  public static final long TEN_SEC = 10;
  /**
   * The constant USER_UPLOAD_TOKEN_MIN_EXPIRE_TIME_SECONDS.
   */
  public static int USER_UPLOAD_TOKEN_MIN_EXPIRE_TIME_SECONDS = (2 * 60 * 60);
  /**
   * The constant RESET_PASSWORD_TOKEN_EXPIRE_TIME_SECONDS.
   */
  public static int RESET_PASSWORD_TOKEN_EXPIRE_TIME_SECONDS = (30 * 60);
  /**
   * The constant NEVER_EXPIRE.
   */
  public static final long NEVER_EXPIRE = -1;
  public static final String SCHEDULER_CACHE_NAME = "SCHEDULER_CACHE";
  public static final String SCHEDULER_CACHE_PEFIX = "SCHEDULER:";
  public static final String SCHEDULER_ITEM_BEING_PROCESSED_CACHE_NAME = "SCHEDULER_IBP_CACHE";
  public static final String SCHEDULER_ITEM_BEING_PROCESSED_CACHE_PEFIX = "SCHEDULER_IBP:";
  public static final String SCHEDULER_ITEM_BEING_PROCESSED_KEY = "SCHEDULER_ITEM_BEING_PROCESSED_KEY:";
  public static final String SCHEDULER_ITEM_BEING_RECOVERED_KEY = "SCHEDULER_ITEM_BEING_RECOVERED_KEY:";
  public static final String DATE_KEY_FORMAT="yyyyMMddhhmm";

  public static final String MSG_SUCCESS = "Success!";

  public static final String VERSION_1 = "/v1";
  public static final String SCHEDULER_ENDPOINT_NAME = "scheduler";
  public static final String SCHEDULER_ENDPOINT = "/" + SCHEDULER_ENDPOINT_NAME;

  public static final TimeUnit THREAD_KEEPALIVE_TIMEUNIT = TimeUnit.MINUTES;

}
