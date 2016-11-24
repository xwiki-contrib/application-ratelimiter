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

import org.xwiki.component.annotation.Role;

/**
 * Configuration of the rate limiting service.
 *
 * @version $Id$
 */
@Role
public interface RateLimitingServiceConfiguration
{
    /**
     * @return the email of the sender of notification. Default to the admin email from preferences.
     */
    String getMailFromAddress();

    /**
     * @return the email of the recipient of abuse notification. Default to the admin email from preferences.
     */
    String getMailToAddress();

    /**
     * @return the minimum interval between similar notification. Default to 2 hours.
     */
    long getMailInterval();

    /**
     * @return a formatted string of the mail interval
     */
    String getFormattedMailInterval();

    /**
     * @return the locale used for logs and notifications. Default to english.
     */
    Locale getLocale();
}
