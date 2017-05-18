package com.kalessil.phpStorm.phpInspectionsEA.options;

import com.kalessil.phpStorm.phpInspectionsEA.gui.PrettyListControl;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

/**
 * Helps on constructions of a normalized inspection options list for IntelliJ's IDE.
 */
public class OptionsComponent {
    @NotNull
    private final JPanel optionsPanel;

    private OptionsComponent() {
        optionsPanel = new JPanel(new MigLayout());
    }

    @NotNull
    static public JPanel create(@NotNull Consumer<OptionsComponent> delegateBuilder) {
        final OptionsComponent component = new OptionsComponent();
        delegateBuilder.accept(component);

        return component.optionsPanel;
    }

    public void addCheckbox(@NotNull String label, boolean defaultValue, @NotNull Consumer<Boolean> updateConsumer) {
        final JCheckBox createdCheckbox = new JCheckBox(label, defaultValue);
        createdCheckbox.addItemListener((itemEvent) -> updateConsumer.accept(createdCheckbox.isSelected()));
        optionsPanel.add(createdCheckbox, "wrap");
    }

    public void addList(@NotNull String label, @NotNull List<String> items, @NotNull Runnable updater) {
        optionsPanel.add(new JLabel(label), "wrap");
        optionsPanel.add((new PrettyListControl(items) {
            @Override
            protected void fireContentsChanged() {
                updater.run();
                super.fireContentsChanged();
            }
        }).getComponent(), "pushx, growx");
    }

    public void delegateRadioCreation(@NotNull Consumer<RadioComponent> delegate) {
        delegate.accept(new RadioComponent());
    }

    public class RadioComponent {
        @NotNull
        final List<RadioOption> radioOptions = new ArrayList<>();

        @Nullable
        private RadioOption selectedOption;

        public void addOption(@NotNull String label, boolean defaultValue, @NotNull Consumer<Boolean> updateConsumer) {
            final RadioOption newOption = new RadioOption(label, defaultValue, updateConsumer);
            radioOptions.add(newOption);
            if (defaultValue) {
                selectedOption = newOption;
            }
        }

        private class RadioOption {
            @NotNull
            private final JCheckBox checkbox;

            @NotNull
            private final Consumer<Boolean> updateConsumer;

            RadioOption(@NotNull String label, boolean defaultValue, @NotNull Consumer<Boolean> updateConsumer) {
                this.updateConsumer = updateConsumer;

                checkbox = new JCheckBox(label, defaultValue);
                checkbox.addItemListener((itemEvent) -> {
                    final boolean isSelected = checkbox.isSelected();
                    if (selectedOption != null && selectedOption != this) {
                        selectedOption.setSelected(false);
                    }
                    updateConsumer.accept(isSelected);
                    selectedOption = isSelected ? this : null;
                });

                optionsPanel.add(checkbox, "wrap");
            }

            private void setSelected(boolean isSelected) {
                checkbox.setSelected(isSelected);
                updateConsumer.accept(isSelected);
            }
        }
    }
}
