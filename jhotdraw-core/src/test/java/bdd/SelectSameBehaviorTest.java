package bdd;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.BeforeStage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.junit.ScenarioTest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import org.jhotdraw.draw.action.SelectSameAction;
import org.jhotdraw.draw.DefaultDrawing;
import org.jhotdraw.draw.DefaultDrawingEditor;
import org.jhotdraw.draw.DefaultDrawingView;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.figure.EllipseFigure;
import org.jhotdraw.draw.figure.Figure;
import org.jhotdraw.draw.figure.RectangleFigure;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD acceptance test for the Select Same subfeature.
 *
 * User story: As a user editing a drawing, I want to select every figure of
 * the same type as my current selection, so that I can edit all similar
 * figures together.
 *
 * Scenario:
 *   Given a drawing with 2 rectangles and 2 ellipses, and one rectangle selected
 *   When  the user selects same
 *   Then  the 2 rectangles are selected
 *   And   no ellipse is selected
 */
public class SelectSameBehaviorTest
        extends ScenarioTest<SelectSameBehaviorTest.GivenFigures,
                             SelectSameBehaviorTest.WhenSelecting,
                             SelectSameBehaviorTest.ThenSelection> {

    @Test
    public void selecting_same_selects_every_figure_of_the_same_type() {
        given().a_drawing_with_$_rectangles_and_$_ellipses(2, 2)
               .and().one_rectangle_is_selected();
        when().the_user_selects_same();
        then().the_$_rectangles_are_selected(2)
              .and().no_ellipse_is_selected();
    }

    // ----------------------------- GIVEN -----------------------------
    public static class GivenFigures extends Stage<GivenFigures> {
        @ProvidedScenarioState DrawingEditor editor;
        @ProvidedScenarioState DefaultDrawingView view;
        @ProvidedScenarioState Drawing drawing;
        @ProvidedScenarioState List<Figure> rectangles = new ArrayList<>();
        @ProvidedScenarioState List<Figure> ellipses = new ArrayList<>();

        @BeforeStage
        void setUpEditorAndView() {
            drawing = new DefaultDrawing();
            view = new DefaultDrawingView();
            view.setDrawing(drawing);
            editor = new DefaultDrawingEditor();
            editor.setActiveView(view);
        }

        public GivenFigures a_drawing_with_$_rectangles_and_$_ellipses(int rects, int ells) {
            for (int i = 0; i < rects; i++) {
                RectangleFigure r = new RectangleFigure();
                r.setSelectable(true);
                drawing.add(r);
                rectangles.add(r);
            }
            for (int i = 0; i < ells; i++) {
                EllipseFigure e = new EllipseFigure();
                e.setSelectable(true);
                drawing.add(e);
                ellipses.add(e);
            }
            return self();
        }

        public GivenFigures one_rectangle_is_selected() {
            view.addToSelection(rectangles.get(0));
            return self();
        }
    }

    // ----------------------------- WHEN -----------------------------
    public static class WhenSelecting extends Stage<WhenSelecting> {
        @ExpectedScenarioState DrawingEditor editor;

        public WhenSelecting the_user_selects_same() {
            new SelectSameAction(editor).actionPerformed(null);
            return self();
        }
    }

    // ----------------------------- THEN -----------------------------
    public static class ThenSelection extends Stage<ThenSelection> {
        @ExpectedScenarioState DefaultDrawingView view;
        @ExpectedScenarioState List<Figure> rectangles;
        @ExpectedScenarioState List<Figure> ellipses;

        public ThenSelection the_$_rectangles_are_selected(int count) {
            assertThat(view.getSelectedFigures())
                    .hasSize(count)
                    .containsAll(rectangles);
            return self();
        }

        public ThenSelection no_ellipse_is_selected() {
            assertThat(view.getSelectedFigures())
                    .doesNotContainAnyElementsOf(ellipses);
            return self();
        }
    }
}
