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
import org.jhotdraw.draw.figure.GroupFigure;
import org.jhotdraw.draw.figure.CompositeFigure;
import org.jhotdraw.draw.figure.Figure;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
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
        groupAction = new GroupAction(mockEditor);

        when(mockEditor.getActiveView()).thenReturn(mockView);
    }

    @Test
    public void testCanGroupWithSufficientSelectionBestCase() {
        when(mockView.getSelectionCount()).thenReturn(2);

        boolean result = groupAction.canGroup();

        assertTrue("Should be able to group with 2 or more items selected", result);
        verify(mockView, times(1)).getSelectionCount();
    }

    @Test
    public void testUngroupFiguresExecutionPath() {
        CompositeFigure mockGroup = mock(CompositeFigure.class);
        Collection<Figure> children = Arrays.asList(mockFigure1, mockFigure2);
        
        when(mockGroup.getChildren()).thenReturn(new LinkedList<>(children));
        when(mockView.getDrawing()).thenReturn(mockDrawing);

        Collection<Figure> result = groupAction.ungroupFigures(mockView, mockGroup);

        verify(mockView).clearSelection();
        verify(mockGroup).basicRemoveAllChildren();
        verify(mockDrawing).remove(mockGroup);
        verify(mockView).addToSelection(result);

        assertEquals("Should successfully return the 2 extracted children", 2, result.size());
    }

    @Test
    public void testCannotGroupWithOnlyOneSelection() {
        when(mockView.getSelectionCount()).thenReturn(1);

        boolean canGroup = groupAction.canGroup();

        assertFalse("Should not be able to group only 1 item", canGroup);

        verify(mockView, times(1)).getSelectionCount();
    }

    @Test
    public void testGroupFiguresExecutionPath() {
        // Setup mock figures and a fake collection
        List<Figure> figuresToGroup = Arrays.asList(mockFigure1, mockFigure2);
        CompositeFigure mockGroup = mock(CompositeFigure.class);
        
        // Stub the drawing model
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

    @Test
    public void testCanUngroupFailsWithMultipleSelections() {
        when(mockView.getSelectionCount()).thenReturn(2);

        GroupAction ungroupAction = new GroupAction(mockEditor, new GroupFigure(), false);
        boolean result = ungroupAction.canUngroup();

        assertFalse("Should not be able to ungroup if multiple items are selected", result);
    }
}
