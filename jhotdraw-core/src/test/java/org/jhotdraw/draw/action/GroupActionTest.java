package org.jhotdraw.draw.action;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.figure.CompositeFigure;
import org.jhotdraw.draw.figure.Figure;

import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class GroupActionTest {
    @Mock private DrawingView mockView;
    @Mock private DrawingEditor mockEditor;
    @Mock private Drawing mockDrawing;
    @Mock private Figure mockFigure1;
    @Mock private Figure mockFigure2;

    private GroupAction groupAction;

    @Before
    public void setUp() {
        when(mockEditor.getActiveView()).thenReturn(mockView);
        groupAction = new GroupAction(mockEditor);
    }

    @Test
    public void testUpdateEnabledStateAllowsGroupingWithMultipleSelections() {
        when(mockView.getSelectionCount()).thenReturn(2);
        groupAction.updateEnabledState();
        assertTrue("Should be enabled with 2 or more items selected", groupAction.isEnabled());
    }

    @Test
    public void testUpdateEnabledStateBlocksGroupingWithSingleSelection() {
        when(mockView.getSelectionCount()).thenReturn(1);
        groupAction.updateEnabledState();
        assertFalse("Should be disabled with only 1 item selected", groupAction.isEnabled());
    }

    @Test
    public void testAbstractGroupActionGroupFiguresExecutionPath() {
        List<Figure> figuresToGroup = Arrays.asList(mockFigure1, mockFigure2);
        CompositeFigure mockGroup = mock(CompositeFigure.class);
        
        when(mockView.getDrawing()).thenReturn(mockDrawing);
        when(mockDrawing.sort(figuresToGroup)).thenReturn(figuresToGroup);
        when(mockDrawing.indexOf(any())).thenReturn(0);

        groupAction.groupFigures(mockView, mockGroup, figuresToGroup);

        verify(mockDrawing).basicRemoveAll(figuresToGroup);
        verify(mockView).clearSelection();
        verify(mockDrawing).add(0, mockGroup);
        verify(mockGroup, times(1)).basicAdd(mockFigure1);
        verify(mockGroup, times(1)).basicAdd(mockFigure2);
        verify(mockView).addToSelection(mockGroup);
    }
}