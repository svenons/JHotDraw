/*
 * @(#)DefaultApplicationModelActionMapTest.java
 *
 * Copyright (c) 1996-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.app;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import org.jhotdraw.action.edit.UndoAction;
import org.jhotdraw.api.app.Application;
import org.jhotdraw.api.app.View;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests {@link DefaultApplicationModel#createActionMap}: the Undo/Redo fix
 * ensures the model does not overwrite a view's own undo action (wired by
 * {@code DrawView.initActions()} to the {@code UndoRedoManager}'s inner
 * action), while still providing a default wrapper when none exists.
 */
public class DefaultApplicationModelActionMapTest {

    private DefaultApplicationModel model;
    private Application app;

    @Before
    public void setUp() {
        model = new DefaultApplicationModel();
        app = mock(Application.class);
    }

    // ----------------------------------------------------------
    // Test 1: a view's existing undo action is preserved, not overwritten
    // ----------------------------------------------------------
    @Test
    public void createActionMap_preservesViewActions() {
        Action viewUndo = new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
            }
        };
        ActionMap viewMap = new ActionMap();
        viewMap.put(UndoAction.ID, viewUndo);

        View view = mock(View.class);
        when(view.getActionMap()).thenReturn(viewMap);

        ActionMap result = model.createActionMap(app, view);

        // The model must NOT register its own wrapper -> leaves the lookup
        // to fall through to the view's own action.
        assertNull("Model must not overwrite the view's undo action",
                result.get(UndoAction.ID));
        // The view's own action remains intact.
        assertSame("View's undo action must stay registered",
                viewUndo, viewMap.get(UndoAction.ID));
    }

    // ----------------------------------------------------------
    // Test 2: a null view causes a fresh wrapper UndoAction to be created
    // ----------------------------------------------------------
    @Test
    public void createActionMap_nullViewCreatesNew() {
        ActionMap result = model.createActionMap(app, null);

        Action undo = result.get(UndoAction.ID);
        assertNotNull("A wrapper UndoAction should be created for a null view", undo);
        assertTrue("Created action should be an UndoAction wrapper",
                undo instanceof UndoAction);
    }

    // ----------------------------------------------------------
    // Test 3: a view without an existing undo action gets a fresh wrapper
    // ----------------------------------------------------------
    @Test
    public void createActionMap_noExistingActionCreatesNew() {
        View view = mock(View.class);
        when(view.getActionMap()).thenReturn(new ActionMap());

        ActionMap result = model.createActionMap(app, view);

        Action undo = result.get(UndoAction.ID);
        assertNotNull("A wrapper UndoAction should be created when none exists", undo);
        assertTrue("Created action should be an UndoAction wrapper",
                undo instanceof UndoAction);
    }
}
