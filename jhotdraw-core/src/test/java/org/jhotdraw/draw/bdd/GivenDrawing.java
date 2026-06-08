/*
 * Copyright (c) 2026 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.draw.bdd;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.ScenarioState.Resolution;
import org.jhotdraw.draw.QuadTreeDrawing;
import org.jhotdraw.draw.figure.Figure;

/**
 * JGiven Given-stage: sets up a QuadTreeDrawing with stub figures.
 * Lab 6 — BDDLab (Software Maintenance portfolio).
 */
public class GivenDrawing extends Stage<GivenDrawing> {

    @ProvidedScenarioState(resolution = Resolution.NAME)
    QuadTreeDrawing drawing;

    @ProvidedScenarioState(resolution = Resolution.NAME)
    Figure figureA;

    @ProvidedScenarioState(resolution = Resolution.NAME)
    Figure figureB;

    @ProvidedScenarioState(resolution = Resolution.NAME)
    Figure figureC;

    public GivenDrawing a_drawing_with_three_figures_A_B_C() {
        drawing = new QuadTreeDrawing();
        figureA = new StubFigure();
        figureB = new StubFigure();
        figureC = new StubFigure();
        drawing.add(figureA);
        drawing.add(figureB);
        drawing.add(figureC);
        return self();
    }
}
