package org.jhotdraw.draw.action;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.figure.CompositeFigure;
import org.jhotdraw.draw.figure.Figure;
import java.util.Collection;
import org.jhotdraw.util.ResourceBundleUtil;

public class GroupUndoableEdit extends AbstractUndoableEdit {
    private static final long serialVersionUID = 1L;
    private final transient AbstractGroupAction action;
    private final transient DrawingView view;
    private final CompositeFigure group;
    private final Collection<Figure> figures;
    private final boolean isGrouping;

    public GroupUndoableEdit(AbstractGroupAction action, DrawingView view, CompositeFigure group, Collection<Figure> figures, boolean isGrouping) {
        this.action = action;
        this.view = view;
        this.group = group;
        this.figures = figures;
        this.isGrouping = isGrouping;
    }

    @Override
    public String getPresentationName() {
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle(AbstractGroupAction.LABELS_RESOURCE);
        return labels.getString(isGrouping ? "edit.groupSelection.text" : "edit.ungroupSelection.text");
    }

    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        if (isGrouping) {
            action.groupFigures(view, group, figures);
        } else {
            action.ungroupFigures(view, group);
        }
    }

    @Override
    public void undo() throws CannotUndoException {
        if (isGrouping) {
            action.ungroupFigures(view, group);
        } else {
            action.groupFigures(view, group, figures);
        }
        super.undo();
    }
}