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

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.ratelimiter.RateLimiter;
import org.xwiki.mail.MailListener;
import org.xwiki.mail.MailSender;
import org.xwiki.mail.MailSenderConfiguration;
import org.xwiki.mail.MimeMessageFactory;
import org.xwiki.mail.XWikiAuthenticator;
import org.xwiki.model.reference.DocumentReference;

/**
 * Default implementation of {@link RateLimiterServiceMailer}.
 *
 * @version $Id$
 */
@Component
@Singleton
public class DefaultRateLimiterServiceMailer implements RateLimiterServiceMailer
{
    private final Map<Integer, Long> lastNotifTimes = new HashMap<Integer, Long>();

    @Inject
    private RateLimiterServiceLocalization localization;

    @Inject
    private MailSenderConfiguration mailConfiguration;

    @Inject
    @Named("template")
    private MimeMessageFactory<MimeMessage> messageFactory;

    @Inject
    private MailSender mailSender;

    @Inject
    @Named("database")
    private Provider<MailListener> databaseMailListenerProvider;

    @Inject
    private RateLimitingServiceConfiguration configuration;

    private int getKey(String consumer, String consumed) {
        return ((consumer == null) ? 0 : 37 * consumer.hashCode()) + ((consumed == null) ? 0 : consumed.hashCode());
    }

    @Override
    public void sendAbuseNotification(String consumer, String consumed, RateLimiter exhaustedLimiter)
    {
        if (configuration.getMailFromAddress() == null || configuration.getMailToAddress() == null) {
            return;
        }

        Long lastNotifTime = lastNotifTimes.get(getKey(consumer, consumed));
        if (lastNotifTime != null && (new Date().getTime() - lastNotifTime) < configuration.getMailInterval()) {
            return;
        }

        Session session;

        if (mailConfiguration.usesAuthentication()) {
            session = Session.getInstance(mailConfiguration.getAllProperties(),
                new XWikiAuthenticator(mailConfiguration));
        } else {
            session = Session.getInstance(mailConfiguration.getAllProperties());
        }

        Map<String, Object> parameters = new HashMap<String, Object>();
        Map<String, Object> vcontext = new HashMap<String, Object>();
        parameters.put("from", configuration.getMailFromAddress());
        parameters.put("to", configuration.getMailToAddress());
        parameters.put("language", configuration.getLocale().getLanguage());
        parameters.put("velocityVariables", vcontext);
        vcontext.put("consumer", consumer);
        vcontext.put("consumed", consumed);
        vcontext.put("limit", exhaustedLimiter.getLimit());
        vcontext.put("period",
            exhaustedLimiter.getPeriod() + " " + localization.getTranslatedUnit(exhaustedLimiter.getPeriodUnit()));
        vcontext.put("interval", configuration.getFormattedMailInterval());

        try {
            MimeMessage message = messageFactory.createMessage(session,
                new DocumentReference("xwiki", "RateLimiter", "NotificationMailTemplate"),
                parameters);

            mailSender.sendAsynchronously(Collections.singleton(message), session, databaseMailListenerProvider.get());
            lastNotifTimes.put(getKey(consumer, consumed), new Date().getTime());
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
