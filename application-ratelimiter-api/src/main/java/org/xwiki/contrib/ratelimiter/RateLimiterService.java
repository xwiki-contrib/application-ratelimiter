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

import org.xwiki.model.reference.EntityReference;

/**
 * A rate limiting service allows limiting consumption rate of a resource based on a configured rate limiter.
 *
 * @version $Id$
 */
public interface RateLimiterService
{
    /**
     * Consume, and check if this is exceeding the limitation.
     *
     * @param consumer the entity consuming.
     * @param consumed the entity being consumed.
     * @param amount the amount that will be consumed.
     * @return true if the consumption is under the limitation.
     */
    boolean consume(EntityReference consumer, EntityReference consumed, long amount);

    /**
     * Retrieve the actual rate limiter for a given consumer on a given consumed entity.
     *
     * @param consumer the entity consuming.
     * @param consumed the entity being consumed.
     * @return a rate limiter, if no limiter is currently available {@link RateLimiter#NOLIMIT} is returned.
     */
    RateLimiter getRateLimiter(EntityReference consumer, EntityReference consumed);
}
