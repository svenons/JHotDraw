package org.jhotdraw.draw.action;

import org.jhotdraw.draw.figure.Figure;
import org.jhotdraw.draw.figure.CompositeFigure;
import org.jhotdraw.draw.figure.GroupFigure;
import java.util.*;
import javax.swing.undo.*;
import org.jhotdraw.draw.*;
import org.jhotdraw.util.ResourceBundleUtil;

public class UngroupAction extends AbstractGroupAction {
    private static final long serialVersionUID = 1L;
    public static final String ID = "edit.ungroupSelection";

    public UngroupAction(DrawingEditor editor) {
        this(editor, new GroupFigure());
    }

    public UngroupAction(DrawingEditor editor, CompositeFigure prototype) {
        super(editor, prototype);
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle(LABELS_RESOURCE);
        labels.configureAction(this, ID);
        updateEnabledState();
    }

    @Override
    protected void updateEnabledState() {
        if (getView() != null) {
            setEnabled(getView().getSelectionCount() == 1 
                && prototype != null 
                && getView().getSelectedFigures().iterator().next().getClass().equals(prototype.getClass()));
        } else {
            setEnabled(false);
        }
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        if (isEnabled()) {
            final DrawingView view = getView();
            final CompositeFigure group = (CompositeFigure) getView().getSelectedFigures().iterator().next();
            final LinkedList<Figure> ungroupedFigures = new LinkedList<>();

            ungroupedFigures.addAll(ungroupFigures(view, group));
            UndoableEdit edit = new GroupUndoableEdit(this, view, group, ungroupedFigures, false);
            fireUndoableEditHappened(edit);
        }
    }
}