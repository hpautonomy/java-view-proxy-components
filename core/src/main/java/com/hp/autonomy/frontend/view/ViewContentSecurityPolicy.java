/*
 * (c) Copyright 2015 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.view;

import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * Utility for adding a content security policy for securing viewed documents.
 */
public class ViewContentSecurityPolicy {
    private static final String CONTENT_SECURITY_POLICY = StringUtils.join(Arrays.asList(
            // Unless another directive applies, prevent loading content
            "default-src 'none'",

            // Allow CSS, fonts, images and media (video, audio etc) to come from any domain or inline
            "font-src * 'unsafe-inline'",
            "img-src * 'unsafe-inline'",
            "style-src * 'unsafe-inline'",
            "media-src * 'unsafe-inline'",

            // Behaves like the iframe sandbox attribute, disabling potentially dangerous features such as form submission
            // Allow same origin so CSS etc can be loaded
            "sandbox allow-same-origin"
    ), "; ");

    private ViewContentSecurityPolicy() {}

    /**
     * Add content security policy headers to an HTTP response. These control what child content can be loaded from the
     * proxied document, reducing the risk of allowing users to serve arbitrary documents from the application domain.
     * @param response The HTTP response
     */
    public static void addContentSecurityPolicy(final HttpServletResponse response) {
        // We need both headers to support all browsers
        response.addHeader("Content-Security-Policy", CONTENT_SECURITY_POLICY);
        response.addHeader("X-Content-Security-Policy", CONTENT_SECURITY_POLICY);
    }
}
