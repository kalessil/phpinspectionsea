package com.kalessil.phpStorm.phpInspectionsEA.gui;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.DoubleClickListener;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

public class PrettyListControl {
    static private String dialogHint = "Adding custom debug function";
    static private String dialogTitle = "Example formats: function_name, \\Full\\Namespace\\Class::method     ";

    private PrettyListModel model;
    private JBList list;

    public PrettyListControl(List<String> list) {
        this.model = new PrettyListModel(list);

        this.list = new JBList(this.model);
        this.list.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel textContainer = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                textContainer.setText(model.getElementAt(index));
                return textContainer;
            }
        });
    }

    public JPanel getComponent() {
        final JPanel listComponent = new JPanel(new BorderLayout());
        listComponent.add(new JLabel(""));

        // handle add button
        listComponent
            .add(ToolbarDecorator.createDecorator(this.list)
            .setAddAction(new AnActionButtonRunnable() {
                public void run(AnActionButton addButton) {
                    String defaultValue = "";
                    String newEntry = (String) JOptionPane.showInputDialog(null, dialogTitle, dialogHint, JOptionPane.PLAIN_MESSAGE, null, null, defaultValue);
                    if (!StringUtil.isEmpty(newEntry)) {
                        model.addElement(newEntry.trim());
                        model.fireContentsChanged();
                    }
                }
            })
            // handle edit button
            .setEditAction(new AnActionButtonRunnable() {
                public void run(AnActionButton editButton) {
                    doEditEntry();
                }
            })
            // handle remove button
            .setRemoveAction(new AnActionButtonRunnable() {
                public void run(AnActionButton removeButton) {
                    //noinspection deprecation
                    for (Object selectedValue : list.getSelectedValues()) {
                        model.removeElement((String) selectedValue);
                    }

                    model.fireContentsChanged();
                }
            })
            // handle positioning adjustments
            .setMoveUpAction(new AnActionButtonRunnable() {
                public void run(AnActionButton upButton) {
                    rearrange(false);
                }
            }).setMoveDownAction(new AnActionButtonRunnable() {
                public void run(AnActionButton downButton) {
                    rearrange(true);
                }
            })
        .createPanel());

        // handle double clicks
        (new DoubleClickListener() {
            protected boolean onDoubleClick(MouseEvent var1) {
                PrettyListControl.this.doEditEntry();
                return true;
            }
        }).installOn(this.list);
        return listComponent;
    }

    private void rearrange(boolean moveDown) {
        int[] selectedIndices = this.list.getSelectedIndices();
        if (selectedIndices.length != 0) {
            int increment = moveDown ? 1 : -1;
            this.list.removeSelectionInterval(0, this.model.getSize() - 1);
            int boundary = moveDown ? selectedIndices[selectedIndices.length - 1] : 0;

            while (true) {
                if (moveDown) {
                    if (boundary < 0) {
                        break;
                    }
                } else if (boundary >= selectedIndices.length) {
                    break;
                }

                int indexToMove = selectedIndices[boundary];
                String valueToMove = this.model.getElementAt(indexToMove);

                this.model.setElementAt(indexToMove, this.model.getElementAt(indexToMove + increment));
                this.model.setElementAt(indexToMove + increment, valueToMove);
                this.list.addSelectionInterval(indexToMove + increment, indexToMove + increment);

                boundary -= increment;
            }

            boundary = moveDown ? this.list.getMaxSelectionIndex() : this.list.getMinSelectionIndex();
            Rectangle cellBounds = this.list.getCellBounds(boundary, boundary);
            if (cellBounds != null) {
                this.list.scrollRectToVisible(cellBounds);
            }
        }
    }

    private void doEditEntry() {
        final String selectedValue = (String) this.list.getSelectedValue();
        final int selectedPosition = this.model.indexOf(selectedValue);

        if (null != selectedValue) {
            String newValue = (String) JOptionPane.showInputDialog(null, dialogTitle, dialogHint, JOptionPane.PLAIN_MESSAGE, null, null, selectedValue);
            if (!StringUtil.isEmpty(newValue)) {
                this.model.setElementAt(selectedPosition, newValue.trim());
                this.model.fireContentsChanged();
            }
        }
    }

    // list prototype
    private class PrettyListModel extends AbstractListModel {
        private List<String> entries;

        private PrettyListModel(List<String> entries) {
            this.entries = entries;
        }

        public void addElement (String element) {
            this.entries.add(element);
        }

        public void removeElement (String element) {
            if (this.entries.contains(element)) {
                this.entries.remove(element);
            }
        }

        public int getSize() {
            return this.entries.size();
        }

        @Nullable
        public String getElementAt(int position) {
            return position < this.entries.size() ? this.entries.get(position) : null;
        }

        public void setElementAt(int position, String element) {
            this.entries.set(position, element);
        }

        public int indexOf(String element) {
            return this.entries.indexOf(element);
        }

        public void fireContentsChanged() {
            this.fireContentsChanged(list, -1, -1);
        }
    }
}

