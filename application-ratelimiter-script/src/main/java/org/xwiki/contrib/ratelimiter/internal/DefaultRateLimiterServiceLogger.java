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

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.contrib.ratelimiter.RateLimiter;
import org.xwiki.environment.Environment;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;

/**
 * Default implementation of {@link RateLimiterServiceLogger}.
 *
 * @version $Id$
 */
@Component
@Singleton
public class DefaultRateLimiterServiceLogger implements RateLimiterServiceLogger, Initializable
{
    @Inject
    private RateLimiterServiceLocalization localization;

    /**
     * Used to get permanent directory.
     */
    @Inject
    private Environment environment;

    private FileAppender<ILoggingEvent> logAppender = new FileAppender<ILoggingEvent>();
    private Logger logger;

    @Override
    public void initialize() throws InitializationException
    {
        File logDir = new File(this.environment.getPermanentDirectory(), "logs");
        logDir.mkdirs();
        File logFile = new File(logDir, "ratelimiter.log");
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        logAppender.setContext(loggerContext);
        logAppender.setName("ratelimiter");
        logAppender.setFile(logFile.getAbsolutePath());
        logAppender.setAppend(true);

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%d %-5level - %msg [%thread]%n");
        encoder.start();
        logAppender.setEncoder(encoder);
        logAppender.start();

        logger = (Logger) LoggerFactory.getLogger(this.getClass());
        logger.addAppender(logAppender);
        logger.setLevel(Level.WARN);
        // Make sure only our new appender is used (and parent's appenders are not used) so that we don't generate
        // logs elsewhere (console, other file, etc).
        logger.setAdditive(false);
    }

    @Override
    public void logAbuse(String consumer, String consumed, RateLimiter exhaustedLimiter)
    {
        logger.warn(localization.getAbuseLogMessage(),
            new Object[] {consumer, consumed, exhaustedLimiter.getLimit(), exhaustedLimiter.getPeriod(),
                localization.getTranslatedUnit(exhaustedLimiter.getPeriodUnit())});
    }

    @Override
    public String getLogFileName()
    {
        return logAppender.rawFileProperty();
    }
}
