package com.kalessil.phpStorm.phpInspectionsEA.options;

import com.kalessil.phpStorm.phpInspectionsEA.gui.PrettyListControl;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    private OptionsComponent() {
        optionsPanel = new JPanel(new MigLayout());
    }

    @NotNull
    public static JPanel create(@NotNull final Consumer<OptionsComponent> delegateBuilder) {
        final OptionsComponent component = new OptionsComponent();
        delegateBuilder.accept(component);

        return component.optionsPanel;
    }

    public void addCheckbox(@NotNull final String label, final boolean defaultValue, @NotNull final Consumer<Boolean> updateConsumer) {
        final AbstractButton createdCheckbox = new JCheckBox(label, defaultValue);
        createdCheckbox.addItemListener((itemEvent) -> updateConsumer.accept(createdCheckbox.isSelected()));
        optionsPanel.add(createdCheckbox, "wrap");
    }

    public void addList(@NotNull final String label, @NotNull final List<String> items, @NotNull final Runnable updater) {
        optionsPanel.add(new JLabel(label), "wrap");
        optionsPanel.add((new PrettyListControl(items) {
            @Override
            protected void fireContentsChanged() {
                updater.run();
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
        private final ButtonGroup buttonGroup = new ButtonGroup();

        List<RadioOption> radioOptions = new ArrayList<>();

        @Nullable
        private RadioOption selectedOption;

        public void addOption(@NotNull final String label, final boolean defaultValue, @NotNull final Consumer<Boolean> updateConsumer) {
            final RadioOption newOption = new RadioOption(label, defaultValue, updateConsumer);
            radioOptions.add(newOption);

            if (defaultValue) {
                selectedOption = newOption;
            }
        }

        private class RadioOption {
            @NotNull
            final JRadioButton radioButton;

            @NotNull
            final Consumer<Boolean> updateConsumer;

            RadioOption(@NotNull final String label, final boolean defaultValue, @NotNull final Consumer<Boolean> updateConsumer) {
                this.updateConsumer = updateConsumer;

                radioButton = new JRadioButton(label, defaultValue);
                radioButton.addItemListener((itemEvent) -> {
                    final boolean isSelected = radioButton.isSelected();

                    if ((selectedOption != null) &&
                        (selectedOption != this)) {
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
