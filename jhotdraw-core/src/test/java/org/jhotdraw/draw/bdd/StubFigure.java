/*
 * Copyright (c) 2026 The authors and contributors of JHotDraw.
 */
package org.jhotdraw.draw.bdd;

import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.figure.AbstractFigure;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.Map;

/**
 * Minimal Figure stub for use in BDD stage classes.
 * Returns a fixed 10x10 rectangle for all spatial queries; all other methods are no-ops.
 */
class StubFigure extends AbstractFigure {

    @Override public void draw(Graphics2D g) {}

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

    @Override public boolean contains(Point2D.Double p) { return false; }
    @Override public Object getTransformRestoreData() { return null; }
    @Override public void restoreTransformTo(Object restoreData) {}
    @Override public void transform(AffineTransform tx) {}
    @Override public <T> void set(AttributeKey<T> key, T value) {}
    @Override public <T> T get(AttributeKey<T> key) { return null; }
    @Override public Map<AttributeKey<?>, Object> getAttributes() { return Collections.emptyMap(); }
    @Override public Object getAttributesRestoreData() { return null; }
    @Override public void restoreAttributesTo(Object restoreData) {}
}
