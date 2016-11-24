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
 * Rate Limiter logger used to log rate limiter events.
 *
 * @version $Id$
 */
@Role
public interface RateLimiterServiceLogger
{
    /**
     * Log to the rate limiter log.
     *
     * @param consumer the consumer concerned.
     * @param consumed the consumed entity.
     * @param exhaustedLimiter the exhausted rate limiter.
     */
    void logAbuse(String consumer, String consumed, RateLimiter exhaustedLimiter);

    /**
     * @return the absolute path name of the log file.
     */
    String getLogFileName();
}
