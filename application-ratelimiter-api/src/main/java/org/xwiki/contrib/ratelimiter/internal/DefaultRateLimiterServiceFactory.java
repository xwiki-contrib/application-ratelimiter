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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.ratelimiter.RateLimiter;
import org.xwiki.contrib.ratelimiter.RateLimiterService;
import org.xwiki.contrib.ratelimiter.RateLimiterServiceFactory;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.observation.ObservationManager;

/**
 * Default implementation of {@link RateLimiterService}.
 *
 * @version $Id$
 */
@Component
@Singleton
public class DefaultRateLimiterServiceFactory implements RateLimiterServiceFactory
{
    @Inject
    private RateLimiterCache cache;

    @Inject
    private ObservationManager observationManager;

    @Inject
    private EntityReferenceSerializer<String> entityReferenceSerializer;

    @Override
    public RateLimiterService create(RateLimiter rateLimiterTemplate)
    {
        return new DefaultRateLimiterService(cache, rateLimiterTemplate,
            observationManager, entityReferenceSerializer);
    }
}
