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

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.xwiki.component.annotation.Component;
import org.xwiki.configuration.ConfigurationSource;

/**
 * Default implementation of {@link RateLimitingServiceConfiguration}.
 *
 * @version $Id$
 */
@Component
@Singleton
public class DefaultRateLimiterServiceConfiguration implements RateLimitingServiceConfiguration
{
    private static final String FROM_PROPERTY = "mail_from";
    private static final String TO_PROPERTY = "mail_to";
    private static final String INTERVAL_PROPERTY = "mail_interval";
    private static final String INTERVAL_UNIT_PROPERTY = "interval_unit";
    private static final String LANGUAGE_PROPERTY = "language";
    private static final String ADMIN_EMAIL_PREFERENCE = "admin_email";
    private static final String DEFAULT_LANGUAGE_PREFERENCE = "default_language";

    @Inject
    @Named("ratelimiterservice")
    private ConfigurationSource configurationSource;

    @Inject
    @Named("documents")
    private ConfigurationSource documentsSource;

    @Override
    public String getMailFromAddress()
    {
        return this.configurationSource.getProperty(FROM_PROPERTY,
            this.documentsSource.getProperty(ADMIN_EMAIL_PREFERENCE, String.class));
    }

    @Override
    public String getMailToAddress()
    {
        return this.configurationSource.getProperty(TO_PROPERTY,
            this.documentsSource.getProperty(ADMIN_EMAIL_PREFERENCE, String.class));
    }

    private TimeUnit safeGetTimeUnit(String property, TimeUnit defaultValue) {
        try {
            return TimeUnit.valueOf(
                this.configurationSource.getProperty(property, defaultValue.toString()));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    public long getMailInterval() {
        return TimeUnit.MILLISECONDS.convert(this.configurationSource.getProperty(INTERVAL_PROPERTY, 2L),
            safeGetTimeUnit(INTERVAL_UNIT_PROPERTY, TimeUnit.HOURS));
    }

    @Override
    public String getFormattedMailInterval() {
        Period period = new Period(getMailInterval());
        PeriodFormatter formatter = PeriodFormat.wordBased(getLocale());
        return formatter.print(period);
    }

    @Override
    public Locale getLocale() {
        return new Locale(this.configurationSource.getProperty(LANGUAGE_PROPERTY,
            this.documentsSource.getProperty(DEFAULT_LANGUAGE_PREFERENCE, "en")));
    }
}
