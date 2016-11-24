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

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.localization.Translation;
import org.xwiki.localization.TranslationBundle;
import org.xwiki.localization.TranslationBundleDoesNotExistsException;
import org.xwiki.localization.TranslationBundleFactory;

/**
 * Default implementation of {@link RateLimiterServiceLocalization}.
 *
 * @version $Id$
 */
@Component
@Singleton
public class DefaultRateLimiterServiceLocalization implements RateLimiterServiceLocalization
{
    private static final String TRANSLATION_BUNDLE = "xwiki:RateLimiter.Translations";

    @Inject
    private RateLimitingServiceConfiguration configuration;

    @Inject
    @Named("document")
    private Provider<TranslationBundleFactory> translationBundleFactoryProvider;

    private TranslationBundle translationBundle;

    @Override
    public String getAbuseLogMessage()
    {
        return getTranslation("ratelimiter.log.abuse.message",
            "User [{}] tried to abuse of [{}] by exceeding the {} requests / {} {} limit.");
    }

    @Override
    public String getTranslatedUnit(TimeUnit unit)
    {
        return getTranslation("RateLimiter.RateLimiterConfigClass_unit_" + unit.toString(),
            unit.toString().toLowerCase());
    }

    private String getTranslation(String id, String defaultValue)
    {
        if (translationBundle == null) {
            try {
                translationBundle = translationBundleFactoryProvider.get().getBundle(TRANSLATION_BUNDLE);
            } catch (TranslationBundleDoesNotExistsException e) {
                // Ignored
            }
        }

        String translation = defaultValue;
        if (translationBundle != null) {
            Translation msg = translationBundle.getTranslation(id, configuration.getLocale());
            if (msg != null) {
                translation = (String) msg.getRawSource();
            }
        }
        return translation;
    }
}
