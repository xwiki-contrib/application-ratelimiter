/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.xwiki.contrib.ratelimiter.internal;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.cache.Cache;
import org.xwiki.cache.CacheManager;
import org.xwiki.cache.config.CacheConfiguration;
import org.xwiki.cache.eviction.LRUEvictionConfiguration;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.contrib.ratelimiter.RateLimiter;

/**
 * Default implementation of {@link RateLimiterCache}.
 *
 * @version $Id$
 */
@Component
@Singleton
public class DefaultRateLimiterCache implements RateLimiterCache, Initializable
{
    /** Default capacity for security cache. */
    private static final int DEFAULT_CAPACITY = 500;

    /** Cache manager to create the cache. */
    @Inject
    private CacheManager cacheManager;

    /** The cache instance. */
    private Cache<RateLimiter> cache;

    private Cache<RateLimiter> newCache() throws InitializationException
    {
        CacheConfiguration cacheConfig = new CacheConfiguration();
        cacheConfig.setConfigurationId("org.xwiki.contrib.ratelimiter.cache");
        LRUEvictionConfiguration lru = new LRUEvictionConfiguration();
        lru.setMaxEntries(DEFAULT_CAPACITY);
        cacheConfig.put(LRUEvictionConfiguration.CONFIGURATIONID, lru);
        try {
            return cacheManager.createNewCache(cacheConfig);
        } catch (Exception e) {
            throw new InitializationException(
                String.format("Unable to create the security cache with a capacity of [%d] entries",
                    lru.getMaxEntries()), e);
        }
    }

    private String getKey(Object consumer, Object consumed)
    {
        return Integer.toString(
            ((consumer == null) ? 0 : 37 * consumer.hashCode()) + ((consumed == null) ? 0 : consumed.hashCode()));
    }

    @Override
    public void initialize() throws InitializationException
    {
        cache = newCache();
    }

    @Override
    public RateLimiter get(Object consumer, Object consumed)
    {
        return cache.get(getKey(consumer, consumed));
    }

    @Override
    public void add(Object consumer, Object consumed, RateLimiter limiter)
    {
        cache.set(getKey(consumer, consumed), limiter);
    }

    @Override
    public void clear()
    {
        cache.removeAll();
    }
}
