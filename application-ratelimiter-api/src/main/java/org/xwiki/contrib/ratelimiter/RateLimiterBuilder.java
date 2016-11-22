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

import org.xwiki.component.annotation.Role;

/**
 * Build rate limiters.
 *
 * @version $Id$
 */
@Role
public interface RateLimiterBuilder
{
    /**
     * @return the result of this builder.
     */
    RateLimiter build();

    /**
     * Add the given limiter to the built rate limiter.
     *
     * @param limiter the limiter to add.
     * @return this builder for call chaining.
     */
    RateLimiterBuilder addLimiter(RateLimiter limiter);

    /**
     * Add an infinite leaky bucket rate limiter with the given configuration to the built rate limiter.
     *
     * @param limit the maximum amount of consumption over a period of time.
     * @param period the period of time.
     * @param unit the unit used to express the period of time.
     * @return this builder for call chaining.
     */
    RateLimiterBuilder addLimiter(long limit, long period, TimeUnit unit);

    /**
     * Add an overflowing leaky bucket rate limiter with the given configuration to the built rate limiter.
     *
     * @param limit the maximum amount of consumption over a period of time.
     * @param overflow the overflow level, should be higher than the limit level.
     * @param period the period of time.
     * @param unit the unit used to express the period of time.
     * @return this builder for call chaining.
     */
    RateLimiterBuilder addLimiter(long limit, long overflow, long period, TimeUnit unit);
}
