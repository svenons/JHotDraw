/*
 * @(#)CombinePathsAction.java
 *
 * Copyright (c) 2007 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.samples.odg.action;

import org.jhotdraw.draw.figure.Figure;
import org.jhotdraw.draw.figure.CompositeFigure;
import java.util.*;
import org.jhotdraw.draw.*;
import org.jhotdraw.draw.action.*;
import org.jhotdraw.samples.odg.figures.ODGPathFigure;
import org.jhotdraw.util.*;

/**
 * CombinePathsAction.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class CombineAction extends GroupAction {

    private static final long serialVersionUID = 1L;
    public static final String ID = "edit.combinePaths";
    private ResourceBundleUtil labels
            = ResourceBundleUtil.getBundle("org.jhotdraw.samples.odg.Labels");

    /**
     * Creates a new instance.
     */
    public CombineAction(DrawingEditor editor) {
        super(editor, new ODGPathFigure());
        labels.configureAction(this, ID);
    }


    @Override
    protected void updateEnabledState() {
        super.updateEnabledState(); 
        
        if (isEnabled()) {
            boolean canCombine = true;
            for (Figure f : getView().getSelectedFigures()) {
                if (!(f instanceof ODGPathFigure)) {
                    canCombine = false;
                    break;
                }
            }
            setEnabled(canCombine);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void groupFigures(DrawingView view, CompositeFigure group, Collection<Figure> figures) {
        Collection<Figure> sorted = view.getDrawing().sort(figures);
        view.getDrawing().basicRemoveAll(figures);
        view.clearSelection();
        view.getDrawing().add(group);
        group.willChange();
        ((ODGPathFigure) group).removeAllChildren();
        for (Map.Entry<AttributeKey<?>, Object> entry : figures.iterator().next().getAttributes().entrySet()) {
            group.set((AttributeKey<Object>) entry.getKey(), entry.getValue());
        }
        for (Figure f : sorted) {
            ODGPathFigure path = (ODGPathFigure) f;
            // XXX - We must fire an UndoableEdito for the flattenTransform!
            path.flattenTransform();
            for (Figure child : path.getChildren()) {
                group.basicAdd(child);
            }
        }
        group.changed();
        view.addToSelection(group);
    }
}
