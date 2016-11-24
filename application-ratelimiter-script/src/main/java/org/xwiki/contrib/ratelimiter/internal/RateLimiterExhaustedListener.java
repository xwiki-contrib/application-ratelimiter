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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.ratelimiter.RateLimiter;
import org.xwiki.contrib.ratelimiter.RateLimiterEntry;
import org.xwiki.contrib.ratelimiter.event.RateLimiterExhaustedEvent;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

/**
 * Listen to rate limiter exhausted events in order to provides notification and logging.
 *
 * @version $Id$
 */
@Component
@Named(RateLimiterExhaustedListener.NAME)
@Singleton
public class RateLimiterExhaustedListener implements EventListener
{
    /**
     * Name of this listener.
     */
    public static final String NAME = "RateLimiterExhaustedListener";

    /**
     * The list of events to listen to.
     */
    private static final List<Event> EVENTS = Collections.<Event>singletonList(new RateLimiterExhaustedEvent());

    @Inject
    private Logger logger;

    @Inject
    private RateLimiterServiceLogger rateLimiterServiceLogger;

    @Inject
    private RateLimiterServiceMailer rateLimiterServiceMailer;

    @Inject
    private EntityReferenceSerializer<String> serializer;

    @Override
    public List<Event> getEvents()
    {
        return EVENTS;
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        if (!(data instanceof RateLimiterEntry)) {
            logger.warn("Incompatible data found in RateListenerExhausted event, ignoring.");
            return;
        }

        RateLimiterEntry rlentry = (RateLimiterEntry) data;

        String consumer = serialize(rlentry.getConsumer());
        if (consumer == null) {
            logger.warn("Unable to determine the consumer, ignoring.");
            return;
        }

        String consumed = serialize(rlentry.getConsumed());
        if (consumed == null) {
            logger.warn("Unable to determine the consumed entity, ignoring.");
            return;
        }

        if (!(rlentry.getLimiter() instanceof CollectionRateLimiter)) {
            logger.warn("Unsupported rate limiter exceeded [{}], ignoring.", rlentry.getLimiter().getClass().getName());
            return;
        }

        RateLimiter exhaustedLimiter =
            getExhaustedRateLimiter(((CollectionRateLimiter) rlentry.getLimiter()).getRateLimiters());

        if (exhaustedLimiter == null) {
            logger.warn("Unable to determine the exceeded limiter, ignoring.");
            return;
        }

        rateLimiterServiceLogger.logAbuse(consumer, consumed, exhaustedLimiter);
        rateLimiterServiceMailer.sendAbuseNotification(consumer, consumed, exhaustedLimiter);
    }

    private String serialize(Object value)
    {
        return (value instanceof String) ? (String) value
            : (value instanceof EntityReference)
            ? serializer.serialize((EntityReference) value)
            : null;
    }

    private RateLimiter getExhaustedRateLimiter(Collection<RateLimiter> ratelimiters)
    {
        RateLimiter exhaustedLimiter = null;
        for (RateLimiter limiter : ratelimiters) {
            if (limiter.getAvailableAmount(false) < 0) {
                if (exhaustedLimiter == null
                    || limiter.getAvailableAmount(false) < exhaustedLimiter.getAvailableAmount(false)) {
                    exhaustedLimiter = limiter;
                }
            }
        }
        return exhaustedLimiter;
    }
}
