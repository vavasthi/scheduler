/*
 * Copyright (c) 2018 Author vinayavasthi
 *
 * This software is a property of Tesco PLC
 */

package com.tesco.utilities.scheduler.exceptions;
import com.tesco.utilities.scheduler.enums.SchedulerModule;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;


public enum SchedulerResponseCode {
    /**
     * GENERAL module
     */
    GENERAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 999, SchedulerModule.GENERAL, "Internal server Error. Unhandled exception case. Contact server team to resolve this. Retrying with the same input data may not really help, unless you expect some internal state to change between retries."),
    GENERAL_UNIDENTIFIED(HttpStatus.INTERNAL_SERVER_ERROR, 998, SchedulerModule.GENERAL, ""),
    GENERAL_SUCCESS(HttpStatus.OK, 0, SchedulerModule.GENERAL, "Success! The request is processed successfully." ),
    GENERAL_BAD_REQUEST(HttpStatus.BAD_REQUEST, 1, SchedulerModule.GENERAL, "General Bad ExceptionRequest. This is a common error when client sends invalid inputs. Check the swagger for required parameters, their types etc. Retrying with the same input data will not help."),

    GENERAL_UNPROCESSABLE_ENTITY(HttpStatus.UNPROCESSABLE_ENTITY, 2, SchedulerModule.GENERAL, "The request was well formed, but unable to be followed due to semantic errors."),
    GENERAL_TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, 3, SchedulerModule.GENERAL, "The client has sent too many requests in a given amount of time. The request should be retried after allowing enough time gap between two requests."),
    GENERAL_FAILED_DEPENDENCY(HttpStatus.FAILED_DEPENDENCY, 4, SchedulerModule.GENERAL, "The request failed due to failure of a previous request, could be with another component or service."),

    GENERAL_FILE_ALREADY_EXISTS(HttpStatus.FAILED_DEPENDENCY, 5, SchedulerModule.GENERAL, "File already exists with same name. Retry by changing the file name."),
    GENERAL_FILE_DOES_NOT_EXIST(HttpStatus.NOT_FOUND, 6, SchedulerModule.GENERAL, "File does not exist."),
    GENERAL_FILE_HAS_INVALID_FORMAT(HttpStatus.BAD_REQUEST, 7, SchedulerModule.GENERAL, "File has invalid format"),

    GENERAL_EXTERNAL_SERVICE_TIMED_OUT(HttpStatus.FAILED_DEPENDENCY, 8, SchedulerModule.GENERAL, "A dependent service or component did not respond within expected time. A retry will only help if you expect the dependency would resolve over time."),
    GENERAL_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, 9, SchedulerModule.GENERAL, ""),
    GENERAL_FORBIDDEN(HttpStatus.FORBIDDEN, 10, SchedulerModule.GENERAL, ""),

    GENERAL_CONSTRAINT_VIOLATED(HttpStatus.UNPROCESSABLE_ENTITY, 11, SchedulerModule.GENERAL, "General validation constraints failed. This may happen because the client provided invalid inputs in the request. The client should review and fix the request before retrying."),
    GENERAL_DATABASE_CONSTRAINT_VIOLATED(HttpStatus.UNPROCESSABLE_ENTITY, 12, SchedulerModule.GENERAL, "Database validation constraints failed. This may happen because the client provided invalid inputs in the request. The client should review and fix the request before retrying."),

    GENERAL_CONFLICT(HttpStatus.CONFLICT, 13, SchedulerModule.GENERAL, ""),
    GENERAL_NOT_FOUND(HttpStatus.NOT_FOUND, 14, SchedulerModule.GENERAL, ""),
    GENERAL_DATA_TYPE_CONFIGURATION_ERROR(HttpStatus.UNPROCESSABLE_ENTITY, 15, SchedulerModule.GENERAL, ""),
    GENERAL_INFRASTRUCTURE_DEPENDENCY_FAILURE(HttpStatus.UNPROCESSABLE_ENTITY, 16, SchedulerModule.GENERAL, ""),

    GENERAL_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, 18, SchedulerModule.GENERAL, ""),
    GENERAL_UPGRADE_REQUIRED(HttpStatus.UPGRADE_REQUIRED, 19, SchedulerModule.GENERAL, ""),

    /**
     * USER module
     */
    USER_NOT_FOUND_BY_ID(HttpStatus.NOT_FOUND, 0, SchedulerModule.USERS, "Invalid user auth token."),
    USER_AUTH_TOKEN_IS_INVALID_OR_EXPIRED(HttpStatus.UNAUTHORIZED, 1, SchedulerModule.USERS, ""),
    USER_NOT_FOUND_BY_NAME(HttpStatus.NOT_FOUND, 2, SchedulerModule.USERS, ""),
    USER_BLOCKED_AUTH_TOKEN(HttpStatus.FORBIDDEN, 3, SchedulerModule.USERS, ""),
    USER_INVALID_OR_EXPIRED_AUTH_TOKEN(HttpStatus.UNAUTHORIZED, 4, SchedulerModule.USERS, ""),
    USER_INVALID_FORMAT_EMAIL(HttpStatus.BAD_REQUEST, 5, SchedulerModule.USERS, ""),
    USER_NOT_FOUND_BY_RESET_PASSWORD_TOKEN(HttpStatus.UNAUTHORIZED, 6, SchedulerModule.USERS, ""),
    USER_EXPIRED_RESET_PASSWORD_TOKEN(HttpStatus.UNAUTHORIZED, 7, SchedulerModule.USERS, ""),
    USER_NOT_FOUND_BY_EMAIL(HttpStatus.NOT_FOUND, 8, SchedulerModule.USERS, "User email not found."),
    USER_MISSING_LOGIN_PASSWORD(HttpStatus.UNAUTHORIZED, 9, SchedulerModule.USERS, "Username password are missing"),
    USER_NOT_FOUND_BY_LOGIN(HttpStatus.NOT_FOUND, 10, SchedulerModule.USERS, "Did not find user by username or password"),
    USER_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, 11, SchedulerModule.USERS, "User authentication failed"),
    USER_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, 12, SchedulerModule.USERS, "User authentication failed"),
    USER_INVALID_UPLOAD_TOKEN(HttpStatus.UNAUTHORIZED, 13, SchedulerModule.USERS, "User authentication failed"),
    USER_NO_ACTIVE_DEVICES(HttpStatus.NOT_FOUND, 14, SchedulerModule.USERS, "User has no active devices. "),
    USER_INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, 15, SchedulerModule.USERS, "ExceptionRequest must contain X-Refresh-Token and X-Auth-Token-Type"),
    USER_REGISTRATION_ID_NOT_FOUND(HttpStatus.NOT_FOUND, 16, SchedulerModule.USERS, "Invalid user registration id, or it may have been expired."),
    ;

    final int id;
    final SchedulerModule module;
    final String message;
    final HttpStatus defaultHttpStatus;

    SchedulerResponseCode(HttpStatus defaultHttpStatus, int id, SchedulerModule module, String message){
        this.module = module;
        this.id = id;
        this.message = message;
        this.defaultHttpStatus = defaultHttpStatus;
    }

    public static SchedulerResponseCode create(String code) {
        for (SchedulerResponseCode m : values()) {
            if (m.getCode().equalsIgnoreCase(code) ) {
                return m;
            }
        }
        throw new IllegalArgumentException(String.format("%s is not a valid response code", code));
    }

    public int getId() {return id;}
    public SchedulerModule getModule() {
        return module;
    }
    public String getMessage() {
        return StringUtils.isBlank(message) ? this.toString() : message;
    }
    public HttpStatus getDefaultHttpStatus() {
        return defaultHttpStatus;
    }
    public String getCode(){
        return String.format("%s%03d", module.getCode(), id);
    }

    @Override
    public String toString() {
        return name();
    }

    public String toCsvString() {
        return String.format("'%s,'%s,'%s,'%s,'%d,'%s,'%s",
                this.module.getCode(),
                this.module.name(),
                this.getCode(),
                this.name(),
                this.getDefaultHttpStatus().value(),
                this.getDefaultHttpStatus().name(),
                this.getMessage());
    }
}
