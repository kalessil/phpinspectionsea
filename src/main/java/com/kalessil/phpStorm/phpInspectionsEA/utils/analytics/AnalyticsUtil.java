package com.kalessil.phpStorm.phpInspectionsEA.utils.analytics;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

import com.kalessil.phpStorm.phpInspectionsEA.EASettings;
import org.apache.http.client.fluent.Request;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

final public class AnalyticsUtil {
    final static private String COLLECTOR_ID    = "UA-16483983-8";
    final static private String COLLECTOR_URL   = "https://www.google-analytics.com/collect"; /* or /debug/collect */
    private static final String pluginNamespace = "com.kalessil.phpStorm.phpInspectionsEA";

    public static void registerPluginException(@NotNull EASettings source, @Nullable Throwable exception) {
        if (exception != null) {
            /* ignore IO-errors, that's not something we can handle */
            final Throwable cause = exception.getCause();
            if (exception instanceof IOException || cause instanceof IOException) {
                return;
            }

            /* report plugin failure location and trace top: to understand is it internals or the plugin */
            final StackTraceElement[] stackTrace  = exception.getStackTrace();
            final List<StackTraceElement> related = Arrays.stream(stackTrace)
                    .filter(element -> element.getClassName().contains(pluginNamespace))
                    .collect(Collectors.toList());
            if (!related.isEmpty()) {
                final StackTraceElement rootCause = related.get(0);
                final String description          = String.format(
                        "[%s:%s@%s] %s.%s#%s: %s",
                        rootCause.getFileName(),
                        rootCause.getLineNumber(),
                        source.getVersion(),
                        stackTrace[0].getClassName(),
                        stackTrace[0].getMethodName(),
                        stackTrace[0].getLineNumber(),
                        exception.getMessage()
                );
                related.clear();

                new Thread(() -> {
                    /* See https://developers.google.com/analytics/devguides/collection/analyticsjs/exceptions */
                    final StringBuilder payload = new StringBuilder();
                    payload
                            .append("v=1")                                              // Version.
                            .append("&tid=").append(COLLECTOR_ID)                       // Tracking ID / Property ID.
                            .append("&cid=").append(source.getUuid())                   // Anonymous Client ID.
                            .append("&t=exception")                                     // Exception hit type.
                            .append("&exd=").append(description)                        // Exception description.
                            .append("&exf=1")                                           // Exception is fatal?
                    ;

                    try {
                        Request.Post(COLLECTOR_URL)
                                .bodyByteArray(payload.toString().getBytes())
                                .connectTimeout(3000)
                                .execute();
                    } catch (Exception failed) {
                        /* we do nothing here - this happens in background and not mission critical */
                    }
                }).start();
            }
        }
    }

    public static void registerPluginEvent(@NotNull EASettings source, @NotNull String action, @NotNull String eventValue) {
        new Thread(() -> {
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
            } catch (Exception failed) {
                /* we do nothing here - this happens in background and not mission critical */
            }
        }).start();
    }
}
