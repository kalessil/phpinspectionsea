package com.kalessil.phpStorm.phpInspectionsEA.license;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.HyperlinkEvent;
import java.net.URL;
import java.util.function.Consumer;

public interface EaNotificationLinksHandler extends NotificationListener {
    TakeLicenseActionListener TAKE_LICENSE_ACTION_LISTENER = new TakeLicenseActionListener();

    final class TakeLicenseActionListener extends Adapter {
        private Consumer<String> callback;

        @Override
        protected void hyperlinkActivated(@NotNull Notification notification, @NotNull HyperlinkEvent event) {
            final URL url = event.getURL();
            this.callback.accept(url == null ? event.getDescription() : url.toString());
        }

        public TakeLicenseActionListener withActionCallback(@NotNull Consumer<String> callback) {
            this.callback = callback;
            return this;
        }
    }
}
