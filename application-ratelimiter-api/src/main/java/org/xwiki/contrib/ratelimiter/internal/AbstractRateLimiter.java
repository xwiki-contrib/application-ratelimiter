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
 * Base class for {@link RateLimiter}.
 *
 * @version $Id$
 */
public abstract class AbstractRateLimiter implements RateLimiter
{
    protected final long period;
    protected final TimeUnit unit;
    protected final long limit;

    /**
     * Initializing constructor.
     *
     * @param limit the maximum amount of consumption over a period of time.
     * @param period the period of time.
     * @param unit the unit used to express the period of time.
     */
    public AbstractRateLimiter(long limit, long period, TimeUnit unit)
    {
        this.period = period;
        this.unit = unit;
        this.limit = limit;
    }

    /**
     * Copy constructor with reinitialization.
     *
     * @param rateLimiter the source to copy.
     */
    public AbstractRateLimiter(AbstractRateLimiter rateLimiter)
    {
        this.period = rateLimiter.period;
        this.unit = rateLimiter.unit;
        this.limit = rateLimiter.limit;
    }

    @Override
    public long getPeriod()
    {
        return period;
    }

    @Override
    public long getPeriod(TimeUnit unit)
    {
        return unit.convert(period, unit);
    }

    @Override
    public TimeUnit getPeriodUnit()
    {
        return unit;
    }

    @Override
    public long getLimit()
    {
        return limit;
    }

    @Override
    public long getAvailableAmount()
    {
        return getAvailableAmount(true);
    }

    @Override
    public long getWaitingTime(long amount, TimeUnit unit)
    {
        return getWaitingTime(amount, unit, true);
    }
}
