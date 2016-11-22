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

package org.xwiki.contrib.ratelimiter.event;

import org.xwiki.observation.event.AbstractFilterableEvent;
import org.xwiki.observation.event.filter.EventFilter;

/**
 * Event emitted when a rate limiter has reach its configured limit and a consumption request has returned false.
 *
 * @version $Id$
 */
public class RateLimiterExhaustedEvent extends AbstractFilterableEvent
{
    private static final long serialVersionUID = 1L;

    /**
     * Constructor initializing an event matching any other event of the same type.
     */
    public RateLimiterExhaustedEvent()
    {
    }

    /**
     * Constructor initializing an event matching only events of the same type affecting the same passed serialized
     * entity reference of the entity having exhausted the limit.
     *
     * @param name serialized entity reference of the entity to observe.
     */
    public RateLimiterExhaustedEvent(String name)
    {
        super(name);
    }

    /**
     * Constructor using a custom {@link EventFilter}.
     *
     * @param eventFilter the filter to use for matching events
     */
    public RateLimiterExhaustedEvent(EventFilter eventFilter)
    {
        super(eventFilter);
    }
}
