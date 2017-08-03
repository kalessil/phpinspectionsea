package com.kalessil.phpStorm.phpInspectionsEA.gui;

import com.intellij.ui.DoubleClickListener;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PrettyListControl {
    private final PrettyListModel model;
    private final String          dialogTitle;
    private final String          dialogMessage;
    private final JBList          list;

    protected PrettyListControl(final List<String> list, final String dialogTitle, final String dialogMessage) {
        this.dialogTitle = dialogTitle;
        this.dialogMessage = dialogMessage;

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

        PrettyListModel(final List<String> referenceItems) {
            final Iterable originalItems = new ArrayList<>(referenceItems);

            referenceList = referenceItems;

            for (final Object item : originalItems) {
                addElement((String) item);
            }
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
            referenceList.clear();
            referenceList.addAll(Collections.list(elements()));

            PrettyListControl.this.fireContentsChanged();
        }
    }
}
