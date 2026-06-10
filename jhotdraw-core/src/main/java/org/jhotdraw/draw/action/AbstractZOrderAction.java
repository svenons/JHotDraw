/*
 * @(#)AbstractZOrderAction.java
 *
 * Copyright (c) 2003-2008 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.draw.action;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.LinkedList;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.figure.Figure;
import org.jhotdraw.util.ResourceBundleUtil;

/**
 * Defines the common workflow for z-order actions on selected figures.
 */
public abstract class AbstractZOrderAction extends AbstractSelectedAction {

    private static final long serialVersionUID = 1L;
    private final String id;

    protected AbstractZOrderAction(DrawingEditor editor, String id) {
        super(editor);
        this.id = id;
        ResourceBundleUtil labels
                = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");
        labels.configureAction(this, id);
        updateEnabledState();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final DrawingView view = getView();
        final LinkedList<Figure> figures = new LinkedList<>(view.getSelectedFigures());
        reorder(view, figures);
        fireUndoableEditHappened(new AbstractUndoableEdit() {
            private static final long serialVersionUID = 1L;

            @Override
            public String getPresentationName() {
                ResourceBundleUtil labels
                        = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");
                return labels.getTextProperty(id);
            }

            @Override
            public void redo() throws CannotRedoException {
                super.redo();
                reorder(view, figures);
            }

            @Override
            public void undo() throws CannotUndoException {
                super.undo();
                reverseReorder(view, figures);
            }
        });
    }

    protected abstract void reorder(DrawingView view, Collection<Figure> figures);

    protected abstract void reverseReorder(DrawingView view, Collection<Figure> figures);
}
