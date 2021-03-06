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
 * A no limit rate limiter.
 *
 * @version $Id$
 */
public class NoLimitRateLimiter implements RateLimiter
{
    @Override
    public long getPeriod()
    {
        return 0;
    }

    @Override
    public long getPeriod(TimeUnit unit)
    {
        return 0;
    }

    @Override
    public TimeUnit getPeriodUnit()
    {
        return TimeUnit.MINUTES;
    }

    @Override
    public long getLimit()
    {
        return Long.MAX_VALUE;
    }

    @Override
    public boolean consume(long amount)
    {
        return true;
    }

    @Override
    public RateLimiter clone(boolean empty)
    {
        return null;
    }

    @Override
    public long getAvailableAmount()
    {
        return Long.MAX_VALUE;
    }

    @Override
    public long getAvailableAmount(boolean update)
    {
        return 0;
    }

    @Override
    public long getWaitingTime(long amount, TimeUnit unit)
    {
        return 0;
    }

    @Override
    public long getWaitingTime(long amount, TimeUnit unit, boolean update)
    {
        return 0;
    }

    @Override
    public void reset()
    {
        //No-op
    }
}
