/*
 * @(#)AbstractUndoRedoAction.java
 *
 * Copyright (c) 1996-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.action.edit;

import java.awt.event.*;
import java.beans.*;
import javax.swing.*;
import org.jhotdraw.action.AbstractViewAction;
import org.jhotdraw.api.app.Application;
import org.jhotdraw.api.app.View;
import org.jhotdraw.util.*;

/**
 * Abstract base class for {@link UndoAction} and {@link RedoAction}.
 * <p>
 * Extracts the common delegation pattern: both actions look up a
 * view-specific action from the active view's ActionMap and delegate
 * to it. This eliminates duplicated code between the two classes.
 *
 * @author Refactored from UndoAction/RedoAction
 */
public abstract class AbstractUndoRedoAction extends AbstractViewAction {

    private static final long serialVersionUID = 1L;

    private final PropertyChangeListener viewActionPropertyListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String name = evt.getPropertyName();
            if (AbstractAction.NAME.equals(name)) {
                putValue(AbstractAction.NAME, evt.getNewValue());
            } else if ("enabled".equals(name)) {
                updateEnabledState();
            }
        }
    };

    /**
     * Creates a new instance.
     *
     * @param app  the application
     * @param view the view this action operates on
     */
    protected AbstractUndoRedoAction(Application app, View view) {
        super(app, view);
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.action.Labels");
        labels.configureAction(this, getActionId());
    }

    /**
     * Returns the action ID used to look up the real action in the view's ActionMap.
     * Subclasses return either {@code "edit.undo"} or {@code "edit.redo"}.
     */
    protected abstract String getActionId();

    protected void updateEnabledState() {
        boolean isEnabled = false;
        Action realAction = getRealAction();
        if (realAction != null && realAction != this) {
            isEnabled = realAction.isEnabled();
        }
        setEnabled(isEnabled);
    }

    @Override
    protected void updateView(View oldValue, View newValue) {
        super.updateView(oldValue, newValue);
        if (newValue != null) {
            Action viewAction = newValue.getActionMap().get(getActionId());
            if (viewAction != null && viewAction != this) {
                putValue(AbstractAction.NAME, viewAction.getValue(AbstractAction.NAME));
                updateEnabledState();
            }
        }
    }

    @Override
    protected void installViewListeners(View p) {
        super.installViewListeners(p);
        Action actionInView = p.getActionMap().get(getActionId());
        if (actionInView != null && actionInView != this) {
            actionInView.addPropertyChangeListener(viewActionPropertyListener);
        }
    }

    @Override
    protected void uninstallViewListeners(View p) {
        super.uninstallViewListeners(p);
        Action actionInView = p.getActionMap().get(getActionId());
        if (actionInView != null && actionInView != this) {
            actionInView.removePropertyChangeListener(viewActionPropertyListener);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Action realAction = getRealAction();
        if (realAction != null && realAction != this) {
            realAction.actionPerformed(e);
        }
    }

    /**
     * Looks up the real (view-specific) action from the active view's ActionMap.
     */
    private Action getRealAction() {
        return (getActiveView() == null) ? null : getActiveView().getActionMap().get(getActionId());
    }
}
