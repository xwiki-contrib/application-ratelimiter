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

import java.util.concurrent.TimeUnit;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.contrib.ratelimiter.RateLimiter;
import org.xwiki.contrib.ratelimiter.RateLimiterBuilder;

/**
 * Build a rate limiter.
 *
 * @version $Id$
 */
@Component
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class DefaultRateLimiterBuilder implements RateLimiterBuilder
{
    private CollectionRateLimiter rateLimiter;

    @Override public RateLimiter build()
    {
        return rateLimiter;
    }

    private void addRateLimiter(RateLimiter limiter)
    {
        if (limiter != null) {
            if (rateLimiter == null) {
                rateLimiter = new CollectionRateLimiter(limiter);
            } else {
                rateLimiter.add(limiter);
            }
        }
    }

    @Override
    public RateLimiterBuilder addLimiter(RateLimiter limiter)
    {
        addRateLimiter(limiter);
        return this;
    }

    @Override
    public RateLimiterBuilder addLimiter(long limit, long period, TimeUnit unit)
    {
        if (limit < 1) {
            throw new IllegalArgumentException(String.format("Limit [%d] should at higher than 0.", limit));
        }
        addRateLimiter(new InfiniteLeakyBucketRateLimiter(limit, period, unit));
        return this;
    }

    @Override
    public RateLimiterBuilder addLimiter(long limit, long overflow, long period, TimeUnit unit)
    {
        if (limit > overflow) {
            throw new IllegalArgumentException(
                String.format("Limit [%d] should be lower or equal to overflow level [%d].", limit, overflow));
        }
        addRateLimiter(new OverflowingLeakyBucketRateLimiter(limit, overflow, period, unit));
        return this;
    }
}
