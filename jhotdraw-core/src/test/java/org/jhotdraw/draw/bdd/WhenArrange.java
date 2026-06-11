/*
 * Copyright (c) 2026 The authors and contributors of JHotDraw.
 */
package org.jhotdraw.draw.bdd;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ScenarioState.Resolution;
import org.jhotdraw.draw.QuadTreeDrawing;
import org.jhotdraw.draw.figure.Figure;

/**
 * JGiven When-stage: performs z-order operations on the drawing.
 * Lab 6 — BDDLab (Software Maintenance portfolio).
 */
public class WhenArrange extends Stage<WhenArrange> {

    @ExpectedScenarioState(resolution = Resolution.NAME)
    QuadTreeDrawing drawing;

    @ExpectedScenarioState(resolution = Resolution.NAME)
    Figure figureA;

    @ExpectedScenarioState(resolution = Resolution.NAME)
    Figure figureB;

    @ExpectedScenarioState(resolution = Resolution.NAME)
    Figure figureC;

    public WhenArrange figure_A_is_sent_to_the_front() {
        drawing.bringToFront(figureA);
        return self();
    }

    public WhenArrange figure_C_is_sent_to_the_back() {
        drawing.sendToBack(figureC);
        return self();
    }

    public WhenArrange figure_C_is_sent_to_the_front() {
        drawing.bringToFront(figureC);
        return self();
    }

    public WhenArrange figure_A_is_sent_to_the_back() {
        drawing.sendToBack(figureA);
        return self();
    }
}
