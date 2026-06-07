/*
 * @(#)AbstractUndoRedoActionDelegateTest.java
 *
 * Copyright (c) 1996-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.action.edit;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import org.jhotdraw.api.app.Application;
import org.jhotdraw.api.app.View;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests the delegation logic in {@link AbstractUndoRedoAction}: the action
 * looks up the "real" view-specific action in the active view's ActionMap and
 * delegates to it, guarding against self-delegation and missing entries.
 * <p>
 * These tests cover the Undo/Redo fix where the wrapper action must hand off
 * to the {@code UndoRedoManager}'s inner action registered in the view.
 */
public class AbstractUndoRedoActionDelegateTest {

    /** Concrete subclass under test, bound to the "edit.undo" lookup key. */
    private static class TestUndoRedoAction extends AbstractUndoRedoAction {
        private static final long serialVersionUID = 1L;

        TestUndoRedoAction(Application app, View view) {
            super(app, view);
        }

        @Override
        protected String getActionId() {
            return UndoAction.ID;
        }
    }

    private Application app;
    private View view;

    @Before
    public void setUp() {
        app = mock(Application.class);
        view = mock(View.class);
        when(app.isEnabled()).thenReturn(true);
        when(view.isEnabled()).thenReturn(true);
    }

    private ActionEvent fakeEvent() {
        return new ActionEvent(new Object(), ActionEvent.ACTION_PERFORMED, "");
    }

    // ----------------------------------------------------------
    // Test 1: actionPerformed delegates to the real view action
    // ----------------------------------------------------------
    @Test
    public void actionPerformed_delegatesToRealAction() {
        // Arrange: the view's ActionMap holds the real undo action
        Action realAction = mock(Action.class);
        ActionMap map = new ActionMap();
        map.put(UndoAction.ID, realAction);
        when(view.getActionMap()).thenReturn(map);

        TestUndoRedoAction action = new TestUndoRedoAction(app, view);
        ActionEvent event = fakeEvent();

        // Act
        action.actionPerformed(event);

        // Assert: the wrapper forwarded the call to the real action
        verify(realAction).actionPerformed(event);
    }

    // ----------------------------------------------------------
    // Test 2: when the lookup returns the wrapper itself, it must
    // NOT delegate (otherwise it would recurse infinitely)
    // ----------------------------------------------------------
    @Test
    public void actionPerformed_skipsWhenSelfDelegation() {
        ActionMap map = new ActionMap();
        when(view.getActionMap()).thenReturn(map);

        TestUndoRedoAction action = new TestUndoRedoAction(app, view);
        // Register the action under its own key -> realAction == this
        map.put(UndoAction.ID, action);

        // Act + Assert: completes without recursing into itself
        try {
            action.actionPerformed(fakeEvent());
        } catch (StackOverflowError e) {
            fail("Self-delegation must be skipped, not recurse infinitely");
        }
    }

    // ----------------------------------------------------------
    // Test 3: an empty/absent ActionMap entry is handled gracefully
    // ----------------------------------------------------------
    @Test
    public void actionPerformed_handlesNullActionMap() {
        // The view's ActionMap has no entry for "edit.undo"
        when(view.getActionMap()).thenReturn(new ActionMap());

        TestUndoRedoAction action = new TestUndoRedoAction(app, view);

        // Act + Assert: no real action -> no crash
        try {
            action.actionPerformed(fakeEvent());
        } catch (NullPointerException e) {
            fail("Should not throw when no real action is registered");
        }
    }

    // ----------------------------------------------------------
    // Test 4: updateEnabledState mirrors the real action's enabled state
    // ----------------------------------------------------------
    @Test
    public void updateEnabledState_whenViewHasAction() {
        Action realAction = new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
            }
        };
        realAction.setEnabled(true);
        ActionMap map = new ActionMap();
        map.put(UndoAction.ID, realAction);
        when(view.getActionMap()).thenReturn(map);

        TestUndoRedoAction action = new TestUndoRedoAction(app, view);

        // Act
        action.updateEnabledState();

        // Assert: enabled real action -> wrapper becomes enabled
        assertTrue("Wrapper should be enabled when the real action is enabled",
                action.isEnabled());
    }

    // ----------------------------------------------------------
    // Test 5: with no active view, the wrapper is disabled
    // ----------------------------------------------------------
    @Test
    public void updateEnabledState_whenViewNull() {
        // No explicit view and the application has no active view
        when(app.getActiveView()).thenReturn(null);

        TestUndoRedoAction action = new TestUndoRedoAction(app, null);

        // Act
        action.updateEnabledState();

        // Assert: no real action reachable -> disabled
        assertFalse("Wrapper should be disabled when there is no active view",
                action.isEnabled());
    }
}
