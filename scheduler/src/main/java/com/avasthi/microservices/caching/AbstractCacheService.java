package com.avasthi.microservices.caching;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class AbstractCacheService {

    private static final Logger logger = LogManager.getLogger(AbstractCacheService.class);

    @Value("${spring.profiles.active}")
    private String springProfilesActive;

    protected long getExpiry() {

        return this.getClass().getAnnotation(DefineCache.class).expiry();
    }
    protected String getPrefix() {
        return getClass().getAnnotation(DefineCache.class).prefix();
    }


    protected void storeObject(RedisTemplate<Object, Object> redisTemplate,
                               final Map<Object, Object> keyValuePairs) {

        if(keyValuePairs == null){
            return;
        }

        Map<Object, Object> prefixedKeyValuePairs = new HashMap<>();
        for (Map.Entry<Object, Object> e : keyValuePairs.entrySet()) {
            if (e.getKey() != null) {

                prefixedKeyValuePairs.put(new CacheKeyPrefix(getPrefix(), e.getKey()), e.getValue());
            }
        }

        redisTemplate.opsForValue().multiSet(prefixedKeyValuePairs);
        if (getExpiry() != SchedulerConstants.NEVER_EXPIRE) {

            for (Object key : prefixedKeyValuePairs.keySet()) {

                redisTemplate.expire(key, getExpiry(), TimeUnit.SECONDS);
            }
        }
    }


    protected <T> Set<T> getValuesFromHashMap(RedisTemplate<Object, Object> redisReadReplicaTemplate,
                                              RedisTemplate<Object, Object> redisTemplate,
                                              Object cacheKey,
                                              Class<T> type) {

        HashMap<Object, T> map = getHashMap(redisReadReplicaTemplate, redisTemplate, cacheKey, type);
        if(map == null){
            return null;
        }

        Set<T> hashSet = new HashSet<>();
        map.values().forEach(value -> hashSet.add((T) value));
        return hashSet;

    }

    protected <T> HashMap<Object, T> getHashMap(RedisTemplate<Object, Object> redisReadReplicaTemplate,
                                                RedisTemplate<Object, Object> redisTemplate,
                                                Object cacheKey,
                                                Class<T> type) {

        try {
            return getHashMap(redisReadReplicaTemplate, cacheKey, type);
        }
        catch(SerializationException serializationException) {
            deleteKey(redisTemplate, cacheKey);
            return null;
        }
        catch(Exception ex) {
            return getHashMap(redisTemplate, cacheKey, type);
        }
    }

    private <T> HashMap<Object, T> getHashMap(RedisTemplate<Object, Object> redisTemplate, Object cacheKey, Class<T> type) {
        HashMap<Object, Object> map = (HashMap<Object, Object>) redisTemplate.opsForHash().entries(cacheKey);
        if (map == null || map.isEmpty()){
            /**
             * We need to populate the map from database. So return null.
             */
            return null;
        }

        /**
         * Otherwise, populate map with values exception for key = 0.
         */
        HashMap<Object, T> resultMap = new HashMap<>();
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            if(!Objects.equals(entry.getKey(), 0)){
                resultMap.put(entry.getKey(), (T) entry.getValue());
            }
        }

        return resultMap;
    }

    protected <T> T getObjectFromHashMap(RedisTemplate<Object, Object> redisTemplate,
                                         Object cacheKey,
                                         Object hashKey,
                                         Class<T> type) {


            return  (T) redisTemplate.opsForHash().get(cacheKey, hashKey);
    }


    protected void storeObjectInHashMap(RedisTemplate<Object, Object> redisTemplate,
                                        final Object cacheKey,
                                        final Object hashKey,
                                        final Object hashValue) {

        if(hashKey == null || hashValue == null){
            return;
        }

        redisTemplate.opsForHash().put(cacheKey,hashKey, hashValue);

        if (getExpiry() != SchedulerConstants.NEVER_EXPIRE) {
            redisTemplate.expire(cacheKey, getExpiry(), TimeUnit.SECONDS);
        }
    }

    protected void deleteObjectFromHashMap(RedisTemplate<Object, Object> redisTemplate,
                                           final Object cacheKey,
                                           final Object hashKey) {

        if(hashKey == null){
            return;
        }

        redisTemplate.opsForHash().delete(cacheKey, hashKey);

        if(redisTemplate.opsForHash().entries(cacheKey).size() == 0){
            storeObjectInHashMap(redisTemplate, cacheKey, 0, null);
        }
    }


    protected void deleteObject(RedisTemplate<Object, Object> redisTemplate,
                                final List<Object> keys) {

        if(keys == null){
            return;
        }

        List<Object> nonNullKeys = new ArrayList<>();
        for (Object o : keys) {
            if (o != null) {
                nonNullKeys.add(new CacheKeyPrefix(getPrefix(), o));
            }
        }
        redisTemplate.delete(nonNullKeys);
    }

    protected void deleteKey(RedisTemplate<Object, Object> redisTemplate,
                             final Object key) {

        if(key == null){
            return;
        }

        List<Object> nonNullKeys = new ArrayList<>();
        if (key != null) {
            nonNullKeys.add(new CacheKeyPrefix(getPrefix(), key));
        }
        redisTemplate.delete(nonNullKeys);
    }
    protected <T> T get(RedisTemplate<Object, Object> redisTemplate,
                        Object key,
                        Class<T> type) {
        Object value;
        try {

            value = redisTemplate.opsForValue().get(new CacheKeyPrefix(getPrefix(), key));
        }
        catch(SerializationException serializationException) {
            deleteKey(redisTemplate, key);
            return null;
        }
        catch(Exception ex) {

            value = redisTemplate.opsForValue().get(new CacheKeyPrefix(getPrefix(), key));
        }
        return (T)value;
    }

    protected Boolean setIfAbsent(RedisTemplate<Object, Object> redisTemplate,
                                  Object key, Object Value) {

        Boolean status = redisTemplate.opsForValue().setIfAbsent(new CacheKeyPrefix(getPrefix(), key),Value);
        if (getExpiry() != SchedulerConstants.NEVER_EXPIRE) {

            redisTemplate.expire(new CacheKeyPrefix(getPrefix(), key), getExpiry(), TimeUnit.SECONDS);
        }
        return status;
    }

    protected Set<Object> keys(RedisTemplate<Object, Object> redisTemplate,
                               String pattern) {
        Set<byte[]> so = redisTemplate.getConnectionFactory().getConnection().keys(pattern.getBytes());
        List<CacheKeyPrefix> keyByteList = new ArrayList<>();
        so.stream().forEach(e -> keyByteList.add((CacheKeyPrefix) redisTemplate.getKeySerializer().deserialize(e)));
        Set<Object> rl = new HashSet<>();
        for (CacheKeyPrefix ckp : keyByteList) {
            rl.add(ckp.getKey());
        }
        return rl;
    }

    protected <K,V> void storeObjectInList(RedisTemplate<Object, Object> redisTemplate,
                                        final K cacheKey,
                                        final List<V> value) {
        if(cacheKey == null || value == null){
            return;
        }
        CacheKeyPrefix cacheKeyPrefix = new CacheKeyPrefix(getPrefix(), cacheKey);
        redisTemplate.opsForList().leftPushAll(cacheKeyPrefix,value);
        if (getExpiry() != SchedulerConstants.NEVER_EXPIRE) {
              redisTemplate.expire(cacheKeyPrefix, getExpiry(), TimeUnit.SECONDS);
        }
    }

    protected <K,V> void storeObjectInHyperLogLog(RedisTemplate<Object, Object> redisTemplate,
                                           final K cacheKey,
                                           final Set<V> values) {
        if(cacheKey == null || values == null){
            return;
        }
        CacheKeyPrefix cacheKeyPrefix = new CacheKeyPrefix(getPrefix(), cacheKey);
        redisTemplate.opsForHyperLogLog().add(cacheKeyPrefix,values);
        if (getExpiry() != SchedulerConstants.NEVER_EXPIRE) {
            redisTemplate.expire(cacheKeyPrefix, getExpiry(), TimeUnit.SECONDS);
        }
    }

    protected <K> Long getCountFromHyperLogLog(RedisTemplate<Object, Object> redisTemplate,
                                                  final K cacheKey
                                                  ) {
        if(cacheKey == null ){
            return null;
        }
        CacheKeyPrefix cacheKeyPrefix = new CacheKeyPrefix(getPrefix(), cacheKey);
        return redisTemplate.opsForHyperLogLog().size(cacheKeyPrefix);
    }

    protected <K,V> Long unionKeysFromHyperLogLog(RedisTemplate<Object, Object> redisTemplate,
                                               final K destinationKey,
                                               final List<V> sourceKeys
    ) {
        if(destinationKey == null || sourceKeys==null || sourceKeys.isEmpty()){
            return null;
        }
        CacheKeyPrefix cacheKeyPrefix = new CacheKeyPrefix(getPrefix(), destinationKey);
        List<CacheKeyPrefix> sourceKeysWithPrefix = new ArrayList<>(sourceKeys.size());
        sourceKeys.forEach(v -> {
            CacheKeyPrefix keyPrefix = new CacheKeyPrefix(getPrefix(), v);
            sourceKeysWithPrefix.add(keyPrefix);
        });
        long result =redisTemplate.opsForHyperLogLog().union(cacheKeyPrefix,sourceKeysWithPrefix);
        return result;
    }

    protected <K> void incrementStringValue(RedisTemplate<Object, Object> redisTemplate,
                                                  final K cacheKey,
                                                  final long delta
                                            ) {
        if(cacheKey == null){
            return;
        }
        CacheKeyPrefix cacheKeyPrefix = new CacheKeyPrefix(getPrefix(), cacheKey);
        redisTemplate.opsForValue().increment(cacheKeyPrefix,delta);
        if (getExpiry() != SchedulerConstants.NEVER_EXPIRE) {
            redisTemplate.expire(cacheKeyPrefix, getExpiry(), TimeUnit.SECONDS);
        }
    }

    protected <K, V> V addToList(RedisTemplate<Object, Object> redisTemplate,
                                 final K cacheKey,
                                 final V cacheValue) {
        if (cacheKey == null) {
            return null;
        }
        CacheKeyPrefix cacheKeyPrefix = new CacheKeyPrefix(getPrefix(), cacheKey);
        redisTemplate.opsForList().rightPush(cacheKey, cacheValue);
        return cacheValue;
    }
    /*
     * Not supported in REDIS2.8
     * Supported in Redis3.2
     */
    protected <K,V> void storeObjectInGeoHash(RedisTemplate<Object, Object> redisTemplate,
                                                  final K cacheKey,
                                                  final Set<V> value) {
        if(cacheKey == null || value == null){
            return;
        }
        CacheKeyPrefix cacheKeyPrefix = new CacheKeyPrefix(getPrefix(), cacheKey);
        redisTemplate.opsForHyperLogLog().add(cacheKeyPrefix,value);
        if (getExpiry() != SchedulerConstants.NEVER_EXPIRE) {
            redisTemplate.expire(cacheKeyPrefix, getExpiry(), TimeUnit.SECONDS);
        }
    }

}
