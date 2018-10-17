/*
 * Copyright (c) 2018 Author vinayavasthi
 *
 * This software is a property of Tesco PLC
 */

package com.tesco.utilities.scheduler.utils;


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

import java.util.Map;
import java.util.Properties;

public class SchedulerKafkaProducer {

    public static SchedulerKafkaProducer INSTANCE = new SchedulerKafkaProducer();

    private SchedulerKafkaProducer() {

    }

    /**
     * This method will return a producer that connects to the hosts as defined in
     * parameter. The producers are cached and reused.
     *
     * @param hosts comma delimited list of hosts in format host:port
     * @param retryCount producer retry count
     * @return
     */
    public Producer<String, String> getProducer(String hosts, int retryCount, Map<String, String> props) {

        Properties properties = new Properties();
        properties.put("bootstrap.servers", hosts);
        properties.put("acks", "all");
        properties.put("retries", retryCount);
        properties.put("buffer.memory", 33554432);
        properties.put("key.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        for (Map.Entry<String, String> e : props.entrySet()) {
            properties.put(e.getKey(), e.getValue());
        }
        return  new KafkaProducer<String, String>(properties);
    }
}
