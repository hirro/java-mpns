/*
 * Copyright 2011, Mahmood Ali.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following disclaimer
 *     in the documentation and/or other materials provided with the
 *     distribution.
 *   * Neither the name of Mahmood Ali. nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.notnoop.mpns.notifications;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.notnoop.mpns.DeliveryClass;
import com.notnoop.mpns.MpnsNotification;
import com.notnoop.mpns.internal.Pair;

/**
 * An abstract notification builder for the common header settings
 *
 * @param <A>   the concrete type of the builder
 * @param <B>   the type of the generated message
 */
@SuppressWarnings("unchecked")
/*package-protected*/ abstract class AbstractNotificationBuilder<A extends AbstractNotificationBuilder<A, B>, B extends MpnsNotification> {
    protected List<Entry<String, String>> headers = new ArrayList<Entry<String, String>>();

    protected AbstractNotificationBuilder() {
    }

    protected AbstractNotificationBuilder(String type) {
        notificationType(type);
    }

    /**
     * Sets the message UUID.
     *
     * The message UUID is optional application-specific identifier for
     * book-keeping and associate it with the response.
     *
     * @param messageId notification message ID
     * @return  this
     */
    public A messageId(String messageId) {
        this.headers.add(Pair.of("X-MessageId", messageId));
        return (A)this;
    }

    /**
     * Sets the notification batching interval, indicating when the notification
     * should be delivered to the device
     *
     * @param delivery  batching interval
     * @return  this
     */
    public A notificationClass(DeliveryClass delivery) {
        this.headers.add(Pair.of("X-NotificationClass", String.valueOf(deliveryValueOf(delivery))));
        return (A)this;
    }


    /**
     * Sets the type of the push notification being sent.
     *
     * As of Windows Phone OS 7.0, the supported types are:
     * <ul>
     *  <li>token (for Tile messages)</li>
     *  <li>toast</li>
     *  <li>raw</li>
     * </ul>
     *
     * This method should probably not be called directly, as the concrete
     * builder class will set the appropriate notification type.
     *
     * @param type  the notification type
     * @return  this
     */
    public A notificationType(String type) {
        this.headers.add(Pair.of("X-WindowsPhone-Target", type));
        return (A)this;
    }

    /**
     * Sets the X-WNS-TTL property.
     * 
     * Specifies the TTL (expiration time) for a notification. 
     * This is not typically needed, but can be used if you want to ensure that your notifications are not displayed
     * later than a certain time. The TTL is specified in seconds and is relative to the time that WNS receives the
     * request. 
     * 
     * When a TTL is specified, the device will not display the notification after that time. 
     * Note that this could result in the notification never being shown at all if the TTL is too short. 
     * In general, an expiration time will be measured in at least minutes.
     * 
     * This header is optional. 
     * If no value is specified, the notification does not expire and will be replaced under the normal notification 
     * replacement scheme.
     *
     * @param ttl the life span of the notification, in seconds, after WNS receives the request.
     * @return  this
     */
    public A ttl(Long ttl) {
        this.headers.add(Pair.of("X-WNS-TTL", Long.toString(ttl)));
        return (A)this;
    }
    
    /**
     * Sets the X-WNS-Cache-Policy property.
     * 
     * When the notification target device is offline, WNS will cache one badge and one tile notification per app. 
     * If notification cycling is enabled for the app, WNS will cache up to five tile notifications. 
     * By default, raw notifications are not cached, but if raw notification caching is enabled, 
     * one raw notification is cached. 
     * Items are not held in the cache indefinitely and will be dropped after a reasonable period of time. 
     * Otherwise, the cached content is delivered when the device next comes online.
     * 
     * This header is optional and should be used only in cases where the cloud service wants to override the
     * default caching behavior
     *
     * @param enabled if true caching is enabled (default)
     * @return  this
     */
    public A cache(boolean enabled) {
        this.headers.add(Pair.of("X-WNS-Cache-Policy", enabled ? "cache" : "no-cache"));
        return (A)this;
    }
    
    /**
     * Sets the X-WNS-RequestForStatus property.
     * 
     * Specifies whether the response should include the device status and WNS connection status.
     * 
     * The default value is false.
     *
     * @param enabled if true specifies that the response should include the device status and WNS connection status. 
     *                else, do not return the device status and notification status.
     * @return  this
     */
    public A requestForStatus(boolean enabled) {
        this.headers.add(Pair.of("X-WNS-RequestForStatus", enabled ? "true" : "false"));
        return (A)this;
    }
        
    /**
     * Sets the notification channel URI that the registered callback message
     * will be sent to.
     *
     * When using an authenticated web service, this parameter is required.
     *
     * @param callbackUri   the notification channel URI
     * @return  this
     */
    public A callbackUri(String callbackUri) {
        this.headers.add(Pair.of("X-CallbackURI", callbackUri));
        return (A)this;
    }

    /**
     * Sets the notification body content type
     *
     * @param contentType   the content type of the body
     * @return  this
     */
    protected A contentType(String contentType) {
        this.headers.add(Pair.of("Content-Type", contentType));
        return (A)this;
    }

    protected abstract int deliveryValueOf(DeliveryClass delivery);

    public abstract B build();
}
