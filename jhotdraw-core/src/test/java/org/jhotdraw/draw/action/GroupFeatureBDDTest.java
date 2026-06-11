package org.jhotdraw.draw.action;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.junit.ScenarioTest;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.jhotdraw.draw.*;
import org.jhotdraw.draw.figure.Figure;
import org.jhotdraw.draw.figure.GroupFigure;
import org.jhotdraw.draw.figure.RectangleFigure;

import java.util.Arrays;
import java.util.List;

public class GroupFeatureBDDTest extends ScenarioTest<
        GroupFeatureBDDTest.GivenCanvasState,
        GroupFeatureBDDTest.WhenUserActs,
        GroupFeatureBDDTest.ThenCanvasState> {
    
    @Test
    public void grouping_multiple_selected_figures_creates_a_single_group() {
        given().two_individual_figures_are_selected_on_the_canvas();
        when().the_group_action_is_executed();
        then().a_single_group_is_created()
            .and().the_group_contains_exactly_two_items();
    }

    public static class GivenCanvasState extends Stage<GivenCanvasState> {
        @ProvidedScenarioState DrawingView mockView;
        @ProvidedScenarioState DrawingEditor mockEditor;
        @ProvidedScenarioState Drawing mockDrawing;
        @ProvidedScenarioState List<Figure> selectedFigures;

        public GivenCanvasState two_individual_figures_are_selected_on_the_canvas() {
            mockView = mock(DrawingView.class);
            mockEditor = mock(DrawingEditor.class);
            mockDrawing = mock(Drawing.class);

            Figure fig1 = new RectangleFigure();
            Figure fig2 = new RectangleFigure();
            selectedFigures = Arrays.asList(fig1, fig2);

            Mockito.when(mockEditor.getActiveView()).thenReturn(mockView);
            Mockito.when(mockView.getDrawing()).thenReturn(mockDrawing);
            Mockito.when(mockView.getSelectionCount()).thenReturn(2);
            Mockito.when(mockDrawing.sort(any())).thenReturn(selectedFigures);

            return this;
        }
    }

    public static class WhenUserActs extends Stage<WhenUserActs> {
        @ExpectedScenarioState DrawingView mockView;
        @ExpectedScenarioState DrawingEditor mockEditor;
        @ExpectedScenarioState List<Figure> selectedFigures;
        @ProvidedScenarioState GroupFigure resultingGroup;

        public WhenUserActs the_group_action_is_executed() {
            GroupAction action = new GroupAction(mockEditor);
            resultingGroup = new GroupFigure();
            action.groupFigures(mockView, resultingGroup, selectedFigures);
            return this;
        }
    }

    public static class ThenCanvasState extends Stage<ThenCanvasState> {
        @ExpectedScenarioState GroupFigure resultingGroup;
        @ExpectedScenarioState Drawing mockDrawing;

        public ThenCanvasState a_single_group_is_created() {
            assertThat(resultingGroup).isNotNull().isInstanceOf(GroupFigure.class);
            verify(mockDrawing).add(0, resultingGroup);
            return this;
        }

        public ThenCanvasState the_group_contains_exactly_two_items() {
            assertThat(resultingGroup.getChildCount()).isEqualTo(2);
            return this;
        }
    }
}