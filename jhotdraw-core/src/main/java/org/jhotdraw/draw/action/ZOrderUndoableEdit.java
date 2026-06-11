/*
 * Copyright (c) 2003-2008 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.draw.action;

import org.jhotdraw.draw.figure.Figure;
import java.util.LinkedList;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.util.ResourceBundleUtil;

/**
 * Extracted from the duplicated anonymous AbstractUndoableEdit in BringToFrontAction and
 * SendToBackAction. Encapsulates the undo/redo logic for z-order operations.
 *
 * <p>When {@code toFront} is {@code true}, redo brings figures to front and undo sends them to
 * back. When {@code false}, the directions are reversed.
 */
class ZOrderUndoableEdit extends AbstractUndoableEdit {

    private static final long serialVersionUID = 1L;

    private final DrawingView view;
    private final LinkedList<Figure> figures;
    private final String labelId;
    private final boolean toFront;

    /**
     * @param view     the view whose drawing is reordered on redo/undo
     * @param figures  the figures whose z-order is changed
     * @param labelId  resource-bundle key for the undo presentation name
     * @param toFront  {@code true} for a bring-to-front edit (redo = front, undo = back);
     *                 {@code false} for a send-to-back edit (redo = back, undo = front)
     */
    ZOrderUndoableEdit(DrawingView view, LinkedList<Figure> figures, String labelId, boolean toFront) {
        this.view = view;
        this.figures = figures;
        this.labelId = labelId;
        this.toFront = toFront;
    }

    @Override
    public String getPresentationName() {
        return ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels").getTextProperty(labelId);
    }

    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        if (toFront) {
            BringToFrontAction.bringToFront(view, figures);
        } else {
            SendToBackAction.sendToBack(view, figures);
        }
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        if (toFront) {
            SendToBackAction.sendToBack(view, figures);
        } else {
            BringToFrontAction.bringToFront(view, figures);
        }
    }
}
