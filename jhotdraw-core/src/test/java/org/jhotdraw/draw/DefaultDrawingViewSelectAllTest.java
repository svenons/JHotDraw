package org.jhotdraw.draw;

import org.jhotdraw.draw.figure.Figure;
import org.jhotdraw.draw.figure.RectangleFigure;
import org.junit.*;
import java.util.*;

import static org.junit.Assert.*;

public class DefaultDrawingViewSelectAllTest {

    private DefaultDrawingView view;
    private Drawing drawing;

    @Before
    public void setUp() {
        drawing = new DefaultDrawing();
        view = new DefaultDrawingView();
        view.setDrawing(drawing);
    }


    private Figure selectableFigure() {
        RectangleFigure f = new RectangleFigure();
        f.setSelectable(true);
        return f;
    }

    private Figure nonSelectableFigure() {
        RectangleFigure f = new RectangleFigure();
        f.setSelectable(false);
        return f;
    }

    @Test
    public void selectAll_selectsAllSelectableFigures() {
        // Arrange
        Figure f1 = selectableFigure();
        Figure f2 = selectableFigure();
        drawing.add(f1);
        drawing.add(f2);

        // Act
        view.selectAll();

        // Assert
        Set<Figure> selected = view.getSelectedFigures();
        assertTrue("f1 should be selected", selected.contains(f1));
        assertTrue("f2 should be selected", selected.contains(f2));
        assertEquals("Exactly 2 figures should be selected", 2, selected.size());
    }
}