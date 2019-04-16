package com.kalessil.phpStorm.phpInspectionsEA.gui;

import com.intellij.ui.DoubleClickListener;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;

public class PrettyListControl {
    private final           PrettyListModel              model;
    @Nullable private final Supplier<Collection<String>> defaultItems;
    private final           String                       dialogTitle;
    private final           String                       dialogMessage;
    private final           JBList                       list;

    protected PrettyListControl(final List<String> list, @Nullable final Supplier<Collection<String>> defaultItems, final String dialogTitle, final String dialogMessage) {
        this.dialogTitle = dialogTitle;
        this.dialogMessage = dialogMessage;
        this.defaultItems = defaultItems;

        model = new PrettyListModel(list);

        this.list = new JBList(model);
        this.list.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(final JList jList, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
                final JLabel textContainer = (JLabel) super.getListCellRendererComponent(jList, value, index, isSelected, cellHasFocus);
                textContainer.setText(model.get(index));

                return textContainer;
            }
        });
    }

    public JPanel getComponent() {
        final JPanel listComponent = new JPanel(new BorderLayout());

        listComponent.add(
            ToolbarDecorator.createDecorator(list)
                            .setAddAction(addButton -> {
                                String newEntry = (String) JOptionPane.showInputDialog(null, dialogMessage, dialogTitle, JOptionPane.PLAIN_MESSAGE, null, null, "");

                                if (!StringUtils.isEmpty(newEntry)) {
                                    model.addElement(newEntry.trim());
                                }
                            })
                            .setEditAction(editButton -> doEditEntry())
                            .setRemoveAction(removeButton -> {
                                for (Object selectedValue : list.getSelectedValuesList()) {
                                    model.removeElement(selectedValue);
                                }
                            })
                            .setMoveUpAction(upButton -> rearrange(-1))
                            .setMoveDownAction(downButton -> rearrange(1))
                            .createPanel()
        );

        (new DoubleClickListener() {
            protected boolean onDoubleClick(final MouseEvent var1) {
                doEditEntry();
                return true;
            }
        }).installOn(list);

        if (defaultItems != null) {
            final JPopupMenu popupMenu = new JPopupMenu();

            final JMenuItem resetOption = new JMenuItem("Reset to default");
            resetOption.addActionListener(e -> {
                final int dialogResult =
                    JOptionPane.showConfirmDialog(list.getComponent(0),
                                                  "Are you sure you want to reset this list to its original values?", "Reset to default...", JOptionPane.YES_NO_OPTION);

                if (dialogResult == JOptionPane.YES_OPTION) {
                    model.clear();
                    model.addAll(defaultItems.get());
                }
            });
            popupMenu.add(resetOption);

            final JMenuItem mergeOption = new JMenuItem("Merge with default");
            mergeOption.addActionListener(e -> model.addAll(defaultItems.get()));
            popupMenu.add(mergeOption);

            list.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent event) {
                    if (!SwingUtilities.isRightMouseButton(event)) {
                        return;
                    }

                    popupMenu.show(event.getComponent(), event.getX(), event.getY());
                }
            });
        }

        return listComponent;
    }

    protected void fireContentsChanged() {
    }

    private void rearrange(final int moveDelta) {
        final int[] selectedIndices = list.getSelectedIndices();

        if (selectedIndices.length != 0) {
            list.clearSelection();

            if (moveDelta == 1) {
                ArrayUtils.reverse(selectedIndices);
            }

            for (final int selectedIndex : selectedIndices) {
                model.moveElement(selectedIndex, moveDelta);

                list.addSelectionInterval(selectedIndex + moveDelta, selectedIndex + moveDelta);
            }

            final int       boundary   = list.getMinSelectionIndex();
            final Rectangle cellBounds = list.getCellBounds(boundary, boundary);

            if (cellBounds != null) {
                list.scrollRectToVisible(cellBounds);
            }
        }
    }

    private void doEditEntry() {
        final String selectedValue    = (String) list.getSelectedValue();
        final int    selectedPosition = model.indexOf(selectedValue);

        if (selectedValue != null) {
            final String newValue = (String) JOptionPane.showInputDialog(null, dialogMessage, dialogTitle, JOptionPane.PLAIN_MESSAGE, null, null, selectedValue);

            if (!StringUtils.isEmpty(newValue)) {
                model.setElementAt(newValue.trim(), selectedPosition);
            }
        }
    }

    // list prototype
    private final class PrettyListModel extends DefaultListModel<String> {
        private final List<String> referenceList;
        private boolean updateImmediately = true;

        PrettyListModel(final List<String> referenceItems) {
            final Iterable originalItems = new ArrayList<>(referenceItems);

            referenceList = referenceItems;

            for (final Object item : originalItems) {
                addElement((String) item);
            }
        }

        public void addAll(final Iterable<String> defaultItemsReference) {
            updateImmediately = false;

            // We can't update ${delegate} directly, so we need avoid
            // that the original reference be updated for every added element.
            for (final String defaultItem : defaultItemsReference) {
                addElement(defaultItem);
            }

            updateImmediately = true;
            updateReference();
        }

        @Override
        protected void fireContentsChanged(final Object source, final int index0, final int index1) {
            super.fireContentsChanged(source, index0, index1);
            updateReference();
        }

        @Override
        protected void fireIntervalAdded(final Object source, final int index0, final int index1) {
            super.fireIntervalAdded(source, index0, index1);
            updateReference();
        }

        @Override
        protected void fireIntervalRemoved(final Object source, final int index0, final int index1) {
            super.fireIntervalRemoved(source, index0, index1);
            updateReference();
        }

        void moveElement(final int selectedIndex, final int moveDelta) {
            final String previous = get(selectedIndex + moveDelta);

            set(selectedIndex + moveDelta, get(selectedIndex));
            set(selectedIndex, previous);

            fireContentsChanged();
        }

        void fireContentsChanged() {
            fireContentsChanged(list, -1, -1);
        }

        void updateReference() {
            if (!updateImmediately) {
                return;
            }

            referenceList.clear();
            referenceList.addAll(new TreeSet<>(Collections.list(elements())));

            PrettyListControl.this.fireContentsChanged();
        }
    }
}
