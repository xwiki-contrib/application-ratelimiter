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
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;

/**
 * Default implementation of {@link RateLimiterCache}.
 *
 * @version $Id$
 */
@Component
@Singleton
public class DefaultRateLimiterCache implements RateLimiterCache, Initializable
{
    /** Separator used for composing key for the cache. */
    private static final String KEY_CACHE_SEPARATOR = "@@";

    /** Default capacity for security cache. */
    private static final int DEFAULT_CAPACITY = 500;

    /** Cache manager to create the cache. */
    @Inject
    private CacheManager cacheManager;

    /** The keys in the cache are generated from instances of {@link org.xwiki.model.reference.EntityReference}. */
    @Inject
    private EntityReferenceSerializer<String> keySerializer;

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

    private String getKey(EntityReference consumer, EntityReference consumed)
    {
        return keySerializer.serialize(consumer) + KEY_CACHE_SEPARATOR + keySerializer.serialize(consumed);
    }

    @Override
    public void initialize() throws InitializationException
    {
        cache = newCache();
    }

    @Override
    public RateLimiter get(EntityReference consumer, EntityReference consumed)
    {
        return cache.get(getKey(consumer, consumed));
    }

    @Override
    public void add(EntityReference consumer, EntityReference consumed, RateLimiter limiter)
    {
        cache.set(getKey(consumer, consumed), limiter);
    }
}
