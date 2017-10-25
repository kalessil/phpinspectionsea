package com.kalessil.phpStorm.phpInspectionsEA.license;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.LicensingFacade;
import org.jetbrains.annotations.Nullable;

final public class LicenseService {
    public boolean shouldCheckPluginLicense() {
        boolean result = false;

        final Application application = ApplicationManager.getApplication();
        if (!application.isEAP() && !application.isHeadlessEnvironment()) {
            final LicensingFacade facade = LicensingFacade.getInstance();
            if (facade != null) {
                final boolean isOssLicense
                        = facade.getLicenseRestrictionsMessages().stream().anyMatch((s) -> s.contains("open source"));
                result = (!isOssLicense && !facade.isEvaluationLicense());
            }
        }

        return result;
    }

    @Nullable
    public String ideHolder() {
        final LicensingFacade facade = LicensingFacade.getInstance();
        return facade == null ? null : facade.getLicensedToMessage();
    }
}
