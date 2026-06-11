package org.jhotdraw.action.edit;

import org.junit.*;
import javax.swing.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.jhotdraw.api.gui.EditableComponent;

import static org.junit.Assert.*;

public class SelectAllActionTest {


    static class TestEditableComponent extends JComponent implements EditableComponent {
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private boolean selectionEmpty = true;
        private boolean selectAllCalled = false;

        @Override
        public void delete() { }

        @Override
        public void duplicate() { }

        @Override
        public void selectAll() {
            selectAllCalled = true;
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

        public boolean wasSelectAllCalled() {
            return selectAllCalled;
        }
    }

    private ActionEvent fakeEvent() {
        return new ActionEvent(new Object(), ActionEvent.ACTION_PERFORMED, "");
    }

    @Test
    public void selectAll_delegatesToEditableComponent_targetNonNull() {
        // Arrange
        TestEditableComponent comp = new TestEditableComponent();
        comp.setEnabled(true);

        SelectAllAction action = new SelectAllAction(comp);

        // Act
        action.actionPerformed(fakeEvent());

        // Assert
        assertTrue("selectAll() should have been called", comp.wasSelectAllCalled());
    }

    @Test
    public void selectAll_doesNothing_whenTargetDisabled() {
        // Arrange
        TestEditableComponent comp = new TestEditableComponent();
        comp.setEnabled(false);

        SelectAllAction action = new SelectAllAction(comp);

        // Act
        action.actionPerformed(fakeEvent());

        // Assert
        assertFalse("selectAll() should NOT be called when target is disabled",
                    comp.wasSelectAllCalled());
    }

}