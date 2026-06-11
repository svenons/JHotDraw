package org.jhotdraw.draw.action;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.junit.ScenarioTest;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;

import org.jhotdraw.draw.*;
import org.jhotdraw.draw.figure.Figure;
import org.jhotdraw.draw.figure.GroupFigure;
import org.jhotdraw.draw.figure.RectangleFigure;

import java.util.Collection;

public class UngroupFeatureBDDTest extends ScenarioTest<
        UngroupFeatureBDDTest.GivenCanvasState,
        UngroupFeatureBDDTest.WhenUserActs,
        UngroupFeatureBDDTest.ThenCanvasState> {

    @Test
    public void ungrouping_a_selected_group_restores_individual_figures() {
        given().a_single_group_containing_two_figures_is_selected();
        when().the_ungroup_action_is_executed();
        then().the_group_is_dissolved_from_the_canvas()
            .and().the_individual_figures_are_restored();
    }

    public static class GivenCanvasState extends Stage<GivenCanvasState> {
        @ProvidedScenarioState DrawingView mockView;
        @ProvidedScenarioState DrawingEditor mockEditor;
        @ProvidedScenarioState Drawing mockDrawing;
        @ProvidedScenarioState GroupFigure mockSelectedGroup;

        public GivenCanvasState a_single_group_containing_two_figures_is_selected() {
            mockView = mock(DrawingView.class);
            mockEditor = mock(DrawingEditor.class);
            mockDrawing = mock(Drawing.class);
            mockSelectedGroup = new GroupFigure();
            
            mockSelectedGroup.add(new RectangleFigure());
            mockSelectedGroup.add(new RectangleFigure());

            Mockito.when(mockEditor.getActiveView()).thenReturn(mockView);
            Mockito.when(mockView.getDrawing()).thenReturn(mockDrawing);
            Mockito.when(mockView.getSelectionCount()).thenReturn(1);
            
            java.util.Set<Figure> selectedSet = new java.util.HashSet<>();
            selectedSet.add(mockSelectedGroup);
            Mockito.when(mockView.getSelectedFigures()).thenReturn(selectedSet);
            
            Mockito.when(mockDrawing.indexOf(mockSelectedGroup)).thenReturn(0);

            return this;
        }
    }

    public static class WhenUserActs extends Stage<WhenUserActs> {
        @ExpectedScenarioState DrawingView mockView;
        @ExpectedScenarioState DrawingEditor mockEditor;
        @ExpectedScenarioState GroupFigure mockSelectedGroup;
        @ProvidedScenarioState Collection<Figure> restoredFigures;

        public WhenUserActs the_ungroup_action_is_executed() {
            UngroupAction action = new UngroupAction(mockEditor);
            restoredFigures = action.ungroupFigures(mockView, mockSelectedGroup);
            return this;
        }
    }

    public static class ThenCanvasState extends Stage<ThenCanvasState> {
        @ExpectedScenarioState Drawing mockDrawing;
        @ExpectedScenarioState GroupFigure mockSelectedGroup;
        @ExpectedScenarioState Collection<Figure> restoredFigures;

        public ThenCanvasState the_group_is_dissolved_from_the_canvas() {
            verify(mockDrawing).remove(mockSelectedGroup);
            return this;
        }

        public ThenCanvasState the_individual_figures_are_restored() {
            assertThat(restoredFigures).hasSize(2);
            verify(mockDrawing).basicAddAll(0, restoredFigures);
            return this;
        }
    }
}