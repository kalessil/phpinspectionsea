package com.kalessil.phpStorm.phpInspectionsEA.options;

import com.kalessil.phpStorm.phpInspectionsEA.gui.PrettyListControl;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
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
        optionsPanel = new JPanel(new MigLayout());
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

        @Nullable
        private Runnable onChange;

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

                    if (onChange != null) {
                        onChange.run();
                    }
                });

                optionsPanel.add(radioButton, "wrap");
                buttonGroup.add(radioButton);
            }
        }

        public void onChange(@NotNull final Runnable runnable) {
            onChange = runnable;
        }
    }
}
