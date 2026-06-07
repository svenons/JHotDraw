/*
 * @(#)UndoRedoManagerTest.java
 *
 * Copyright (c) 1996-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.undo;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests {@link UndoRedoManager}: the LIFO undo/redo stack behaviour, the
 * "has significant edits" property notification, and the re-entrancy guard
 * that discards edits arriving while an undo/redo is in progress.
 */
public class UndoRedoManagerTest {

    private UndoRedoManager manager;

    @Before
    public void setUp() {
        manager = new UndoRedoManager();
    }

    /** Creates a significant, undoable/redoable mock edit. */
    private UndoableEdit significantEdit() {
        UndoableEdit edit = mock(UndoableEdit.class);
        when(edit.isSignificant()).thenReturn(true);
        when(edit.canUndo()).thenReturn(true);
        when(edit.canRedo()).thenReturn(true);
        return edit;
    }

    /** Records the last property change fired by the manager. */
    private static class RecordingListener implements PropertyChangeListener {
        boolean notified = false;
        String propertyName;
        Object newValue;

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            notified = true;
            propertyName = evt.getPropertyName();
            newValue = evt.getNewValue();
        }
    }

    /**
     * Edit whose undo() re-enters the manager by adding another edit,
     * simulating a modification triggered during an undo operation.
     */
    private static class ReentrantEdit extends AbstractUndoableEdit {
        private static final long serialVersionUID = 1L;
        private final UndoRedoManager manager;
        private final UndoableEdit reentrant;

        ReentrantEdit(UndoRedoManager manager, UndoableEdit reentrant) {
            this.manager = manager;
            this.reentrant = reentrant;
        }

        @Override
        public void undo() {
            super.undo();
            // This edit arrives while an undo is in progress.
            manager.addEdit(reentrant);
        }
    }

    // ----------------------------------------------------------
    // Test 1: undo() reverses the last edit
    // ----------------------------------------------------------
    @Test
    public void undo_reversesLastEdit() {
        UndoableEdit edit = significantEdit();
        manager.addEdit(edit);

        manager.undo();

        verify(edit).undo();
    }

    // ----------------------------------------------------------
    // Test 2: redo() re-applies a previously undone edit
    // ----------------------------------------------------------
    @Test
    public void redo_reappliesUndoneEdit() {
        UndoableEdit edit = significantEdit();
        manager.addEdit(edit);

        manager.undo();
        manager.redo();

        verify(edit).redo();
    }

    // ----------------------------------------------------------
    // Test 3: edits are undone in LIFO order (last added first)
    // ----------------------------------------------------------
    @Test
    public void undo_lifoOrder() {
        UndoableEdit first = significantEdit();
        UndoableEdit second = significantEdit();
        manager.addEdit(first);
        manager.addEdit(second);

        manager.undo();

        // Only the most recent edit is undone by a single undo()
        verify(second).undo();
        verify(first, never()).undo();
    }

    // ----------------------------------------------------------
    // Test 4: undo() on an empty stack throws CannotUndoException
    // ----------------------------------------------------------
    @Test(expected = CannotUndoException.class)
    public void undo_onEmptyStack() {
        manager.undo();
    }

    // ----------------------------------------------------------
    // Test 5: adding a significant edit fires HAS_SIGNIFICANT_EDITS_PROPERTY
    // ----------------------------------------------------------
    @Test
    public void propertyChange_onSignificantEdit() {
        RecordingListener listener = new RecordingListener();
        manager.addPropertyChangeListener(listener);

        manager.addEdit(significantEdit());

        assertTrue("A property change should be fired", listener.notified);
        assertEquals(UndoRedoManager.HAS_SIGNIFICANT_EDITS_PROPERTY, listener.propertyName);
        assertEquals(Boolean.TRUE, listener.newValue);
        assertTrue("Manager should report significant edits", manager.hasSignificantEdits());
    }

    // ----------------------------------------------------------
    // Test 6: while undo/redo is in progress, incoming edits are discarded
    // ----------------------------------------------------------
    @Test
    public void executeWithProgressFlag() {
        UndoableEdit reentrant = mock(UndoableEdit.class);
        ReentrantEdit edit = new ReentrantEdit(manager, reentrant);
        manager.addEdit(edit);

        // Undoing the edit causes it to add 'reentrant' mid-operation.
        manager.undo();

        // The re-entrant edit must be discarded (die()), never kept on the stack.
        verify(reentrant).die();
        verify(reentrant, never()).undo();
    }
}
