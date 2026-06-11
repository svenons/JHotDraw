package bdd;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.BeforeStage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.junit.ScenarioTest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import org.jhotdraw.action.edit.SelectAllAction;
import org.jhotdraw.draw.DefaultDrawing;
import org.jhotdraw.draw.DefaultDrawingEditor;
import org.jhotdraw.draw.DefaultDrawingView;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.figure.Figure;
import org.jhotdraw.draw.figure.RectangleFigure;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD acceptance test for the Select All subfeature.
 *
 * User story: As a user editing a drawing, I want to select every figure
 * at once, so that I can apply one operation to the whole drawing without
 * clicking each figure individually.
 *
 * Scenario:
 *   Given a drawing with 3 figures
 *   When  the user selects all
 *   Then  all 3 figures are selected
 */
public class SelectAllBehaviorTest
        extends ScenarioTest<SelectAllBehaviorTest.GivenFigures,
                             SelectAllBehaviorTest.WhenSelecting,
                             SelectAllBehaviorTest.ThenSelection> {

    @Test
    public void selecting_all_figures_selects_every_figure_on_the_drawing() {
        given().a_drawing_with_$_rectangles(3);
        when().the_user_selects_all();
        then().all_$_figures_are_selected(3);
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
    }

    // ----------------------------- WHEN -----------------------------
    public static class WhenSelecting extends Stage<WhenSelecting> {
        @ExpectedScenarioState DefaultDrawingView view;

        public WhenSelecting the_user_selects_all() {
            new SelectAllAction(view).actionPerformed(null);
            return self();
        }
    }

    // ----------------------------- THEN -----------------------------
    public static class ThenSelection extends Stage<ThenSelection> {
        @ExpectedScenarioState DefaultDrawingView view;

        public ThenSelection all_$_figures_are_selected(int count) {
            assertThat(view.getSelectedFigures()).hasSize(count);
            return self();
        }
    }
}
