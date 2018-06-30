package com.avasthi.microservices.enums;

public enum SchedulerModule {
    GENERAL(0),
    USERS(1);

    final int code;
    SchedulerModule(int code){ this.code = code; }

    /**
     * Gets code.
     *
     * @return the code
     */
    public String getCode(){
       return String.format("%03d", code);
    }


}
