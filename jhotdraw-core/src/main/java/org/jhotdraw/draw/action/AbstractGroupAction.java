package org.jhotdraw.draw.action;

import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.figure.CompositeFigure;
import org.jhotdraw.draw.figure.Figure;
import java.util.Collection;
import java.util.LinkedList;

public abstract class AbstractGroupAction extends AbstractSelectedAction {
    private static final long serialVersionUID = 1L;
    protected static final String LABELS_RESOURCE = "org.jhotdraw.draw.Labels";
    protected CompositeFigure prototype;

    protected AbstractGroupAction(org.jhotdraw.draw.DrawingEditor editor, CompositeFigure prototype) {
        super(editor);
        this.prototype = prototype;
    }

    // The shared math for grouping
    public void groupFigures(DrawingView view, CompositeFigure group, Collection<Figure> figures) {
        Collection<Figure> sorted = view.getDrawing().sort(figures);
        int index = view.getDrawing().indexOf(sorted.iterator().next());
        view.getDrawing().basicRemoveAll(figures);
        view.clearSelection();
        view.getDrawing().add(index, group);
        group.willChange();
        for (Figure f : sorted) {
            f.willChange();
            group.basicAdd(f);
        }
        group.changed();
        view.addToSelection(group);
    }

    // The shared math for ungrouping
    public Collection<Figure> ungroupFigures(DrawingView view, CompositeFigure group) {
        LinkedList<Figure> figures = new LinkedList<>(group.getChildren());
        view.clearSelection();
        group.basicRemoveAllChildren();
        view.getDrawing().basicAddAll(view.getDrawing().indexOf(group), figures);
        view.getDrawing().remove(group);
        view.addToSelection(figures);
        return figures;
    }
}