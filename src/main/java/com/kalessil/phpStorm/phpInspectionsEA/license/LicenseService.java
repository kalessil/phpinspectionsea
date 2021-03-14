package com.kalessil.phpStorm.phpInspectionsEA.license;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.ui.LicensingFacade;
import com.intellij.util.net.HttpConfigurable;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
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
import java.util.Arrays;
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

    @Nullable
    private String getProxy() {
        final HttpConfigurable proxySettings = HttpConfigurable.getInstance();
        final String host                    = proxySettings.PROXY_HOST;
        if (! proxySettings.USE_HTTP_PROXY || host == null || host.isEmpty()) {
            return null;
        }

        String credentials = "";
        final String login = proxySettings.getProxyLogin();
        if (login != null && ! login.isEmpty()) {
            final String password = proxySettings.getPlainProxyPassword();
            credentials = login + (password == null ? "" : (':' + password)) + '@';
        }

        return String.format("http://%s%s:%s/", credentials, host, proxySettings.PROXY_PORT);
    }

    public boolean shouldCheckPluginLicense() {
        if (this.shouldCheckLicense == null) {
            this.shouldCheckLicense = ! ApplicationManager.getApplication().isUnitTestMode();
            this.shouldAllowUsage   = ! this.shouldCheckLicense;
        }
        return this.shouldCheckLicense;
    }

    public void initializeClient() throws IOException, URISyntaxException, TurboActivateException {
        final URL binaries = EAUltimateApplicationComponent.class.getClassLoader().getResource("TurboActivate/TurboActivate.dat");
        if (binaries == null) {
            throw new RuntimeException("Licensing related resources are missing.");
        }

        final String latest = "php-inspections-ea-ultimate-20210314-1305";
        final Path location = (new File(Paths.get(PathManager.getTempPath()).toFile(), latest)).toPath().toAbsolutePath();
        final String path   = location.toString();
        if (! Files.exists(location)) {
            /* create location, extract TurboActivate resources */
            Files.createDirectory(location);
            final String[] sourceDetails        = binaries.toURI().toString().split("!");
            final String pluginJarPath          = sourceDetails[0];
            final String licensingResourcesPath = sourceDetails[1].replace("/TurboActivate.dat", "/");
            final FileSystem pluginJarFs        = FileSystems.newFileSystem(URI.create(pluginJarPath), new HashMap<>());
            Files.walk(pluginJarFs.getPath(licensingResourcesPath)).forEach(sourceFile -> {
                try {
                    Files.copy(
                            sourceFile,
                            location.resolve(path + File.separator + sourceFile.toString()),
                            StandardCopyOption.COPY_ATTRIBUTES
                    );
                } catch (final Throwable copyFailure) {
                    throw new RuntimeException(copyFailure);
                }
            });
            pluginJarFs.close();

            /* cleanup older extractions */
            final Path cleanupDirectory = location.getParent();
            final String[] directories  = cleanupDirectory.toFile().list();
            if (directories != null) {
                final String cleanupPath = cleanupDirectory.toString();
                Arrays.stream(directories)
                        .filter(name  -> ! name.equals(latest) && name.startsWith("php-inspections-ea-ultimate-"))
                        .forEach(name -> {
                            try {
                                Files.walk(Paths.get(cleanupPath + File.separator + name))
                                        .map(Path::toFile)
                                        .sorted((first, second) -> -FileUtil.compareFiles(first, second))
                                        .forEach(File::delete);
                            } catch (final Throwable deleteFailure) {
                                throw new RuntimeException(deleteFailure);
                            }
                        });
            }
        }
        this.client = new TurboActivate("2d65930359df9afb6f9a54.36732074", path + "/TurboActivate/");
    }

    boolean isClientInitialized() {
        return this.client != null;
    }

    public boolean shouldAllowUsage() {
        return shouldAllowUsage;
    }

    public boolean isActiveLicense() throws TurboActivateException {
        client.SetCustomProxy(this.getProxy());
        final IsGenuineResult result = client.IsGenuine(14, 7, true, false);
        final boolean isGenuine      = result == IsGenuineResult.Genuine || result == IsGenuineResult.GenuineFeaturesChanged;
        if (isGenuine) {
            licenseDaysRemaining = client.GenuineDays(14, 7, new BoolRef());
        }
        /* positive when check succeeded or network error occurred and license activated */
        final boolean isActive = isGenuine || (result == IsGenuineResult.InternetError && client.IsActivated());
        shouldAllowUsage       = isActive || shouldAllowUsage;
        return isActive;
    }

    public boolean isActivatedLicense() throws TurboActivateException {
        return client.IsActivated();
    }

    public boolean isTrialLicense() {
        boolean result = true;
        try {
            client.SetCustomProxy(this.getProxy());
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
                client.SetCustomProxy(this.getProxy());
                client.Activate(null);
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
            client.SetCustomProxy(this.getProxy());
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
            client.SetCustomProxy(this.getProxy());
            client.UseTrial(TurboActivate.TA_USER | TurboActivate.TA_VERIFIED_TRIAL, null);
            trialDaysRemaining = client.TrialDaysRemaining(TurboActivate.TA_USER | TurboActivate.TA_VERIFIED_TRIAL);
            shouldAllowUsage   = trialDaysRemaining > 0;
        } catch (TurboActivateException activationFailed) {
            final String message = activationFailed.getMessage();
            errorDetails.append(message == null ? activationFailed.getClass().getName() : message);
            result = false;
        }
        return result;
    }
}
