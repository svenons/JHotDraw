package org.jhotdraw.draw.action;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static org.mockito.Mockito.*;

import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.figure.CompositeFigure;
import org.jhotdraw.draw.figure.Figure;

import java.util.Collection;
import java.util.Arrays;

@RunWith(MockitoJUnitRunner.class)
public class GroupUndoableEditTest {

    @Mock private AbstractGroupAction mockAction;
    @Mock private DrawingView mockView;
    @Mock private CompositeFigure mockGroup;
    @Mock private Figure mockFigure1;

    private Collection<Figure> figures;

    @Before
    public void setUp() {
        figures = Arrays.asList(mockFigure1);
    }

    @Test
    public void testUndoGroupingTriggersUngroupMath() throws Exception {
        GroupUndoableEdit edit = new GroupUndoableEdit(mockAction, mockView, mockGroup, figures, true);
        
        edit.undo();
        
        verify(mockAction, times(1)).ungroupFigures(mockView, mockGroup);
    }

    @Test
    public void testRedoGroupingTriggersGroupMath() throws Exception {
        GroupUndoableEdit edit = new GroupUndoableEdit(mockAction, mockView, mockGroup, figures, true);
        
        edit.undo(); 
        edit.redo();

        verify(mockAction, times(1)).groupFigures(mockView, mockGroup, figures);
    }

    @Test
    public void testUndoUngroupingTriggersGroupMath() throws Exception {
        GroupUndoableEdit edit = new GroupUndoableEdit(mockAction, mockView, mockGroup, figures, false);
        
        edit.undo();
        
        verify(mockAction, times(1)).groupFigures(mockView, mockGroup, figures);
    }
}