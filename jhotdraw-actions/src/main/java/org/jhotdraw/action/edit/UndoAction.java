/*
 * @(#)UndoAction.java
 *
 * Copyright (c) 1996-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.action.edit;

import org.jhotdraw.api.app.Application;
import org.jhotdraw.api.app.View;

/**
 * Undoes the last user action on the active view.
 * <p>
 * This action delegates to a view-specific undo action registered
 * in the view's ActionMap under the key {@value #ID}.
 * <p>
 * This action is called when the user selects the Undo item in the Edit
 * menu. The menu item is automatically created by the application.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class UndoAction extends AbstractUndoRedoAction {

    private static final long serialVersionUID = 1L;
    public static final String ID = "edit.undo";

    public UndoAction(Application app, View view) {
        super(app, view);
    }

    @Override
    protected String getActionId() {
        return ID;
    }
}
