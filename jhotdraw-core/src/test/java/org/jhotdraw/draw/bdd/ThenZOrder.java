/*
 * Copyright (c) 2026 The authors and contributors of JHotDraw.
 */
package org.jhotdraw.draw.bdd;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ScenarioState.Resolution;
import org.assertj.core.api.Assertions;
import org.jhotdraw.draw.QuadTreeDrawing;
import org.jhotdraw.draw.figure.Figure;

import java.util.List;

/**
 * JGiven Then-stage: asserts z-order positions using AssertJ.
 * Lab 6 — BDDLab (Software Maintenance portfolio).
 */
public class ThenZOrder extends Stage<ThenZOrder> {

    @ExpectedScenarioState(resolution = Resolution.NAME)
    QuadTreeDrawing drawing;

    @ExpectedScenarioState(resolution = Resolution.NAME)
    Figure figureA;

    @ExpectedScenarioState(resolution = Resolution.NAME)
    Figure figureC;

    public ThenZOrder figure_A_is_at_the_front() {
        List<Figure> children = drawing.getChildren();
        Assertions.assertThat(children.get(children.size() - 1))
                .as("figureA should be the topmost (last) child")
                .isSameAs(figureA);
        return self();
    }

    public ThenZOrder figure_C_is_at_the_back() {
        Assertions.assertThat(drawing.getChild(0))
                .as("figureC should be the bottommost (first) child")
                .isSameAs(figureC);
        return self();
    }

    public ThenZOrder figure_C_is_still_at_the_front() {
        List<Figure> children = drawing.getChildren();
        Assertions.assertThat(children.get(children.size() - 1))
                .as("figureC should still be at the front")
                .isSameAs(figureC);
        return self();
    }

    public ThenZOrder figure_A_is_still_at_the_back() {
        Assertions.assertThat(drawing.getChild(0))
                .as("figureA should still be at the back")
                .isSameAs(figureA);
        return self();
    }
}
