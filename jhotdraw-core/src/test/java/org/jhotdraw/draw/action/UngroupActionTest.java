package org.jhotdraw.draw.action;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static org.mockito.Mockito.*;

import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.figure.CompositeFigure;
import org.jhotdraw.draw.figure.Figure;
import org.jhotdraw.draw.figure.GroupFigure;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

@RunWith(MockitoJUnitRunner.class)
public class UngroupActionTest {
    @Mock private DrawingView mockView;
    @Mock private DrawingEditor mockEditor;
    @Mock private Drawing mockDrawing;
    @Mock private Figure mockFigure1;
    @Mock private Figure mockFigure2;

    private UngroupAction ungroupAction;

    @Before
    public void setUp() {
        when(mockEditor.getActiveView()).thenReturn(mockView);
        ungroupAction = new UngroupAction(mockEditor, new GroupFigure());
    }

    @Test
    public void testUpdateEnabledStateFailsWithMultipleSelections() {
        when(mockView.getSelectionCount()).thenReturn(2);
        ungroupAction.updateEnabledState();
        assertFalse("Should be disabled if multiple items are selected", ungroupAction.isEnabled());
    }

    @Test
    public void testUpdateEnabledStateSucceedsWithSingleGroupFigure() {
        when(mockView.getSelectionCount()).thenReturn(1);
        
        GroupFigure mockSelectedGroup = new GroupFigure();
        when(mockView.getSelectedFigures()).thenReturn(Collections.singleton(mockSelectedGroup));

        ungroupAction.updateEnabledState();
        assertTrue("Should be enabled when exactly one group is selected", ungroupAction.isEnabled());
    }

    @Test
    public void testUngroupFiguresExecutionPath() {
        CompositeFigure mockGroup = mock(CompositeFigure.class);
        Collection<Figure> children = Arrays.asList(mockFigure1, mockFigure2);
        
        when(mockGroup.getChildren()).thenReturn(new LinkedList<>(children));
        when(mockView.getDrawing()).thenReturn(mockDrawing);

        Collection<Figure> result = ungroupAction.ungroupFigures(mockView, mockGroup);

        verify(mockView).clearSelection();
        verify(mockGroup).basicRemoveAllChildren();
        verify(mockDrawing).remove(mockGroup);
        verify(mockView).addToSelection(result);

        assertEquals("Should successfully return the 2 extracted children", 2, result.size());
    }
}