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

import org.xwiki.contrib.ratelimiter.RateLimiter;

/**
 * Implement a {@link RateLimiter} using a leaky bucket like algorithm. The bucket never overflow, but the bucket
 * level should be lower than limit in order to allow normal consumption.
 *
 * @version $Id$
 */
public class InfiniteLeakyBucketRateLimiter extends AbstractRateLimiter
{
    protected final long leakrate;
    protected long lastUpdate;
    protected long bucketLevel;

    /**
     * Initializing constructor.
     *
     * @param limit the maximum amount of consumption over a period of time.
     * @param period the period of time.
     * @param unit the unit used to express the period of time.
     */
    public InfiniteLeakyBucketRateLimiter(long limit, long period, TimeUnit unit)
    {
        super(limit, period, unit);
        this.leakrate = TimeUnit.NANOSECONDS.convert(period, unit) / limit;
        this.lastUpdate = System.nanoTime();
    }

    /**
     * Copy constructor.
     *
     * @param rateLimiter source object.
     */
    public InfiniteLeakyBucketRateLimiter(InfiniteLeakyBucketRateLimiter rateLimiter)
    {
        this(rateLimiter, false);
    }

    /**
     * Copy constructor with reinitialization.
     *
     * @param rateLimiter the source to copy.
     * @param empty when true, the copy is empty.
     */
    public InfiniteLeakyBucketRateLimiter(InfiniteLeakyBucketRateLimiter rateLimiter, boolean empty)
    {
        super(rateLimiter);
        this.leakrate = rateLimiter.leakrate;
        if (!empty) {
            this.lastUpdate = rateLimiter.lastUpdate;
            this.bucketLevel = rateLimiter.bucketLevel;
        } else {
            this.lastUpdate = System.nanoTime();
        }
    }

    @Override
    public RateLimiter clone(boolean empty)
    {
        return new InfiniteLeakyBucketRateLimiter(this, empty);
    }

    protected synchronized boolean internalConsume(long amount)
    {
        long elapsed = System.nanoTime() - lastUpdate;
        long leak = elapsed / leakrate;
        lastUpdate += leak * leakrate;
        bucketLevel = Math.max(0, bucketLevel - leak + amount);
        return bucketLevel <= limit;
    }

    @Override
    public boolean consume(long amount)
    {
        return internalConsume(amount);
    }

    @Override
    public long getAvailableAmount(boolean update)
    {
        if (update) {
            internalConsume(0);
        }
        return limit - bucketLevel;
    }

    @Override
    public long getWaitingTime(long amount, TimeUnit unit, boolean update)
    {
        if (update) {
            internalConsume(0);
        }
        long overflow = (bucketLevel - limit + amount);
        if (overflow <= 0) {
            return 0;
        }
        return unit.convert(overflow * leakrate, TimeUnit.NANOSECONDS);
    }

    @Override
    public void reset()
    {
        this.bucketLevel = 0;
    }
}
