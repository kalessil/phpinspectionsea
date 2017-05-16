package com.kalessil.phpStorm.phpInspectionsEA.options;

import com.kalessil.phpStorm.phpInspectionsEA.gui.PrettyListControl;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Helps on constructions of a normalized inspection options list for IntelliJ's IDE.
 */
public class OptionsComponent {
    /**
     * Stores the options panel.
     */
    private final JPanel optionsPanel;

    /**
     * Construct the OptionsComponent.
     */
    private OptionsComponent() {
        optionsPanel = new JPanel();
        optionsPanel.setLayout(new MigLayout());
    }

    /**
     * Creates a new OptionsComponent.
     *
     * @param componentDefiner The component definer, that will create the option fields.
     */
    static public JPanel create(@NotNull final Consumer<OptionsComponent> componentDefiner) {
        // We should create a OptionsComponent that offer the features to create fields.
        // Then the component definer will use that new instance to create them.
        final OptionsComponent component = new OptionsComponent();
        componentDefiner.accept(component);

        // Returns only the JPanel component.
        return component.optionsPanel;
    }

    /**
     * Creates a Checkbox.
     *
     * @param fieldTitle     The title of the field on options panel.
     * @param defaultValue   The default or current field value stored on inspection.
     * @param updateConsumer The update consumer (called after item change).
     */
    public JCheckBox createCheckbox(final String fieldTitle, final boolean defaultValue, final Consumer<Boolean> updateConsumer) {
        final JCheckBox createdCheckbox = new JCheckBox(fieldTitle, defaultValue);
        createdCheckbox.addItemListener((itemEvent) -> updateConsumer.accept(createdCheckbox.isSelected()));

        optionsPanel.add(createdCheckbox, "wrap");

        return createdCheckbox;
    }

    /**
     * Creates a new Radio component.
     *
     * @param componentDefiner The component definer, that will create the option radios.
     */
    public void createRadio(@NotNull final Consumer<RadioComponent> componentDefiner) {
        final RadioComponent radioComponent = new RadioComponent();
        componentDefiner.accept(radioComponent);
    }

    /**
     * Creates a List.
     *
     * @param fieldTitle  The title of this field on panel.
     * @param listItems   The default or current list items.
     * @param listUpdater The event called after a modification on list items.
     */
    public void createList(final String fieldTitle, @NotNull final List<String> listItems, @NotNull final Runnable listUpdater) {
        optionsPanel.add(new JLabel(fieldTitle), "wrap");
        optionsPanel.add((new PrettyListControl(listItems) {
            protected void fireContentsChanged() {
                listUpdater.run();
                super.fireContentsChanged();
            }
        }).getComponent(), "pushx, growx");
    }

    /**
     * Create the checkboxes used as radios.
     */
    public class RadioComponent {
        /**
         * Store all Radio Elements handled by this Radio Component.
         */
        ArrayList<Widget> widgets = new ArrayList<>();

        /**
         * Store the current selected Radio.
         */
        @Nullable
        private Widget currentSelection;

        /**
         * @param fieldTitle     The title of the field on options panel.
         * @param defaultValue   The default or current field value stored on inspection.
         * @param updateConsumer The update consumer (called after item change).
         */
        public void createOption(final String fieldTitle, final boolean defaultValue, final Consumer<Boolean> updateConsumer) {
            final Widget createWidget = new Widget(fieldTitle, defaultValue, updateConsumer);
            widgets.add(createWidget);

            if (defaultValue) {
                currentSelection = createWidget;
            }
        }

        /**
         * Handle a JCheckBox component and it Update Consumer.
         */
        private class Widget {
            /**
             * Stores the JCheckBox component related to the Widget.
             */
            final JCheckBox checkbox;

            /**
             * Stores the Update Consumer related to the Widget.
             */
            final Consumer<Boolean> updateConsumer;

            /**
             * Constructs the Widget.
             *
             * @param fieldTitle     The title of the field on options panel.
             * @param defaultValue   The default or current field value stored on inspection.
             * @param updateConsumer The update consumer (called after item change).
             */
            Widget(final String fieldTitle, final boolean defaultValue, final Consumer<Boolean> updateConsumer) {
                this.updateConsumer = updateConsumer;

                checkbox = new JCheckBox(fieldTitle, defaultValue);
                checkbox.addItemListener((itemEvent) -> {
                    final boolean isSelected = checkbox.isSelected();

                    if (currentSelection != null && currentSelection != this) {
                        currentSelection.setSelected(false);
                    }

                    updateConsumer.accept(isSelected);
                    currentSelection = isSelected ? this : null;
                });

                optionsPanel.add(checkbox, "wrap");
            }

            /**
             * Update the checkbox "checked" status related to Widget and call the Consumer.
             *
             * @param isSelected Indicate the new status.
             */
            final void setSelected(final boolean isSelected) {
                checkbox.setSelected(isSelected);
                updateConsumer.accept(isSelected);
            }
        }
    }
}
