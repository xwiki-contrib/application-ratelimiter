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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.xwiki.contrib.ratelimiter.RateLimiter;

/**
 * Implement a {@link RateLimiter} using a collection of other {@link RateLimiter}.
 *
 * @version $Id$
 */
public class CollectionRateLimiter implements RateLimiter
{
    private Collection<RateLimiter> rateLimiters = new ArrayList<RateLimiter>();

    /**
     * Construct a new collection of rate limiter with an initial one.
     *
     * @param rateLimiter the initial rate limiter to add to the collection.
     */
    public CollectionRateLimiter(RateLimiter rateLimiter)
    {
        this.rateLimiters.add(rateLimiter);
    }

    /**
     * Construct a new collection of rate limiter adding all provided limiters.
     *
     * @param rateLimiters the collection of limiters to be added.
     */
    public CollectionRateLimiter(Collection<RateLimiter> rateLimiters)
    {
        this.rateLimiters.addAll(rateLimiters);
    }

    /**
     * Copy constructor.
     *
     * @param rateLimiters the rate limiter collection to copy.
     */
    public CollectionRateLimiter(CollectionRateLimiter rateLimiters)
    {
        this(rateLimiters, false);
    }

    /**
     * Copy constructor from template.
     *
     * @param rateLimiters the rate limiter collection to copy.
     * @param empty when true, the copied collection is reset.
     */
    public CollectionRateLimiter(CollectionRateLimiter rateLimiters, boolean empty)
    {
        for (RateLimiter rateLimiter : rateLimiters.getRateLimiters()) {
            this.rateLimiters.add(rateLimiter.clone(empty));
        }
    }

    @Override
    public RateLimiter clone(boolean empty)
    {
        return new CollectionRateLimiter(this, empty);
    }

    /**
     * Add a rate limiter to the collection.
     *
     * @param rateLimiter the rate limiter to add.
     * @return true if the rate limiter has been added.
     */
    public boolean add(RateLimiter rateLimiter) {
        return rateLimiters.add(rateLimiter);
    }

    /**
     * Remove a rate limiter from the collection.
     *
     * @param rateLimiter the rate limiter to remove.
     * @return true if the limiter has been removed.
     */
    public boolean remove(RateLimiter rateLimiter) {
        return rateLimiters.remove(rateLimiter);
    }

    /**
     * @return the collection of rate limiter wrapped in this limiter.
     */
    public Collection<RateLimiter> getRateLimiters()
    {
        return Collections.unmodifiableCollection(rateLimiters);
    }

    @Override
    public boolean consume(long amount)
    {
        boolean result = true;
        for (RateLimiter rateLimiter : rateLimiters) {
            result &= rateLimiter.consume(amount);
        }
        return result;
    }

    @Override
    public long getAvailableAmount()
    {
        return getAvailableAmount(true);
    }


    @Override
    public long getAvailableAmount(boolean update)
    {
        long amount = Long.MAX_VALUE;
        for (RateLimiter rateLimiter : rateLimiters) {
            amount = Math.min(amount, rateLimiter.getAvailableAmount(update));
        }
        return amount;
    }

    @Override
    public long getWaitingTime(long amount, TimeUnit unit)
    {
        return getWaitingTime(amount, unit, true);
    }

    @Override
    public long getWaitingTime(long amount, TimeUnit unit, boolean update)
    {
        long waitTime = 0;
        for (RateLimiter rateLimiter : rateLimiters) {
            waitTime = Math.max(waitTime, rateLimiter.getWaitingTime(amount, unit, update));
        }
        return waitTime;
    }

    @Override
    public synchronized void reset()
    {
        for (RateLimiter rateLimiter : rateLimiters) {
            rateLimiter.reset();
        }
    }

    @Override
    public long getPeriod()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getPeriod(TimeUnit unit)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public TimeUnit getPeriodUnit()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getLimit()
    {
        throw new UnsupportedOperationException();
    }
}
