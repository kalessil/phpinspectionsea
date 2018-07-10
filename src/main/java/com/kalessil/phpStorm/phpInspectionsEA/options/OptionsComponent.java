package com.kalessil.phpStorm.phpInspectionsEA.options;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.options.ex.Settings;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.SeparatorFactory;
import com.kalessil.phpStorm.phpInspectionsEA.gui.PrettyListControl;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) David Rodrigues <david.proweb@gmail.com>
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

/**
 * Helps on constructions of a normalized inspection options list for IntelliJ's IDE.
 */
public final class OptionsComponent {
    @NotNull
    private final JPanel optionsPanel;

    @NotNull
    public static JPanel create(@NotNull final Consumer<OptionsComponent> delegateBuilder) {
        final OptionsComponent component = new OptionsComponent();
        delegateBuilder.accept(component);

        return component.optionsPanel;
    }

    private OptionsComponent() {
        optionsPanel = new JPanel(new MigLayout("fillx"));
    }

    private OptionsComponent(@NotNull final String label) {
        optionsPanel = new JPanel(new MigLayout("fillx"));
        //noinspection AbsoluteAlignmentInUserInterface
        optionsPanel.add(SeparatorFactory.createSeparator(label, null), BorderLayout.NORTH);
    }

    public void addCheckbox(
        @NotNull final String label,
        final boolean defaultValue,
        @NotNull final Consumer<Boolean> updateConsumer
    ) {
        final AbstractButton createdCheckbox = new JCheckBox(label, defaultValue);
        createdCheckbox.addItemListener((itemEvent) -> updateConsumer.accept(createdCheckbox.isSelected()));
        optionsPanel.add(createdCheckbox, "wrap");
    }

    public void addSpinner(
        @NotNull final String label,
        final Integer defaultValue,
        @NotNull final Consumer<Integer> updateConsumer
    ) {
        final JSpinner createdSpinner = new JSpinner(new SpinnerNumberModel(defaultValue, 0, null, 1));
        createdSpinner.addChangeListener((itemEvent) -> updateConsumer.accept((Integer) createdSpinner.getValue()));

        optionsPanel.add(new JLabel(label), "");
        optionsPanel.add(createdSpinner, "pushx, growx");
    }

    public void addList(
        @NotNull final String label,
        @NotNull final List<String> items,
        @Nullable final Supplier<Collection<String>> defaultItems,
        @Nullable final Runnable updater,
        @NotNull final String dialogTitle,
        @NotNull final String dialogMessage
    ) {
        optionsPanel.add(new JLabel(label), "wrap");
        optionsPanel.add((new PrettyListControl(items, defaultItems, dialogTitle, dialogMessage) {
            @Override
            protected void fireContentsChanged() {
                if (updater != null) {
                    updater.run();
                }

                super.fireContentsChanged();
            }
        }).getComponent(), "pushx, growx");
    }

    private void addHyperlink(
        @NotNull final String label,
        @NotNull final Consumer<HyperlinkEvent> consumer
    ) {
        final HyperlinkLabel createdHyperlink = new HyperlinkLabel(label);
        createdHyperlink.addHyperlinkListener(consumer::accept);

        optionsPanel.add(createdHyperlink);
    }

    public void addHyperlink(
        @NotNull final String label,
        @NotNull final Class configurableClass
    ) {
        addHyperlink(label, hyperlinkEvent ->
            DataManager.getInstance().getDataContextFromFocus().doWhenDone((com.intellij.util.Consumer<DataContext>) context -> {
                if (context != null) {
                    final Settings settings = Settings.KEY.getData(context);
                    if (settings != null) {
                        settings.select(settings.find(configurableClass));
                    }
                }
            })
        );
    }

    public void addPanel(
        @NotNull final String label,
        @NotNull final Consumer<OptionsComponent> consumer
    ) {
        final OptionsComponent optionsComponent = new OptionsComponent(label);
        consumer.accept(optionsComponent);
        optionsPanel.add(optionsComponent.optionsPanel, "wrap, growx");
    }

    public void delegateRadioCreation(@NotNull final Consumer<RadioComponent> delegate) {
        final RadioComponent radioComponent = new RadioComponent();
        delegate.accept(radioComponent);

        if ((radioComponent.selectedOption == null) &&
            (radioComponent.radioOptions.size() >= 1)) {
            final RadioComponent.RadioOption firstWidget = radioComponent.radioOptions.get(0);
            firstWidget.radioButton.setSelected(true);
            firstWidget.updateConsumer.accept(true);
        }
    }

    public class RadioComponent {
        List<RadioOption> radioOptions = new ArrayList<>();
        private final ButtonGroup buttonGroup = new ButtonGroup();

        @Nullable
        private RadioOption selectedOption;

        public void addOption(
            @NotNull final String label,
            final boolean defaultValue,
            @NotNull final Consumer<Boolean> updateConsumer
        ) {
            final RadioOption newOption = new RadioOption(label, defaultValue, updateConsumer);
            radioOptions.add(newOption);

            if (defaultValue) {
                selectedOption = newOption;
            }
        }

        private class RadioOption {
            @NotNull final JRadioButton radioButton;

            @NotNull final Consumer<Boolean> updateConsumer;

            RadioOption(
                @NotNull final String label,
                final boolean defaultValue,
                @NotNull final Consumer<Boolean> updateConsumer
            ) {
                this.updateConsumer = updateConsumer;

                radioButton = new JRadioButton(label, defaultValue);
                radioButton.addItemListener((itemEvent) -> {
                    final boolean isSelected = radioButton.isSelected();

                    if ((selectedOption != null) &&
                        !Objects.equals(selectedOption, this)) {
                        selectedOption.updateConsumer.accept(false);
                    }

                    updateConsumer.accept(isSelected);
                    selectedOption = isSelected ? this : null;
                });

                optionsPanel.add(radioButton, "wrap");
                buttonGroup.add(radioButton);
            }
        }
    }
}
