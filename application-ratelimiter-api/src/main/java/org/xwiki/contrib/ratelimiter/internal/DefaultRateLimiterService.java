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

import org.xwiki.contrib.ratelimiter.RateLimiter;
import org.xwiki.contrib.ratelimiter.RateLimiterEntry;
import org.xwiki.contrib.ratelimiter.RateLimiterService;
import org.xwiki.contrib.ratelimiter.event.RateLimiterExhaustedEvent;
import org.xwiki.observation.ObservationManager;

/**
 * Default implementation of a {@link RateLimiterService}.
 *
 * @version $Id$
 */
class DefaultRateLimiterService implements RateLimiterService
{
    private final RateLimiterCache cache;
    private final RateLimiter rateLimiterTemplate;
    private final ObservationManager observationManager;

    DefaultRateLimiterService(RateLimiterCache cache, RateLimiter rateLimiterTemplate,
        ObservationManager observationManager)
    {
        this.cache = cache;
        this.rateLimiterTemplate = rateLimiterTemplate;
        this.observationManager = observationManager;
    }

    @Override
    public boolean consume(Object consumer, Object consumed, long amount)
    {
        RateLimiter limiter = safeGetRateLimiter(consumer, consumed);

        boolean wasNotExhausted = limiter.getAvailableAmount(false) >= 0;

        if (limiter.consume(amount)) {
            return true;
        }

        if (wasNotExhausted) {
            observationManager.notify(new RateLimiterExhaustedEvent(), this,
                new RateLimiterEntry(consumer, consumed, limiter));
        }
        return false;
    }

    @Override
    public RateLimiter getRateLimiter(Object consumer, Object consumed)
    {
        RateLimiter limiter = cache.get(consumer, consumed);
        return (limiter != null) ? limiter : RateLimiter.NOLIMIT;
    }

    private RateLimiter safeGetRateLimiter(Object consumer, Object consumed)
    {
        RateLimiter limiter = cache.get(consumer, consumed);
        if (limiter == null) {
            if (rateLimiterTemplate != null) {
                synchronized (cache) {
                    limiter = cache.get(consumer, consumed);
                    if (limiter == null) {
                        limiter = rateLimiterTemplate.clone(true);
                        cache.add(consumer, consumed, limiter);
                    }
                }
            } else {
                limiter = RateLimiter.NOLIMIT;
            }
        }
        return limiter;
    }
}
