package org.jhotdraw.draw.action;

import org.junit.*;
import java.util.*;

import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.figure.Figure;
import org.jhotdraw.draw.figure.RectangleFigure;
import org.jhotdraw.draw.figure.EllipseFigure;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class SelectSameActionTest {

    private DrawingView view;
    private Drawing drawing;
    private SelectSameAction action;

    @Before
    public void setUp() {
        DrawingEditor editor = mock(DrawingEditor.class);
        view = mock(DrawingView.class);
        drawing = mock(Drawing.class);
        when(editor.getActiveView()).thenReturn(view);
        when(view.getDrawing()).thenReturn(drawing);
        action = new SelectSameAction(editor);
    }

    private Set<Figure> selection(Figure... figs) {
        return new LinkedHashSet<>(Arrays.asList(figs));
    }

    private List<Figure> children(Figure... figs) {
        return Arrays.asList(figs);
    }


    @Test
    public void selectSame_selectsOtherFiguresOfSameClass() {
        Figure rect1 = new RectangleFigure();
        Figure rect2 = new RectangleFigure();
        when(view.getSelectedFigures()).thenReturn(selection(rect1));
        when(drawing.getChildren()).thenReturn(children(rect1, rect2));

        action.selectSame();

        verify(view).addToSelection(rect1);
        verify(view).addToSelection(rect2);
    }


    @Test
    public void selectSame_ignoresFiguresOfDifferentClass() {
        Figure rect1 = new RectangleFigure();
        Figure ellipse1 = new EllipseFigure();
        when(view.getSelectedFigures()).thenReturn(selection(rect1));
        when(drawing.getChildren()).thenReturn(children(rect1, ellipse1));

        action.selectSame();

        verify(view).addToSelection(rect1);
        verify(view, never()).addToSelection(ellipse1);
    }


    @Test
    public void selectSame_withMultipleSelectedClasses_selectsAllMatching() {
        Figure rect1 = new RectangleFigure();
        Figure rect2 = new RectangleFigure();
        Figure ellipse1 = new EllipseFigure();
        Figure ellipse2 = new EllipseFigure();
        when(view.getSelectedFigures()).thenReturn(selection(rect1, ellipse1));
        when(drawing.getChildren()).thenReturn(children(rect1, rect2, ellipse1, ellipse2));

        action.selectSame();

        verify(view).addToSelection(rect2);
        verify(view).addToSelection(ellipse2);
    }


    @Test
    public void selectSame_withEmptySelection_selectsNothing() {
        Figure rect1 = new RectangleFigure();
        Figure ellipse1 = new EllipseFigure();
        when(view.getSelectedFigures()).thenReturn(selection());          // empty
        when(drawing.getChildren()).thenReturn(children(rect1, ellipse1));

        action.selectSame();

        verify(view, never()).addToSelection(any(Figure.class));
    }
}
