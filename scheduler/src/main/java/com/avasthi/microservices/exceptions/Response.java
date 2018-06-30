package com.avasthi.microservices.exceptions;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.util.UUID;

/**
 * The type Response.
 */
public class Response {
    private String code;
    private SchedulerResponseCode codeName;
    private int status;
    private HttpStatus statusName;

    public Response() {
    }

    public Response(String code, SchedulerResponseCode codeName, int status, HttpStatus statusName) {
        this.code = code;
        this.codeName = codeName;
        this.status = status;
        this.statusName = statusName;
    }

    /**
     * Gets code.
     *
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets code.
     *
     * @param code the code
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Gets code name.
     *
     * @return the code name
     */
    public SchedulerResponseCode getCodeName() {
        return codeName;
    }

    /**
     * Sets code name.
     *
     * @param codeName the code name
     */
    public void setCodeName(SchedulerResponseCode codeName) {
        this.codeName = codeName;
    }


    /**
     * Gets status name.
     *
     * @return the status name
     */
    public HttpStatus getStatusName() {
        return statusName;
    }

    /**
     * Sets status name.
     *
     * @param statusName the status name
     */
    public void setStatusName(HttpStatus statusName) {
        this.statusName = statusName;
    }

    /**
     * Gets status.
     *
     * @return the status
     */
    public int getStatus() {
        return status;
    }

    /**
     * Sets status.
     *
     * @param status the status
     */
    public void setStatus(int status) {
        this.status = status;
    }

}
