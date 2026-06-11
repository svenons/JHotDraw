package bdd;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.BeforeStage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.junit.ScenarioTest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import org.jhotdraw.action.edit.ClearSelectionAction;
import org.jhotdraw.draw.DefaultDrawing;
import org.jhotdraw.draw.DefaultDrawingEditor;
import org.jhotdraw.draw.DefaultDrawingView;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.figure.Figure;
import org.jhotdraw.draw.figure.RectangleFigure;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD acceptance test for the Deselect All subfeature.
 *
 * User story: As a user editing a drawing, I want to clear my whole selection
 * in one step, so that I can start a fresh selection without unpicking figures
 * one by one.
 *
 * Scenario:
 *   Given a drawing with 3 figures and all of them selected
 *   When  the user deselects all
 *   Then  no figures are selected
 */
public class ClearSelectionBehaviorTest
        extends ScenarioTest<ClearSelectionBehaviorTest.GivenFigures,
                             ClearSelectionBehaviorTest.WhenSelecting,
                             ClearSelectionBehaviorTest.ThenSelection> {

    @Test
    public void deselecting_all_clears_the_whole_selection() {
        given().a_drawing_with_$_rectangles(3).and().all_figures_are_selected();
        when().the_user_deselects_all();
        then().no_figures_are_selected();
    }

    // ----------------------------- GIVEN -----------------------------
    public static class GivenFigures extends Stage<GivenFigures> {
        @ProvidedScenarioState DrawingEditor editor;
        @ProvidedScenarioState DefaultDrawingView view;
        @ProvidedScenarioState Drawing drawing;
        @ProvidedScenarioState List<Figure> rectangles = new ArrayList<>();

        @BeforeStage
        void setUpEditorAndView() {
            drawing = new DefaultDrawing();
            view = new DefaultDrawingView();
            view.setDrawing(drawing);
            editor = new DefaultDrawingEditor();
            editor.setActiveView(view);
        }

        public GivenFigures a_drawing_with_$_rectangles(int count) {
            for (int i = 0; i < count; i++) {
                RectangleFigure r = new RectangleFigure();
                r.setSelectable(true);
                drawing.add(r);
                rectangles.add(r);
            }
            return self();
        }

        public GivenFigures all_figures_are_selected() {
            for (Figure f : rectangles) view.addToSelection(f);
            return self();
        }
    }

    // ----------------------------- WHEN -----------------------------
    public static class WhenSelecting extends Stage<WhenSelecting> {
        @ExpectedScenarioState DefaultDrawingView view;

        public WhenSelecting the_user_deselects_all() {
            new ClearSelectionAction(view).actionPerformed(null);
            return self();
        }
    }

    // ----------------------------- THEN -----------------------------
    public static class ThenSelection extends Stage<ThenSelection> {
        @ExpectedScenarioState DefaultDrawingView view;

        public ThenSelection no_figures_are_selected() {
            assertThat(view.getSelectedFigures()).isEmpty();
            return self();
        }
    }
}
