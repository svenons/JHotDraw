/*
 * Copyright (c) 2026 The authors and contributors of JHotDraw.
 */
package org.jhotdraw.draw.action;

import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.QuadTreeDrawing;
import org.jhotdraw.draw.figure.AbstractFigure;
import org.jhotdraw.draw.figure.Figure;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests the extracted {@link ZOrderUndoableEdit} itself — that its {@code redo()}/{@code undo()}
 * reorder the drawing exactly like the original anonymous edits did. The Arrange unit/BDD suites pin
 * {@code QuadTreeDrawing}; this test pins the refactored class, closing that coverage gap.
 */
public class ZOrderUndoableEditTest {

    private QuadTreeDrawing drawing;
    private DrawingView view;
    private Figure a;
    private Figure c;

    @Before
    public void setUp() {
        drawing = new QuadTreeDrawing();
        a = new StubFigure();
        Figure b = new StubFigure();
        c = new StubFigure();
        drawing.add(a);   // index 0 = back
        drawing.add(b);
        drawing.add(c);   // last = front

        // A DrawingView whose only relevant behaviour is getDrawing(); all else returns null/0.
        view = (DrawingView) Proxy.newProxyInstance(
                DrawingView.class.getClassLoader(),
                new Class[]{DrawingView.class},
                (proxy, method, args) -> "getDrawing".equals(method.getName()) ? drawing : null);
    }

    private List<Figure> children() {
        return drawing.getChildren();
    }

    private static LinkedList<Figure> only(Figure f) {
        LinkedList<Figure> l = new LinkedList<>();
        l.add(f);
        return l;
    }

    /** A bring-to-front edit (toFront == true): undo sends the figure to back, redo brings it to front. */
    @Test
    public void bringToFrontEdit_undoSendsToBack_redoBringsToFront() {
        ZOrderUndoableEdit edit =
                new ZOrderUndoableEdit(view, only(a), BringToFrontAction.ID, true);

        edit.undo();
        assertSame("undo of a bring-to-front edit sends A to the back", a, children().get(0));

        edit.redo();
        assertSame("redo brings A back to the front", a, children().get(children().size() - 1));
    }

    /** A send-to-back edit (toFront == false): undo brings the figure to front, redo sends it to back. */
    @Test
    public void sendToBackEdit_undoBringsToFront_redoSendsToBack() {
        ZOrderUndoableEdit edit =
                new ZOrderUndoableEdit(view, only(c), SendToBackAction.ID, false);

        edit.undo();
        assertSame("undo of a send-to-back edit brings C to the front", c, children().get(children().size() - 1));

        edit.redo();
        assertSame("redo sends C to the back", c, children().get(0));
    }

    @Test
    public void presentationName_isNonNull() {
        ZOrderUndoableEdit edit =
                new ZOrderUndoableEdit(view, only(a), BringToFrontAction.ID, true);
        assertNotNull(edit.getPresentationName());
    }

    /** Minimal figure stub; {@code getLayer()==0} keeps the layer-comparator sort stable. */
    static class StubFigure extends AbstractFigure {
        @Override public int getLayer() { return 0; }
        @Override public void draw(Graphics2D g) { }
        @Override public Rectangle2D.Double getBounds() { return new Rectangle2D.Double(0, 0, 10, 10); }
        @Override public Rectangle2D.Double getDrawingArea() { return new Rectangle2D.Double(0, 0, 10, 10); }
        @Override public Rectangle2D.Double getDrawingArea(double factor) { return new Rectangle2D.Double(0, 0, 10, 10); }
        @Override public boolean contains(Point2D.Double p) { return false; }
        @Override public Object getTransformRestoreData() { return null; }
        @Override public void restoreTransformTo(Object restoreData) { }
        @Override public void transform(AffineTransform tx) { }
        @Override public <T> void set(AttributeKey<T> key, T value) { }
        @Override public <T> T get(AttributeKey<T> key) { return null; }
        @Override public Map<AttributeKey<?>, Object> getAttributes() { return Collections.emptyMap(); }
        @Override public Object getAttributesRestoreData() { return null; }
        @Override public void restoreAttributesTo(Object restoreData) { }
    }
}