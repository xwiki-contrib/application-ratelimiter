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

package org.xwiki.contrib.ratelimiter;

import java.util.concurrent.TimeUnit;

import org.xwiki.contrib.ratelimiter.internal.NoLimitRateLimiter;

/**
 * A very simple rate limiting interface.
 *
 * @version $Id$
 */
public interface RateLimiter
{
    /**
     * A no limit rate limiter.
     */
    RateLimiter NOLIMIT = new NoLimitRateLimiter();

    /**
     * @return the rate limiter period in the unit expressed during construction.
     */
    long getPeriod();

    /**
     * @param unit the unit requested for returning the period.
     * @return the rate limiter period in the requested unit.
     */
    long getPeriod(TimeUnit unit);

    /**
     * @return the unit used for the period during construction.
     */
    TimeUnit getPeriodUnit();

    /**
     * @return the limit of this limiter.
     */
    long getLimit();

    /**
     * Consume a given amount, and check if this is exceeding the limitation.
     *
     * @param amount the amount that will be consumed.
     * @return true if the consumption is under the limitation.
     */
    boolean consume(long amount);

    /**
     * Clone this rate limiter.
     *
     * @param empty when true, the consumption level of the limiter is emptied.
     * @return a deep clone of this rate limiter.
     */
    RateLimiter clone(boolean empty);

    /**
     * @return the current consumable amount.
     */
    long getAvailableAmount();

    /**
     * @param update when true, update the state of the limiter before reporting the value.
     * @return the current consumable amount.
     */
    long getAvailableAmount(boolean update);

    /**
     * Return the time to wait before a given amount can be consumed with success.
     *
     * @param amount the amount that should be consumable.
     * @param unit the unit in which the time to wait should be returned.
     * @return the time to wait or 0 if the amount is consumable now.
     */
    long getWaitingTime(long amount, TimeUnit unit);

    /**
     * Return the time to wait before a given amount can be consumed with success.
     *
     * @param amount the amount that should be consumable.
     * @param unit the unit in which the time to wait should be returned.
     * @param update when true, update the state of the limiter before reporting the value.
     * @return the time to wait or 0 if the amount is consumable now.
     */
    long getWaitingTime(long amount, TimeUnit unit, boolean update);

    /**
     * Reset the current limiter.
     */
    void reset();
}
