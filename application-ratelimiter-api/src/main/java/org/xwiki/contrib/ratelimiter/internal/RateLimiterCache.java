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

import org.xwiki.component.annotation.Role;
import org.xwiki.contrib.ratelimiter.RateLimiter;

/**
 * A cache for storing rate limiters per entities (ie: users).
 *
 * @version $Id$
 */
@Role
public interface RateLimiterCache
{
    /**
     * Get the rate limiter for the given entity.
     *
     * @param consumer the entity consuming.
     * @param consumed the entity being consumed.
     * @return the rate limiter for the entity pair or NULL if the cache does not contains any rate limiter for that
     *         pair.
     */
    RateLimiter get(Object consumer, Object consumed);

    /**
     * Add the provided rate limiter to the cache for the given entity.
     *
     * @param consumer the entity consuming.
     * @param consumed the entity being consumed.
     * @param limiter the limiter to add.
     */
    void add(Object consumer, Object consumed, RateLimiter limiter);

    /**
     * Clear the cache.
     */
    void clear();
}
