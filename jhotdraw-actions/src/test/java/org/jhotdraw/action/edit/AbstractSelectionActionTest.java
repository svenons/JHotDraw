package org.jhotdraw.action.edit;

import org.junit.*;
import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.jhotdraw.api.gui.EditableComponent;

import static org.junit.Assert.*;

public class AbstractSelectionActionTest {

    // Test double: real JComponent implementing EditableComponent
    static class TestEditableComponent extends JComponent implements EditableComponent {
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private boolean selectionEmpty = true;

        @Override
        public void delete() { }

        @Override
        public void duplicate() { }

        @Override
        public void selectAll() {
            boolean old = selectionEmpty;
            selectionEmpty = false;
            pcs.firePropertyChange(SELECTION_EMPTY_PROPERTY, old, selectionEmpty);
        }

        @Override
        public void clearSelection() {
            boolean old = selectionEmpty;
            selectionEmpty = true;
            pcs.firePropertyChange(SELECTION_EMPTY_PROPERTY, old, selectionEmpty);
        }

        @Override
        public boolean isSelectionEmpty() {
            return selectionEmpty;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            pcs.addPropertyChangeListener(l);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            pcs.removePropertyChangeListener(l);
        }

        public void setSelectionEmpty(boolean empty) {
            boolean old = selectionEmpty;
            selectionEmpty = empty;
            pcs.firePropertyChange(SELECTION_EMPTY_PROPERTY, old, empty);
        }
    }

    // Concrete subclass of AbstractSelectionAction for testing
    static class ConcreteSelectionAction extends AbstractSelectionAction {
        public ConcreteSelectionAction(JComponent target) {
            super(target);
        }

        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            // Empty implementation for testing updateEnabled behavior
        }
    }


    @Test
    public void updateEnabled_reflectsTargetEnabledState() {
        // Arrange
        TestEditableComponent comp = new TestEditableComponent();
        comp.setEnabled(true);
        comp.setSelectionEmpty(false); // has selection

        ConcreteSelectionAction action = new ConcreteSelectionAction(comp);
        action.updateEnabled();

        // Assert
        assertTrue("Action should be enabled when target is enabled and has selection", 
                   action.isEnabled());
    }


    @Test
    public void updateEnabled_disablesWhenSelectionEmpty() {
        // Arrange
        TestEditableComponent comp = new TestEditableComponent();
        comp.setEnabled(true);
        comp.setSelectionEmpty(true); // empty selection

        ConcreteSelectionAction action = new ConcreteSelectionAction(comp);
        action.updateEnabled();

        // Assert: even though target is enabled, empty selection disables the action
        assertFalse("Action should be disabled when selection is empty", 
                    action.isEnabled());
    }
}