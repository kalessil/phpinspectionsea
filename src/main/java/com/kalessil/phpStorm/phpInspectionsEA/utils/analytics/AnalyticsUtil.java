package com.kalessil.phpStorm.phpInspectionsEA.utils.analytics;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateSettings;
import org.apache.http.client.fluent.Request;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

final public class AnalyticsUtil {
    private final static Set<String> stopList = new HashSet<>();
    static {
        /* ugly, but: PhpStorm 2016 compatibility + reducing crash-reports rate */
        stopList.add("com.jetbrains.php.lang.parser.PhpParserException");
        stopList.add("com.intellij.openapi.diagnostic.RuntimeExceptionWithAttachments");
        stopList.add("com.intellij.openapi.progress.ProcessCanceledException");
        stopList.add("com.intellij.util.indexing.StorageException");
        stopList.add("com.intellij.psi.stubs.UpToDateStubIndexMismatch");
        stopList.add("java.util.ConcurrentModificationException");
        stopList.add("java.lang.OutOfMemoryError");
        stopList.add("com.intellij.psi.impl.source.FileTrees");
        stopList.add("OpenapiEquivalenceUtil.java");
        stopList.add("OpenapiResolveUtil.java");
    }

    final static private String COLLECTOR_ID    = "UA-16483983-8";
    final static private String COLLECTOR_URL   = "https://www.google-analytics.com/collect"; /* or /debug/collect */
    private static final String pluginNamespace = "com.kalessil.phpStorm.phpInspectionsEA";

    public static void registerLoggedException(@Nullable String version, @Nullable String uuid, @Nullable Throwable error) {
        if (error != null) {
            /* ignore IO-errors, that's not something we can handle */
            final Throwable cause = error.getCause();
            if (stopList.contains(error.getClass().getName()) || error instanceof IOException || cause instanceof IOException) {
                return;
            }

            /* report plugin failure location and trace top: to understand is it internals or the plugin */
            final StackTraceElement[] stackTrace  = error.getStackTrace();
            final List<StackTraceElement> related = Arrays.stream(stackTrace)
                    .filter(element -> element.getClassName().contains(pluginNamespace))
                    .collect(Collectors.toList());
            if (!related.isEmpty()) {
                final StackTraceElement entryPoint = related.get(0);
                if (!stopList.contains(entryPoint.getFileName()) && !stopList.contains(stackTrace[0].getClassName())) {
                    final String description = String.format(
                        "[%s:%s@%s] %s::%s#%s: %s|%s",
                        entryPoint.getFileName(),
                        entryPoint.getLineNumber(),
                        version,
                        stackTrace[0].getClassName(),
                        stackTrace[0].getMethodName(),
                        stackTrace[0].getLineNumber(),
                        error.getMessage(),
                        error.getClass().getName()
                    );
                    invokeExceptionReporting(uuid, description);
                }
                related.clear();
            }
        }
    }

    static private void invokeExceptionReporting(@Nullable String uuid, @NotNull String description) {
        new Thread(() -> {
            /* See https://developers.google.com/analytics/devguides/collection/analyticsjs/exceptions */
            final StringBuilder payload = new StringBuilder();
            payload
                    .append("v=1")                                              // Version.
                    .append("&tid=").append(COLLECTOR_ID)                       // Tracking ID / Property ID.
                    .append("&cid=").append(uuid)                               // Anonymous Client ID.
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

    public static void registerPluginEvent(@NotNull EAUltimateSettings source, @NotNull String action, @NotNull String eventValue) {
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
