/*
 * Copyright (c) 2018 Author vinayavasthi
 *
 * This software is a property of Tesco PLC
 */

package com.tesco.utilities.scheduler.caching;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Arrays;


@Configuration
public class SchedulerRedisConfiguration {

  private
  @Value("${redis.scheduler.nodes:localhost}")
  String redisHost;
  @Value("${redis.scheduler.database:1}")
  private int redisDatabase;
  private
  @Value("${redis.scheduler.password:null}")
  String redisPassword;
  @Value("${redis.scheduler.pool.maxIdle:5}")
  private int maxIdle;
  private
  @Value("${redis.scheduler.pool.minIdle:1}")
  int minIdle;
  private
  @Value("${redis.scheduler.pool.maxRedirects:3}")
  int maxRedirects;
  private
  @Value("${redis.scheduler.pool.maxTotal:20}")
  int maxTotal;
  private
  @Value("${redis.scheduler.pool.maxWaitMillis:3000}")
  int maxWaitMillis;

  JedisConnectionFactory jedisSchedulerConnectionFactory() {

    String[] hosts = redisHost.split(",");
    JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setMaxIdle(maxIdle);
    poolConfig.setMinIdle(minIdle);
    poolConfig.setMaxWaitMillis(maxWaitMillis);
    poolConfig.setMaxTotal(maxTotal);

    if (hosts.length == 1) {
      return new JedisConnectionFactory(poolConfig);
    }
    else {

      RedisClusterConfiguration configuration = new RedisClusterConfiguration(Arrays.asList(hosts));
      if (redisPassword != null || !redisPassword.isEmpty()) {
        configuration.setPassword(RedisPassword.of(redisPassword));
      }
      configuration.setMaxRedirects(maxRedirects);
      JedisConnectionFactory factory = new JedisConnectionFactory(configuration, poolConfig);
      return factory;
    }
  }

  public RedisTemplate<Object, Object> redisTemplate() {
    RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<Object, Object>();
    redisTemplate.setConnectionFactory(jedisSchedulerConnectionFactory());
    redisTemplate.setKeySerializer(new JdkSerializationRedisSerializer());
    redisTemplate.setValueSerializer(new JdkSerializationRedisSerializer());
    redisTemplate.setEnableTransactionSupport(true);
    redisTemplate.afterPropertiesSet();
    return redisTemplate;
  }

}
