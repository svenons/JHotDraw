package org.jhotdraw.draw.action;

import org.jhotdraw.draw.figure.Figure;
import org.jhotdraw.draw.figure.CompositeFigure;
import org.jhotdraw.draw.figure.GroupFigure;
import java.util.*;
import javax.swing.undo.*;
import org.jhotdraw.draw.*;
import org.jhotdraw.util.ResourceBundleUtil;

public class GroupAction extends AbstractGroupAction {
    private static final long serialVersionUID = 1L;
    public static final String ID = "edit.groupSelection";

    public GroupAction(DrawingEditor editor) {
        this(editor, new GroupFigure());
    }

    public GroupAction(DrawingEditor editor, CompositeFigure prototype) {
        super(editor, prototype);
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle(LABELS_RESOURCE);
        labels.configureAction(this, ID);
        updateEnabledState();
    }

    @Override
    protected void updateEnabledState() {
        if (getView() != null) {
            setEnabled(getView().getSelectionCount() > 1);
        } else {
            setEnabled(false);
        }
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        if (getView() != null && getView().getSelectionCount() > 1) {
            final DrawingView view = getView();
            final LinkedList<Figure> ungroupedFigures = new LinkedList<>(view.getSelectedFigures());
            final CompositeFigure group = (CompositeFigure) prototype.clone();

            groupFigures(view, group, ungroupedFigures);
            UndoableEdit edit = new GroupUndoableEdit(this, view, group, ungroupedFigures, true);
            fireUndoableEditHappened(edit);
        }
    }
}