package com.kalessil.phpStorm.phpInspectionsEA.utils.analytics;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.kalessil.phpStorm.phpInspectionsEA.EASettings;
import org.apache.http.client.fluent.Request;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final public class AnalyticsUtil {
    final static private String COLLECTOR_ID        = "UA-16483983-8";
    final static private String COLLECTOR_DEBUG_URL = "https://www.google-analytics.com/debug/collect";
    final static private String COLLECTOR_URL       = "https://www.google-analytics.com/collect";

    @Nullable
    static public String lastError = null;

    public static void registerPluginEvent(@NotNull EASettings source, @NotNull String action, @NotNull String eventValue) {
        new Thread() {
            public void run() {
                /* See https://developers.google.com/analytics/devguides/collection/protocol/v1/devguide#event */
                final StringBuilder payload = new StringBuilder();
                payload
                    .append("v=1")                                              // Version.
                    .append("&tid=").append(COLLECTOR_ID)                       // Tracking ID / Property ID.
                    .append("&cid=").append(source.getUuid())                   // Anonymous Client ID.
                    .append("&t=event")                                         // Event hit type
                    .append("&ec=plugin")                                       // Event Category. Required.
                    .append("&ea=").append(action)                              // Event Action. Required.
                    .append("&el=").append(source.getVersion())                 // Event label - current version
                    .append("&ev=").append(eventValue.replaceAll("[^\\d]", "")) // Event value - oldest version as int
                ;

                try {
                    Request.Post(COLLECTOR_URL)
                        .bodyByteArray(payload.toString().getBytes())
                        .connectTimeout(3000)
                    .execute();

                    lastError = null;
                } catch (Exception failed) {
                    lastError = failed.getClass().getName() + " - " + failed.getMessage();
                }
            }
        }.start();
    }
}
