/*
 * Copyright 2011-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hibernate.cache.redis.regions;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.redis.jedis.JedisClient;
import org.hibernate.cache.redis.strategy.RedisAccessStrategyFactory;
import org.hibernate.cache.spi.CacheDataDescription;
import org.hibernate.cache.spi.TransactionalDataRegion;
import org.hibernate.cfg.Settings;

import java.util.Properties;

/**
 * RedisTransactionalDataRegion
 *
 * @author sunghyouk.bae@gmail.com
 * @since 13. 4. 5. 오후 9:44
 */
@Slf4j
public class RedisTransactionalDataRegion extends RedisDataRegion implements TransactionalDataRegion {

    /**
     * Hibernate settings associated with the persistence unit.
     */
    protected final Settings settings;
    /**
     * Metadata associated with the objects sorted in the region
     */
    protected final CacheDataDescription metadata;

    public RedisTransactionalDataRegion(RedisAccessStrategyFactory accessStrategyFactory,
                                        JedisClient redis,
                                        String regionName,
                                        Settings settings,
                                        CacheDataDescription metadata,
                                        Properties props) {
        super(accessStrategyFactory, redis, regionName, props);

        this.settings = settings;
        this.metadata = metadata;
    }

    public Settings getSettings() {
        return settings;
    }

    @Override
    public boolean isTransactionAware() {
        return false;
    }

    @Override
    public CacheDataDescription getCacheDataDescription() {
        return metadata;
    }

    public Object get(Object key) {
        log.trace("get cache item... key=[{}], expiration=[{}] sec", key, getExpireInSeconds());
        try {
            Object value = redis.get(getName(), key, getExpireInSeconds());
            log.debug("retrieve cache item... key=[{}], value=[{}]", key, value);
            return value;
        } catch (Exception e) {
            log.warn("Fail to get cache item... key=" + key, e);
            return null;
        }
    }


    public void put(Object key, Object value) {
        log.trace("put cache item... key=[{}], value=[{}], expire=[{}] sec", key, value, getExpireInSeconds());
        try {
            redis.set(getName(), key, value, getExpireInSeconds());
        } catch (Exception e) {
            log.warn("Fail to put cache item... key=" + key, e);
        }
    }

    public void remove(Object key) throws CacheException {
        log.trace("remove cache item... key=[{}]", key);
        try {
            redis.del(getName(), key);
        } catch (Exception e) {
            log.warn("Fail to remove cache item... key=" + key, e);
        }
    }


    public void clear() {
        log.trace("clear cache item... region=[{}]", getName());
        try {
            redis.deleteRegion(getName());
        } catch (Exception e) {
            log.warn("Fail to clear region... name=" + getName(), e);
        }
    }

//    public void writeLock(Object key) {
//        // nothing to do.
//    }
//
//    public void writeUnlock(Object key) {
//        // nothing to do.
//    }
//
//    public void readLock(Object key) {
//        // nothing to do.
//    }
//
//    public void readUnlock(Object key) {
//        // nothing to do.
//    }

    /**
     * Returns <code>true</code> if the locks used by the locking methods of this region are the independent of the cache.
     * <p/>
     * Independent locks are not locked by the cache when the cache is accessed directly.  This means that for an independent lock
     * lock holds taken through a region method will not block direct access to the cache via other means.
     */
//    public final boolean locksAreIndependentOfCache() {
//        return false;
//    }
}
