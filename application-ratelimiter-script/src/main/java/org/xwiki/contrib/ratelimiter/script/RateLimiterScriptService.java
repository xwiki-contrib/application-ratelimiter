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

package org.xwiki.contrib.ratelimiter.script;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.ratelimiter.RateLimiter;
import org.xwiki.contrib.ratelimiter.RateLimiterBuilder;
import org.xwiki.contrib.ratelimiter.RateLimiterService;
import org.xwiki.contrib.ratelimiter.RateLimiterServiceFactory;
import org.xwiki.contrib.ratelimiter.internal.RateLimiterServiceActionListener;
import org.xwiki.contrib.ratelimiter.internal.RateLimiterServiceLogger;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.ObservationManager;
import org.xwiki.script.service.ScriptService;
import org.xwiki.security.authorization.ContextualAuthorizationManager;
import org.xwiki.security.authorization.Right;
import org.xwiki.velocity.VelocityManager;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.XWikiRequest;

/**
 * Script service for creating and using {@link RateLimiterService}.
 *
 * @version $Id$
 */
@Component
@Named("ratelimiter")
public class RateLimiterScriptService implements ScriptService
{
    @Inject
    private Provider<RateLimiterBuilder> builderProvider;

    @Inject
    private RateLimiterServiceFactory factory;

    @Inject
    private RateLimiterServiceLogger rateLimiterServiceLogger;

    @Inject
    private ContextualAuthorizationManager contextualAuthorizationManager;

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    private ObservationManager observationManager;

    @Inject
    private VelocityManager velocityManager;

    private Object getCurrentConsumed() {
        return contextProvider.get().getDoc().getDocumentReference().getWikiReference();
    }

    private static String getRemoteAddress(XWikiRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        return (ipAddress != null) ? ipAddress.split(",", 1)[0] : request.getRemoteAddr();
    }

    private Object getCurrentConsumer() {
        DocumentReference user = contextProvider.get().getUserReference();
        return user == null ? getRemoteAddress(contextProvider.get().getRequest()) : user;
    }

    /**
     * @return a new {@link RateLimiterBuilder} ready for building a new rate limiter template.
     */
    public RateLimiterBuilder getBuilder()
    {
        return builderProvider.get();
    }

    /**
     * Create a new {@link RateLimiterService} based on the provided template.
     * Programming rights is required to use this method.
     *
     * @param limiterTemplate a rate limiter to be used as a template for creating limiter of this service.
     * @return a new {@link RateLimiterService} ready for use.
     */
    public RateLimiterService createRateLimiterService(RateLimiter limiterTemplate)
    {
        if (contextualAuthorizationManager.hasAccess(Right.PROGRAM)) {
            return factory.create(limiterTemplate);
        }
        return null;
    }

    /**
     * Initialize or reinitialize the action rate limiting service with the given template. If the provided template
     * is null, the service is deactivated.
     *
     * @param limiterTemplate a rate limiter to be used as a template for creating limiter of the default service.
     */
    public void setActionRateLimiter(RateLimiter limiterTemplate)
    {
        if (contextualAuthorizationManager.hasAccess(Right.PROGRAM)) {
            EventListener listener = observationManager.getListener(RateLimiterServiceActionListener.NAME);
            if (listener != null) {
                observationManager.removeListener(RateLimiterServiceActionListener.NAME);
            }

            if (limiterTemplate != null) {
                observationManager.addListener(
                    new RateLimiterServiceActionListener(factory.create(limiterTemplate),
                        velocityManager, contextualAuthorizationManager));
            }
        }
    }

    /**
     * @return the current rate limiter service.
     */
    public RateLimiterService getService()
    {
        EventListener listener = observationManager.getListener(RateLimiterServiceActionListener.NAME);
        if (listener != null) {
            return ((RateLimiterServiceActionListener) listener).getService();
        }
        return null;
    }

    /**
     * Consume as the current user on the current wiki, limiters are created on the fly as needed, and events are
     * sent when exhaustion happen.
     *
     * @param amount the amount being consumed.
     * @return true if the consumption was successful.
     */
    public boolean consume(long amount)
    {
        RateLimiterService service = getService();
        return service == null
            || service.consume(getCurrentConsumer(), getCurrentConsumed(), amount);
    }

    /**
     * Consume as the current user on the current wiki, limiters are created on the fly as needed, and events are
     * sent when exhaustion happen.
     *
     * @param consumer the entity consuming.
     * @param consumed the entity being consumed.
     * @param amount the amount being consumed.
     * @return true if the consumption was successful.
     */
    public boolean consume(Object consumer, Object consumed, long amount)
    {
        if (contextualAuthorizationManager.hasAccess(Right.PROGRAM)) {
            RateLimiterService service = getService();
            return service == null
                || service.consume(consumer, consumed, amount);
        }
        return true;
    }

    /**
     * @return the current consumable amount for the current user on the current wiki.
     */
    public long getAvailableAmount()
    {
        RateLimiterService service = getService();
        return service == null ? Long.MAX_VALUE
            : service.getRateLimiter(getCurrentConsumer(), getCurrentConsumed())
                .getAvailableAmount();
    }

    /**
     * @param consumer the entity consuming.
     * @param consumed the entity being consumed.
     * @return the current consumable amount for the current user on the current wiki.
     */
    public long getAvailableAmount(Object consumer, Object consumed)
    {
        if (contextualAuthorizationManager.hasAccess(Right.PROGRAM)) {
            RateLimiterService service = getService();
            return service == null ? Long.MAX_VALUE
                : service.getRateLimiter(consumer, consumed).getAvailableAmount();
        }
        return Long.MAX_VALUE;
    }

    /**
     * Return in seconds the time to wait before a given amount can be consumed by the current user on the current wiki.
     *
     * @param amount the amount that should be consumable.
     * @param unit the {@link TimeUnit} to use for reporting the return value.
     * @return the time to wait or 0 if the amount is consumable now.
     */
    public long getWaitingTime(long amount, TimeUnit unit)
    {
        RateLimiterService service = getService();
        return service == null ? 0
            : service.getRateLimiter(getCurrentConsumer(), getCurrentConsumed())
                .getWaitingTime(amount, unit);
    }

    /**
     * Return in seconds the time to wait before a given amount can be consumed by given user on a given resource.
     *
     * @param consumer the entity consuming.
     * @param consumed the entity being consumed.
     * @param amount the amount that should be consumable.
     * @param unit the {@link TimeUnit} to use for reporting the return value.
     * @return the time to wait or 0 if the amount is consumable now.
     */
    public long getWaitingTime(Object consumer, Object consumed, long amount, TimeUnit unit)
    {
        if (contextualAuthorizationManager.hasAccess(Right.PROGRAM)) {
            RateLimiterService service = getService();
            return service == null ? 0
                : service.getRateLimiter(consumer, consumed)
                    .getWaitingTime(amount, unit);
        }
        return 0;
    }

    /**
     * Return as a human readable formatted string the time to wait before a given amount can be consumed by the
     * current user on the current wiki.
     *
     * @param amount the amount that should be consumable.
     * @param locale the locale used to format the result.
     * @return the time to wait as a formatted string.
     */
    public String getFormattedWaitingTime(long amount, Locale locale)
    {
        Period period = new Period(getWaitingTime(amount, TimeUnit.MILLISECONDS));
        PeriodFormatter formatter = PeriodFormat.wordBased(locale);
        return formatter.print(period);
    }

    /**
     * Return as a human readable formatted string the time to wait before a given amount can be consumed by the
     * current user on the current wiki.
     *
     * @param consumer the entity consuming.
     * @param consumed the entity being consumed.
     * @param amount the amount that should be consumable.
     * @param locale the locale used to format the result.
     * @return the time to wait as a formatted string.
     */
    public String getFormattedWaitingTime(Object consumer, Object consumed, long amount,
        Locale locale)
    {
        if (contextualAuthorizationManager.hasAccess(Right.PROGRAM)) {
            Period period = new Period(getWaitingTime(consumer, consumed, amount, TimeUnit.MILLISECONDS));
            PeriodFormatter formatter = PeriodFormat.wordBased(locale);
            return formatter.print(period);
        }
        return null;
    }

    /**
     * Retrieve the rate limiter currently applied.
     * Please note that consuming on that limiter is not a good idea since it will not trigger event, and
     * might not be persisted (in case you received a NOLIMIT instance), see {@link #consume(long)}.
     * Programming right is required.
     *
     * @return the limiter or {@link RateLimiter#NOLIMIT} if none is available for the given entity.
     */
    public RateLimiter getRateLimiter()
    {
        RateLimiterService service = getService();
        if (contextualAuthorizationManager.hasAccess(Right.PROGRAM)) {
            if (service == null) {
                return RateLimiter.NOLIMIT;
            }
            return service.getRateLimiter(getCurrentConsumer(), getCurrentConsumed());
        }
        return null;
    }

    /**
     * @return the filename of the rate limiter log.
     */
    public String getLogFileName()
    {
        return rateLimiterServiceLogger.getLogFileName();
    }
}
