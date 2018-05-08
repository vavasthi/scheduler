package com.avasthi.microservices.caching;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
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
  @Value("${redis.scheduler.password}")
  String redisPassword;
  @Value("${redis.scheduler.pool.maxIdle:20}")
  private int maxIdle;
  private
  @Value("${redis.scheduler.pool.minIdle:5}")
  int minIdle;
  private
  @Value("${redis.scheduler.pool.maxRedirects:3}")
  int maxRedirects;
  private
  @Value("${redis.scheduler.pool.maxTotal:2000}")
  int maxTotal;
  private
  @Value("${redis.scheduler.pool.maxWaitMillis:30000}")
  int maxWaitMillis;

  JedisConnectionFactory jedisSchedulerConnectionFactory() {

    RedisClusterConfiguration configuration = new RedisClusterConfiguration(Arrays.asList(redisHost.split(",")));
    if (redisPassword != null || !redisPassword.isEmpty()) {
      configuration.setPassword(RedisPassword.of(redisPassword));
    }
    configuration.setMaxRedirects(maxRedirects);
    JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setMaxIdle(maxIdle);
    poolConfig.setMinIdle(minIdle);
    poolConfig.setMaxWaitMillis(maxWaitMillis);
    poolConfig.setMaxTotal(maxTotal);
    JedisConnectionFactory factory = new JedisConnectionFactory(configuration, poolConfig);
    return factory;
  }

  public RedisTemplate<Object, Object> redisTemplate() {
    RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<Object, Object>();
    redisTemplate.setConnectionFactory(jedisSchedulerConnectionFactory());
    return redisTemplate;
  }

}
