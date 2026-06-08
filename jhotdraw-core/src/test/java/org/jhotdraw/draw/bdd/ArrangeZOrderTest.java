/*
 * Copyright (c) 2026 The authors and contributors of JHotDraw.
 */
package org.jhotdraw.draw.bdd;

import com.tngtech.jgiven.junit.ScenarioTest;
import org.junit.Test;

/**
 * BDD scenarios for the Arrange (Send to Front / Send to Back) feature.
 *
 * <p>Uses JGiven stage classes (GivenDrawing, WhenArrange, ThenZOrder) and AssertJ assertions,
 * all operating at the domain model level (no Swing required).
 *
 * Lab 6 — BDDLab (Software Maintenance portfolio).
 */
public class ArrangeZOrderTest extends ScenarioTest<GivenDrawing, WhenArrange, ThenZOrder> {

    @Test
    public void send_figure_to_front() {
        given().a_drawing_with_three_figures_A_B_C();
        when().figure_A_is_sent_to_the_front();
        then().figure_A_is_at_the_front();
    }

    @Test
    public void send_figure_to_back() {
        given().a_drawing_with_three_figures_A_B_C();
        when().figure_C_is_sent_to_the_back();
        then().figure_C_is_at_the_back();
    }

    @Test
    public void sending_front_figure_to_front_is_a_no_op() {
        given().a_drawing_with_three_figures_A_B_C();
        when().figure_C_is_sent_to_the_front();
        then().figure_C_is_still_at_the_front();
    }

    @Test
    public void sending_back_figure_to_back_is_a_no_op() {
        given().a_drawing_with_three_figures_A_B_C();
        when().figure_A_is_sent_to_the_back();
        then().figure_A_is_still_at_the_back();
    }
}
