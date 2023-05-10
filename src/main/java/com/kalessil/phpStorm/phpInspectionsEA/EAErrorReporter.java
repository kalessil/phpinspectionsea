package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.util.Consumer;
import com.kalessil.phpStorm.phpInspectionsEA.utils.analytics.AnalyticsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class EAErrorReporter extends ErrorReportSubmitter {
    @Override
    public @NotNull String getReportActionText() {
        return "Report to PHP Inspections EA Extended";
    }

    @Override
    public boolean submit(IdeaLoggingEvent @NotNull [] events,
                          @Nullable String additionalInfo,
                          @NotNull Component parentComponent,
                          @NotNull Consumer<? super SubmittedReportInfo> consumer) {
        if (ApplicationManager.getApplication().isUnitTestMode()) {
            return false;
        }

        final EASettings settings = EASettings.getInstance();
        if (settings.getSendCrashReports()) {
            return false;
        }

        for (IdeaLoggingEvent event : events) {
            AnalyticsUtil.registerLoggedException(
                    settings.getVersion(),
                    settings.getUuid(),
                    event.getThrowable(),
                    consumer
            );

        }
        return true;
    }
}
