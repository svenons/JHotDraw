/*
 * @(#)BringToFrontAction.java
 *
 * Copyright (c) 2003-2008 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.draw.action;

import org.jhotdraw.draw.figure.Figure;
import java.util.Collection;
import java.util.LinkedList;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.util.ResourceBundleUtil;

/**
 * UI action for <em>Arrange &rarr; Send to Front</em>. Collects the figures currently selected in the
 * active {@link DrawingView} and raises them to the top of the drawing's z-order by delegating to the
 * {@link Drawing} model, then registers a {@link ZOrderUndoableEdit} so the operation can be undone.
 *
 * <p>Depends only on the {@code Drawing} interface (not a concrete implementation), keeping the UI
 * layer decoupled from the model (Dependency Inversion Principle).
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class BringToFrontAction extends AbstractSelectedAction {

    private static final long serialVersionUID = 1L;
    public static final String ID = "edit.bringToFront";

    /**
     * Creates a new instance.
     */
    public BringToFrontAction(DrawingEditor editor) {
        super(editor);
        ResourceBundleUtil labels
                = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");
        labels.configureAction(this, ID);
        updateEnabledState();
    }

    /**
     * Brings the current selection to the front and records an undoable edit. The edit is created with
     * {@code toFront == true}, so redo brings to front and undo sends to back.
     */
    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        final DrawingView view = getView();
        final LinkedList<Figure> figures = new LinkedList<>(view.getSelectedFigures());
        bringToFront(view, figures);
        fireUndoableEditHappened(new ZOrderUndoableEdit(view, figures, ID, true));
    }

    /**
     * Brings the given figures to the front of the drawing, preserving their relative order (they are
     * sorted by current z-order before each is moved to the top).
     *
     * @param view    the drawing view whose drawing is modified
     * @param figures the figures to bring to front
     */
    public static void bringToFront(DrawingView view, Collection<Figure> figures) {
        Drawing drawing = view.getDrawing();
        for (Figure figure : drawing.sort(figures)) {
            drawing.bringToFront(figure);
        }
    }
}
