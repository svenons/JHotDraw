package org.jhotdraw.draw.figure;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


public class GroupFigureTest {
    private GroupFigure groupFigure;
    private Figure testChild;

    @Before
    public void setUp() {
        groupFigure = new GroupFigure();
        testChild = new RectangleFigure();
    }

    @Test
    public void testBasicAddBestCase() {
        groupFigure.basicAdd(testChild);

        assertEquals("The group should contain exactly 1 child", 1, groupFigure.getChildCount());
        assertTrue("Group should successfully contain the specific testChild", groupFigure.getChildren().contains(testChild));
    }

    @Test
    public void testBasicRemoveBoundaryCase() {
        int initialCount = groupFigure.getChildCount();
        
        groupFigure.basicRemove(testChild);
        
        assertEquals("Child count should safely remain 0 without crashing", 0, initialCount);
        assertEquals(0, groupFigure.getChildCount());
    }

    @Test
    public void testIsTransformableState() {
        groupFigure.basicAdd(testChild);
        
        assertEquals("Group transformable state must match child state", 
                     testChild.isTransformable(), 
                     groupFigure.isTransformable());
    }

    @Test
    public void testCloneCreatesIndependentCopy() {
        groupFigure.basicAdd(testChild);

        GroupFigure clonedGroup = (GroupFigure) groupFigure.clone();

        assertNotSame("Cloned group should be a distinct memory instance", groupFigure, clonedGroup);
        assertEquals("Cloned group should have the same number of children", 
                     groupFigure.getChildCount(), 
                     clonedGroup.getChildCount());

        clonedGroup.basicRemoveAllChildren();
        assertEquals("Original group should retain its children after clone is cleared", 1, groupFigure.getChildCount());
        assertEquals("Cloned group should be empty", 0, clonedGroup.getChildCount());
    }
}