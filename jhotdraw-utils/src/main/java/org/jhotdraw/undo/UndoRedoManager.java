/*
 * @(#)UndoRedoManager.java
 *
 * Copyright (c) 1996-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.undo;

import java.awt.event.*;
import java.beans.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.undo.*;
import org.jhotdraw.util.*;

/**
 * Extends {@link javax.swing.undo.UndoManager} with property change support,
 * a "has significant edits" flag, and inner undo/redo actions suitable for
 * direct use in menus and ActionMaps.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class UndoRedoManager extends UndoManager {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(UndoRedoManager.class.getName());

    /** Property name fired when the "has significant edits" flag changes. */
    public static final String HAS_SIGNIFICANT_EDITS_PROPERTY = "hasSignificantEdits";

    protected PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);
    private static final boolean DEBUG = false;

    private static ResourceBundleUtil labels;

    private boolean hasSignificantEdits = false;

    /**
     * Flag set during undo/redo operations. While true, incoming edits
     * are discarded to prevent re-entrant modifications corrupting the stack.
     */
    private boolean undoOrRedoInProgress;

    /**
     * Sentinel edit that disables undo and redo when added to the manager.
     */
    public static final UndoableEdit DISCARD_ALL_EDITS = new AbstractUndoableEdit() {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean canUndo() {
            return false;
        }

        @Override
        public boolean canRedo() {
            return false;
        }
    };

    /**
     * Inner undo action for use in a menu bar.
     */
    private class UndoAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        public UndoAction() {
            labels.configureAction(this, "edit.undo");
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            try {
                undo();
            } catch (CannotUndoException e) {
                LOG.log(Level.WARNING, "Cannot undo", e);
            }
        }
    }

    /**
     * Inner redo action for use in a menu bar.
     */
    private class RedoAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        public RedoAction() {
            labels.configureAction(this, "edit.redo");
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            try {
                redo();
            } catch (CannotRedoException e) {
                LOG.log(Level.WARNING, "Cannot redo", e);
            }
        }
    }

    private UndoAction undoAction;
    private RedoAction redoAction;

    public static ResourceBundleUtil getLabels() {
        if (labels == null) {
            labels = ResourceBundleUtil.getBundle("org.jhotdraw.undo.Labels");
        }
        return labels;
    }

    public UndoRedoManager() {
        getLabels();
        undoAction = new UndoAction();
        redoAction = new RedoAction();
    }

    public void setLocale(Locale l) {
        labels = ResourceBundleUtil.getBundle("org.jhotdraw.undo.Labels", l);
    }

    @Override
    public void discardAllEdits() {
        super.discardAllEdits();
        updateActions();
        setHasSignificantEdits(false);
    }

    public void setHasSignificantEdits(boolean newValue) {
        boolean oldValue = hasSignificantEdits;
        hasSignificantEdits = newValue;
        firePropertyChange(HAS_SIGNIFICANT_EDITS_PROPERTY, oldValue, newValue);
    }

    public boolean hasSignificantEdits() {
        return hasSignificantEdits;
    }

    @Override
    public boolean addEdit(UndoableEdit anEdit) {
        if (DEBUG) {
            LOG.log(Level.FINE, "UndoRedoManager@{0}.add {1}",
                    new Object[]{hashCode(), anEdit});
        }
        if (undoOrRedoInProgress) {
            anEdit.die();
            return true;
        }
        boolean success = super.addEdit(anEdit);
        updateActions();
        if (success && anEdit.isSignificant() && editToBeUndone() == anEdit) {
            setHasSignificantEdits(true);
        }
        return success;
    }

    public Action getUndoAction() {
        return undoAction;
    }

    public Action getRedoAction() {
        return redoAction;
    }

    /**
     * Updates the enabled state and presentation names of the undo and redo actions.
     */
    private void updateActions() {
        if (DEBUG) {
            LOG.log(Level.FINE, "UndoRedoManager@{0}.updateActions editToBeUndone={1} canUndo={2} canRedo={3}",
                    new Object[]{hashCode(), editToBeUndone(), canUndo(), canRedo()});
        }
        updateAction(undoAction, canUndo(), getUndoPresentationName(), "edit.undo.text");
        updateAction(redoAction, canRedo(), getRedoPresentationName(), "edit.redo.text");
    }

    /**
     * Updates a single action's enabled state and display label.
     */
    private void updateAction(AbstractAction action, boolean canPerform,
                              String presentationName, String defaultLabelKey) {
        action.setEnabled(canPerform);
        String label = canPerform ? presentationName : labels.getString(defaultLabelKey);
        action.putValue(Action.NAME, label);
        action.putValue(Action.SHORT_DESCRIPTION, label);
    }

    /**
     * Executes an undo or redo operation with the progress flag set,
     * ensuring incoming edits are discarded during the operation and
     * actions are updated afterward.
     */
    private void executeWithProgressFlag(Runnable operation) {
        undoOrRedoInProgress = true;
        try {
            operation.run();
        } finally {
            undoOrRedoInProgress = false;
            updateActions();
        }
    }

    @Override
    public void undo() throws CannotUndoException {
        executeWithProgressFlag(super::undo);
    }

    @Override
    public void redo() throws CannotUndoException {
        executeWithProgressFlag(super::redo);
    }

    @Override
    public void undoOrRedo() throws CannotUndoException, CannotRedoException {
        executeWithProgressFlag(super::undoOrRedo);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(propertyName, listener);
    }

    protected void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        propertySupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    protected void firePropertyChange(String propertyName, int oldValue, int newValue) {
        propertySupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        propertySupport.firePropertyChange(propertyName, oldValue, newValue);
    }
}
