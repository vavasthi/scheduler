package com.avasthi.microservices.caching;

import io.lettuce.core.ReadFrom;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;

import java.util.Arrays;


@Log4j2
@Configuration
public class SchedulerRedisConfiguration {
  private
  @Value("${redis.scheduler.hosts:localhost}")
  String[] redisHosts;
  private
  @Value("${redis.scheduler.port:6379}")
  int redisPort;
  @Value("${redis.scheduler.database:1}")
  private int redisDatabase;
  @Value("${redis.scheduler.password:}")
  private String redisPassword;
  @Value("${redis.scheduler.pool.maxIdle:5}")
  private int maxIdle;
  @Value("${redis.scheduler.pool.minIdle:1}")
  private int minIdle;
  @Value("${redis.scheduler.pool.maxRedirects:3}")
  private int maxRedirects;
  @Value("${redis.scheduler.pool.maxTotal:20}")
  private int maxTotal;
  private
  @Value("${redis.scheduler.pool.maxWaitMillis:3000}")
  int maxWaitMillis;

  @Bean("schedulerRedisConnectionFactory")
  public LettuceConnectionFactory connectionFactory() {

    LettuceConnectionFactory connectionFactory = null;
    if (redisHosts.length > 1) {
      RedisClusterConfiguration clusterConfiguration
              = new RedisClusterConfiguration(Arrays.asList(redisHosts));
      if (redisPassword != null && !redisPassword.isEmpty()) {

        clusterConfiguration.setPassword(RedisPassword.of(redisPassword));
      }
      LettuceClientConfiguration clientConfiguration = LettuceClientConfiguration.builder()
              .readFrom(ReadFrom.REPLICA_PREFERRED)
              .build();
      connectionFactory = new LettuceConnectionFactory(clusterConfiguration, clientConfiguration);
    }
    else {

      String[] pair = redisHosts[0].split(":");
      String host = pair[0];
      int port = 6379;
      try {

        if (pair.length > 1) {

          port = Integer.parseInt(pair[1]);
        }
        else {
          port = redisPort;
        }
      }
      catch(Exception e) {
        log.error("Port number is numeric", e);
      }
      RedisStandaloneConfiguration redisStandaloneConfiguration
              = new RedisStandaloneConfiguration(host, port);
      redisStandaloneConfiguration.setDatabase(redisDatabase);
      if (redisPassword != null && !redisPassword.isEmpty()) {

        redisStandaloneConfiguration.setPassword(RedisPassword.of(redisPassword));
      }
      LettuceClientConfiguration clientConfiguration = LettuceClientConfiguration.builder()
              .readFrom(ReadFrom.REPLICA_PREFERRED)
              .build();
      connectionFactory = new LettuceConnectionFactory(redisStandaloneConfiguration, clientConfiguration);
    }
    connectionFactory.setDatabase(redisDatabase);
    return connectionFactory;
  }

  public RedisTemplate<Object, Object> redisTemplate() {
    RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<Object, Object>();
    redisTemplate.setConnectionFactory(connectionFactory());
    redisTemplate.setKeySerializer(new JdkSerializationRedisSerializer());
    redisTemplate.setValueSerializer(new JdkSerializationRedisSerializer());
    redisTemplate.setEnableTransactionSupport(true);
    redisTemplate.afterPropertiesSet();
    return redisTemplate;
  }

}
