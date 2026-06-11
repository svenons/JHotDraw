package org.jhotdraw.action.edit;

import org.junit.*;
import javax.swing.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.jhotdraw.api.gui.EditableComponent;

import static org.junit.Assert.*;


public class ClearSelectionActionTest {

    // Test double: a real JComponent that implements EditableComponent and
    // records whether clearSelection() was called.
    static class TestEditableComponent extends JComponent implements EditableComponent {
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private boolean selectionEmpty = false;          // starts with a selection
        private boolean clearSelectionCalled = false;

        @Override public void delete() { }
        @Override public void duplicate() { }
        @Override public void selectAll() { selectionEmpty = false; }

        @Override
        public void clearSelection() {
            clearSelectionCalled = true;
            boolean old = selectionEmpty;
            selectionEmpty = true;
            pcs.firePropertyChange(SELECTION_EMPTY_PROPERTY, old, selectionEmpty);
        }

        @Override public boolean isSelectionEmpty() { return selectionEmpty; }
        @Override public void addPropertyChangeListener(PropertyChangeListener l) { pcs.addPropertyChangeListener(l); }
        @Override public void removePropertyChangeListener(PropertyChangeListener l) { pcs.removePropertyChangeListener(l); }

        public boolean wasClearSelectionCalled() { return clearSelectionCalled; }
    }

    private ActionEvent fakeEvent() {
        return new ActionEvent(new Object(), ActionEvent.ACTION_PERFORMED, "");
    }

    @Test
    public void clearSelection_delegatesToEditableComponent_targetNonNull() {
        TestEditableComponent comp = new TestEditableComponent();
        comp.setEnabled(true);

        ClearSelectionAction action = new ClearSelectionAction(comp);
        action.actionPerformed(fakeEvent());

        assertTrue("clearSelection() should have been called", comp.wasClearSelectionCalled());
    }

    @Test
    public void clearSelection_doesNothing_whenTargetDisabled() {
        TestEditableComponent comp = new TestEditableComponent();
        comp.setEnabled(false);

        ClearSelectionAction action = new ClearSelectionAction(comp);
        action.actionPerformed(fakeEvent());

        assertFalse("clearSelection() should NOT be called when target is disabled",
                comp.wasClearSelectionCalled());
    }

    @Test
    public void clearSelection_usesFocusedComponent_whenTargetNull() {
        ClearSelectionAction action = new ClearSelectionAction(null);
        try {
            action.actionPerformed(fakeEvent());
        } catch (NullPointerException e) {
            fail("Should not throw NullPointerException when target is null");
        }
    }


    @Test
    public void clearSelection_handlesUnsupportedComponent_gracefully() {
        JPanel unsupported = new JPanel();
        unsupported.setEnabled(true);

        ClearSelectionAction action = new ClearSelectionAction(unsupported);
        try {
            action.actionPerformed(fakeEvent());
        } catch (Exception e) {
            fail("Should handle unsupported component type gracefully: " + e.getMessage());
        }
    }
}
