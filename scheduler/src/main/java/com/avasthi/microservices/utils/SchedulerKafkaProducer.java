package com.avasthi.microservices.utils;


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SchedulerKafkaProducer {

    private Map<String, Producer<String, String>> producerMap = new HashMap<>();
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
    public Producer<String, String> getProducer(String hosts, int retryCount) {

        Producer<String, String> producer = producerMap.get(hosts);
        if (producer != null) {
            return producer;
        }
        else {

            Properties properties = new Properties();
            properties.put("bootstrap.servers", hosts);
            properties.put("acks", "all");
            properties.put("retries", retryCount);
            properties.put("buffer.memory", 33554432);
            properties.put("key.serializer",
                    "org.apache.kafka.common.serialization.StringSerializer");
            properties.put("value.serializer",
                    "org.apache.kafka.common.serialization.StringSerializer");
            producer = new KafkaProducer<String, String>(properties);
            producerMap.put(hosts, producer);
            return producer;
        }
    }
}
