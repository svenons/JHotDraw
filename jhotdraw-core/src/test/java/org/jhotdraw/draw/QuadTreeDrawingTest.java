/*
 * Copyright (c) 2026 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.draw;

import org.jhotdraw.draw.figure.AbstractFigure;
import org.jhotdraw.draw.figure.Figure;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * JUnit 4 tests for QuadTreeDrawing.bringToFront and QuadTreeDrawing.sendToBack.
 *
 * Lab 5 — TestLab1 (Software Maintenance portfolio).
 */
public class QuadTreeDrawingTest {

    private QuadTreeDrawing drawing;
    private Figure figureA;
    private Figure figureB;
    private Figure figureC;

    @Before
    public void setUp() {
        drawing = new QuadTreeDrawing();
        figureA = new StubFigure();
        figureB = new StubFigure();
        figureC = new StubFigure();
        drawing.add(figureA);
        drawing.add(figureB);
        drawing.add(figureC);
    }

    // --- bringToFront ---

    @Test
    public void bringToFront_movesMiddleFigureToLast() {
        drawing.bringToFront(figureB);
        List<Figure> children = drawing.getChildren();
        assertEquals(figureB, children.get(children.size() - 1));
    }

    @Test
    public void bringToFront_movesBackFigureToLast() {
        drawing.bringToFront(figureA);
        List<Figure> children = drawing.getChildren();
        assertEquals(figureA, children.get(children.size() - 1));
    }

    @Test
    public void bringToFront_alreadyAtFront_figureRemainsLast() {
        drawing.bringToFront(figureC);
        List<Figure> children = drawing.getChildren();
        assertEquals(figureC, children.get(children.size() - 1));
    }

    @Test
    public void bringToFront_notInDrawing_orderUnchanged() {
        Figure outsider = new StubFigure();
        List<Figure> before = drawing.getChildren();
        drawing.bringToFront(outsider);
        assertEquals(before, drawing.getChildren());
    }

    @Test
    public void bringToFront_childCountUnchanged() {
        int before = drawing.getChildCount();
        drawing.bringToFront(figureA);
        assert drawing.getChildCount() == before : "child count must not change after bringToFront";
        assertEquals(before, drawing.getChildCount());
    }

    // --- sendToBack ---

    @Test
    public void sendToBack_movesMiddleFigureToFirst() {
        drawing.sendToBack(figureB);
        assertEquals(figureB, drawing.getChild(0));
    }

    @Test
    public void sendToBack_movesFrontFigureToFirst() {
        drawing.sendToBack(figureC);
        assertEquals(figureC, drawing.getChild(0));
    }

    @Test
    public void sendToBack_alreadyAtBack_figureRemainsFirst() {
        drawing.sendToBack(figureA);
        assertEquals(figureA, drawing.getChild(0));
    }

    @Test
    public void sendToBack_notInDrawing_orderUnchanged() {
        Figure outsider = new StubFigure();
        List<Figure> before = drawing.getChildren();
        drawing.sendToBack(outsider);
        assertEquals(before, drawing.getChildren());
    }

    @Test
    public void sendToBack_childCountUnchanged() {
        int before = drawing.getChildCount();
        drawing.sendToBack(figureC);
        assert drawing.getChildCount() == before : "child count must not change after sendToBack";
        assertEquals(before, drawing.getChildCount());
    }

    // -------------------------------------------------------------------------
    // Minimal Figure stub — returns a simple rectangle for spatial operations.
    // All other methods are no-ops so no Swing or external context is needed.
    // -------------------------------------------------------------------------
    static class StubFigure extends AbstractFigure {

        @Override
        public void draw(Graphics2D g) {
        }

        @Override
        public Rectangle2D.Double getBounds() {
            return new Rectangle2D.Double(0, 0, 10, 10);
        }

        @Override
        public Rectangle2D.Double getDrawingArea() {
            return new Rectangle2D.Double(0, 0, 10, 10);
        }

        @Override
        public Rectangle2D.Double getDrawingArea(double factor) {
            return new Rectangle2D.Double(0, 0, 10, 10);
        }

        @Override
        public boolean contains(Point2D.Double p) {
            return false;
        }

        @Override
        public Object getTransformRestoreData() {
            return null;
        }

        @Override
        public void restoreTransformTo(Object restoreData) {
        }

        @Override
        public void transform(AffineTransform tx) {
        }

        @Override
        public <T> void set(AttributeKey<T> key, T value) {
        }

        @Override
        public <T> T get(AttributeKey<T> key) {
            return null;
        }

        @Override
        public Map<AttributeKey<?>, Object> getAttributes() {
            return java.util.Collections.emptyMap();
        }

        @Override
        public Object getAttributesRestoreData() {
            return null;
        }

        @Override
        public void restoreAttributesTo(Object restoreData) {
        }
    }
}
