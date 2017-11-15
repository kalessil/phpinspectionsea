package com.kalessil.phpStorm.phpInspectionsEA.license;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.LicensingFacade;
import com.kalessil.phpStorm.phpInspectionsEA.EAApplicationComponent;
import com.wyday.turboactivate.BoolRef;
import com.wyday.turboactivate.IsGenuineResult;
import com.wyday.turboactivate.TurboActivate;
import com.wyday.turboactivate.TurboActivateException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.HashMap;

/* Based on https://wyday.com/limelm/help/using-turboactivate-with-java/ */
final public class LicenseService {
    private int trialDaysRemaining = 0;
    private int licenseDaysRemaining = 0;

    @Nullable
    private Boolean shouldCheckLicense = null;

    @NotNull
    private Boolean shouldAllowUsage   = false;

    @Nullable
    private TurboActivate client;

    public boolean shouldCheckPluginLicense() {
        if (this.shouldCheckLicense == null) {
            boolean result = false;
            final Application application = ApplicationManager.getApplication();
            if (!application.isHeadlessEnvironment()) {
                final LicensingFacade facade = LicensingFacade.getInstance();
                result = application.isEAP() || (facade != null && !facade.isEvaluationLicense());
            }
            this.shouldCheckLicense = result;
            shouldAllowUsage        = !result || shouldAllowUsage;
        }
        return this.shouldCheckLicense;
    }

    public void initializeClient() throws IOException, URISyntaxException, TurboActivateException {
        final URL binaries = EAApplicationComponent.class.getResource("/TurboActivate/");
        if (binaries == null) {
            throw new RuntimeException("Licensing related resources are missing.");
        }

        final Path pluginFolder      = Paths.get(System.getProperty("user.home") + "/.ea-ultimate/");
        final Path tempFolder        = Files.createTempDirectory(pluginFolder, "bin-").toAbsolutePath();
        final String[] sourceDetails = binaries.toURI().toString().split("!");
        final FileSystem pluginJarFs = FileSystems.newFileSystem(URI.create(sourceDetails[0]), new HashMap<>());
        Files.walk(pluginJarFs.getPath(sourceDetails[1])).forEach(sourceFile -> {
            try {
                Files.copy(
                    sourceFile,
                    tempFolder.resolve(tempFolder.toString() + File.separator + sourceFile.toString()),
                    StandardCopyOption.COPY_ATTRIBUTES
                );
            } catch (Throwable copyFailure) {
                throw new RuntimeException(copyFailure);
            }
        });
        pluginJarFs.close();
        /* TODO: cleanup pluginFolder - older than 3 days, ignore failures */

        this.client = new TurboActivate("2d65930359df9afb6f9a54.36732074", tempFolder.toString() + "/TurboActivate/");
    }

    boolean isClientInitialized() {
        return this.client != null;
    }

    public boolean shouldAllowUsage() {
        return shouldAllowUsage;
    }

    public boolean isActiveLicense() throws TurboActivateException {
        final IsGenuineResult result = client.IsGenuine(90, 14, true, false);
        final boolean isGenuine      = result == IsGenuineResult.Genuine || result == IsGenuineResult.GenuineFeaturesChanged;
        if (isGenuine) {
            licenseDaysRemaining = client.GenuineDays(90, 14, new BoolRef());
        }
        /* positive when  check succeeded or network error occurred and license activated */
        final boolean isActive = isGenuine || (result == IsGenuineResult.InternetError && client.IsActivated());
        shouldAllowUsage       = isActive || shouldAllowUsage;
        return isActive;
    }

    public boolean isActivatedLicense() throws TurboActivateException {
        return client.IsActivated();
    }

    private boolean isTrialLicense() {
        boolean result = true;
        try {
            trialDaysRemaining = client.TrialDaysRemaining(TurboActivate.TA_USER | TurboActivate.TA_VERIFIED_TRIAL);
        } catch (TurboActivateException failure) {
            result = false;
        }
        return result;
    }

    public boolean isActiveTrialLicense() {
        final boolean isActive = this.isTrialLicense() && this.trialDaysRemaining > 0;
        shouldAllowUsage       = isActive || shouldAllowUsage;
        return isActive;
    }

    int getTrialDaysRemaining() {
        return this.trialDaysRemaining;
    }

    boolean applyLicenseKey(@Nullable String key, @NotNull StringBuilder errorDetails) {
        boolean result;
        try {
            result = client.CheckAndSavePKey(key, TurboActivate.TA_USER);
            if (result) {
                client.Activate(this.getLicenseHolder());
                licenseDaysRemaining = client.GenuineDays(90, 14, new BoolRef());
                shouldAllowUsage     = true;
            } else {
                errorDetails.append(String.format("key '%s' seems has a typo", key));
            }
        } catch (TurboActivateException activationFailed) {
            final String message = activationFailed.getMessage();
            errorDetails.append(message == null ? activationFailed.getClass().getName() : message);
            result = false;
        }
        return result;
    }

    boolean deactivateLicenseKey(@NotNull StringBuilder key, @NotNull StringBuilder errorDetails) {
        boolean result;
        try {
            key.append(client.GetPKey());
            client.Deactivate(true);
            result           = true;
            shouldAllowUsage = false;
        } catch (TurboActivateException|UnsupportedEncodingException deactivationFailed) {
            final String message = deactivationFailed.getMessage();
            errorDetails.append(message == null ? deactivationFailed.getClass().getName() : message);
            result = false;
        }
        return result;
    }

    boolean startTrial(@NotNull StringBuilder errorDetails) {
        boolean result = true;
        try {
            client.UseTrial(TurboActivate.TA_USER | TurboActivate.TA_VERIFIED_TRIAL, this.getLicenseHolder());
            trialDaysRemaining = client.TrialDaysRemaining(TurboActivate.TA_USER | TurboActivate.TA_VERIFIED_TRIAL);
            shouldAllowUsage   = trialDaysRemaining > 0;
        } catch (TurboActivateException activationFailed) {
            final String message = activationFailed.getMessage();
            errorDetails.append(message == null ? activationFailed.getClass().getName() : message);
            result = false;
        }
        return result;
    }

    @Nullable
    private String getLicenseHolder() {
        String result = null;
        /* EAPs doesn't have license holder information */
        if (!ApplicationManager.getApplication().isEAP()) {
            final LicensingFacade facade = LicensingFacade.getInstance();
            result = facade == null ? null : facade.getLicensedToMessage();
        }
        return result;
    }
}
