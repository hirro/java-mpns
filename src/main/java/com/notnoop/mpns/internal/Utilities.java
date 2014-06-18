/*
* Copyright 2011, Mahmood Ali.
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are
* met:
*
* * Redistributions of source code must retain the above copyright
* notice, this list of conditions and the following disclaimer.
* * Redistributions in binary form must reproduce the above
* copyright notice, this list of conditions and the following disclaimer
* in the documentation and/or other materials provided with the
* distribution.
* * Neither the name of Mahmood Ali. nor the names of its
* contributors may be used to endorse or promote products derived from
* this software without specific prior written permission.
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
package com.notnoop.mpns.internal;

import com.notnoop.mpns.DeliveryClass;
import com.notnoop.mpns.MpnsDelegate;
import com.notnoop.mpns.MpnsNotification;
import com.notnoop.mpns.MpnsResponse;
import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.slf4j.LoggerFactory;

public final class Utilities {
    private Utilities() { throw new AssertionError("Uninstantiable class"); }

    /**
     * The content type "text/xml"
     */
    public static String XML_CONTENT_TYPE = "text/xml";

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Utilities.class);

    public static ThreadSafeClientConnManager poolManager(int maxConnections) {
        ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager();
        cm.setMaxTotal(maxConnections);
        cm.setDefaultMaxPerRoute(maxConnections);

        return cm;
    }

    /**
     * Returns {@code value} if the {@code cond} is non-null; otherwise returns an empty String.
     * @param cond the condition
     * @param value the value to return if the condition is true
     * @return see description
     */
    public static String ifNonNull(Object cond, String value) {
        return cond != null ? value : "";
    }
    
    public static String xmlElement(String name, String content) {
    	return xmlElement(name, content, false);
    }
    
    public static String xmlElementClear(String name, String content) {
    	return xmlElement(name, content, true);
    }
    
    private static String xmlElement(String name, String content, boolean isClear) {
    	if( content == null || "".equals(content.trim())) {
    		return "";
    	}
    	StringBuilder sb = new StringBuilder(500);
    	sb.append("<wp:").append(name);
    	if( isClear ) {
    		sb.append(" Action=\"Clear\"");
    	}
		sb.append(">");
    	sb.append(escapeXml(content));
    	sb.append("</wp:").append(name).append(">");
    	return sb.toString();
    }

    public static String escapeXml(String value) {
        if (value == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); ++i) {
            char ch = value.charAt(i);
            switch (ch) {
            case '&': sb.append("&amp;"); break;
            case '<': sb.append("&lt;"); break;
            case '>': sb.append("&gt;"); break;
            case '"': sb.append("&quot;"); break;
            case '\'': sb.append("&apos;"); break;
            default: sb.append(ch);
            }
        }

        return sb.toString();
    }

    public static byte[] toUTF8(String content) {
        try {
            return content.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("The world is coming to an end!  No UTF-8 support");
        }
    }

    private static String headerValue(HttpResponse response, String name) {
        Header header = response.getFirstHeader(name);

        return header == null ? null: header.getValue();
    }

    private static MpnsResponse[] logicalResponses = MpnsResponse.values();
    public static MpnsResponse logicalResponseFor(HttpResponse response) {
        
        // Get the interesting header values
        final String notificationStatus = headerValue(response, "X-NotificationStatus");
        final String deviceConnectionStatus = headerValue(response, "X-DeviceConnectionStatus");
        final String subscriptionStatus = headerValue(response, "X-SubscriptionStatus");
        final int statusCode = response.getStatusLine().getStatusCode();

        for (MpnsResponse r: logicalResponses) {
            if (r.getResponseCode() != statusCode) {
                continue;
            }

            if (r.getNotificationStatus() != null
                && !r.getNotificationStatus().equals(notificationStatus)) {
                continue;
            }

            if (r.getDeviceConnectionStatus() != null
                && !r.getDeviceConnectionStatus().equals(deviceConnectionStatus)) {
                continue;
            }

            if (r.getSubscriptionStatus() != null
                && !r.getSubscriptionStatus().equals(subscriptionStatus)) {
                continue;
            }

            return r;
        }

        // Didn't find anything
        LOG.error(
                "Unmatched error code - Notification status: [{}], Connection status: [{}], Subscription status: [{}], Status code: [{}]",
                notificationStatus,
                deviceConnectionStatus,
                subscriptionStatus,
                statusCode);
        return MpnsResponse.UNDEFINED;
    }

    public static void fireDelegate(MpnsNotification message, HttpResponse response, MpnsDelegate delegate) {
        if (delegate != null) {
            MpnsResponse r = Utilities.logicalResponseFor(response);

            if (r.isSuccessful()) {
                delegate.messageSent(message, r);
            } else {
                delegate.messageFailed(message, r);
            }
        }
    }
    
    public static int getTileDelivery(DeliveryClass delivery) {
    	if( delivery == null ) {
    		delivery = DeliveryClass.IMMEDIATELY;
    	}
        switch (delivery) {
        case IMMEDIATELY:   return 1;
        case WITHIN_450:    return 11;
        case WITHIN_900:    return 21;
        default:            return 1; // IMMEDIATELY is the default
        }
    }
}
