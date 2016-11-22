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

import java.util.Arrays;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.xwiki.bridge.event.ActionExecutingEvent;
import org.xwiki.contrib.ratelimiter.RateLimiterService;
import org.xwiki.contrib.ratelimiter.script.RateLimiterScriptService;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;
import org.xwiki.security.authorization.ContextualAuthorizationManager;
import org.xwiki.security.authorization.Right;
import org.xwiki.velocity.VelocityManager;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * Listen to action in order to introduce appropriate rate limitation.
 *
 * @version $Id$
 */
public class RateLimiterActionListener implements EventListener
{
    /**
     * Name of this listener.
     */
    public static final String NAME = "RateLimiterActionListener";

    /**
     * The list of events to listen to.
     */
    private static final List<Event> EVENTS = Arrays.<Event>asList(new ActionExecutingEvent());

    private static final String CTX_DOC = "doc";

    private static final String CTX_CDOC = "cdoc";

    private static final String CTX_TDOC = "tdoc";

    private static final List<String> SKIN_ACTIONS = Arrays.asList("skin", "jsx", "ssx");

    private final RateLimiterService service;
    private final VelocityManager velocityManager;
    private final ContextualAuthorizationManager contextualAuthorizationManager;


    /**
     * Constructor.
     *
     * @param service the rate limiter service that will support this listener.
     * @param velocityManager the velocity manager in order to get the velocity context.
     * @param contextualAuthorizationManager the authorization manager in order to avoid rate limiting on admins.
     */
    public RateLimiterActionListener(RateLimiterService service, VelocityManager velocityManager,
        ContextualAuthorizationManager contextualAuthorizationManager)
    {
        this.service = service;
        this.velocityManager = velocityManager;
        this.contextualAuthorizationManager = contextualAuthorizationManager;
    }

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
        if (contextualAuthorizationManager.hasAccess(Right.ADMIN)) {
            return;
        }

        XWikiContext xcontext = (XWikiContext) data;
        XWikiDocument doc = xcontext.getDoc();
        String action = ((ActionExecutingEvent) event).getActionName();
        EntityReference user = xcontext.getUserReference();

        if (user == null) {
            user = RateLimiterScriptService.getIpUser(xcontext);
        }

        boolean allowed =
            service.consume(user, doc.getDocumentReference().getWikiReference(), 1);

        if (SKIN_ACTIONS.contains(action) || (action.equals("download") && doc.getObject("XWiki.XWikiSkin") != null)) {
            // Do not rate limit during skin and resource actions, including download action of skin files
            return;
        }

        if (!allowed) {
            if (action.equals("view")) {
                try {
                    changeContextDoc(xcontext.getWiki().getDocument("RateLimiter.ExhaustedError", xcontext),
                        xcontext, velocityManager.getVelocityContext());
                } catch (XWikiException e) {
                    ((ActionExecutingEvent) event).cancel("Rate Limiter exhausted error failure");
                }
            } else {
                ((ActionExecutingEvent) event).cancel("Rate Limiter exhausted");
            }
        }
    }

    /**
     * @return the rate limiter service supporting this listener.
     */
    public RateLimiterService getService()
    {
        return service;
    }

    private void changeContextDoc(XWikiDocument doc, XWikiContext xcontext, VelocityContext vcontext)
        throws XWikiException
    {

        xcontext.put(CTX_DOC, doc);
        xcontext.put(CTX_CDOC, doc);

        Document vdoc = doc.newDocument(xcontext);
        vcontext.put(CTX_DOC, vdoc);
        vcontext.put(CTX_CDOC, vdoc);

        XWikiDocument tdoc = doc.getTranslatedDocument(xcontext);
        xcontext.put(CTX_TDOC, tdoc);
        vcontext.put(CTX_TDOC, tdoc.newDocument(xcontext));
    }
}
