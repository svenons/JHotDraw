/*
 * @(#)SendToBackAction.java
 *
 * Copyright (c) 2003-2008 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.draw.action;

import org.jhotdraw.draw.figure.Figure;
import java.util.*;
import org.jhotdraw.draw.*;
import org.jhotdraw.util.ResourceBundleUtil;

/**
 * UI action for <em>Arrange &rarr; Send to Back</em>. Collects the figures currently selected in the
 * active {@link DrawingView} and lowers them to the bottom of the drawing's z-order by delegating to
 * the {@link Drawing} model, then registers a {@link ZOrderUndoableEdit} so the operation can be
 * undone.
 *
 * <p>Mirror image of {@link BringToFrontAction}. Note the pre-existing asymmetry: unlike
 * {@code bringToFront}, this path does not sort the figures before moving them &mdash; see the inline
 * sorting caveat in {@link #sendToBack}.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class SendToBackAction extends AbstractSelectedAction {

    private static final long serialVersionUID = 1L;
    public static final String ID = "edit.sendToBack";

    /**
     * Creates a new instance.
     */
    public SendToBackAction(DrawingEditor editor) {
        super(editor);
        ResourceBundleUtil labels
                = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");
        labels.configureAction(this, ID);
        updateEnabledState();
    }

    /**
     * Sends the current selection to the back and records an undoable edit. The edit is created with
     * {@code toFront == false}, so redo sends to back and undo brings to front.
     */
    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        final DrawingView view = getView();
        final LinkedList<Figure> figures = new LinkedList<>(view.getSelectedFigures());
        sendToBack(view, figures);
        fireUndoableEditHappened(new ZOrderUndoableEdit(view, figures, ID, false));
    }

    /**
     * Sends the given figures to the back of the drawing.
     *
     * @param view    the drawing view whose drawing is modified
     * @param figures the figures to send to back
     */
    public static void sendToBack(DrawingView view, Collection<Figure> figures) {
        Drawing drawing = view.getDrawing();
        for (Figure figure : figures) { // XXX Shouldn't the figures be sorted here back to front?
            drawing.sendToBack(figure);
        }
    }
}
