package org.jhotdraw.draw.figure;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.awt.geom.Point2D;

public class GroupFigureTest {
    private GroupFigure groupFigure;
    private Figure testChild;
    private Figure testChild2;

    @Before
    public void setUp() {
        groupFigure = new GroupFigure();
        testChild = new RectangleFigure();
        testChild2 = new TextFigure();
    }

    @Test
    public void testBasicAddBestCase() {
        groupFigure.basicAdd(testChild);

        assertEquals("The group should contain exactly 1 child", 1, groupFigure.getChildCount());
        assertTrue("Group should successfully contain the specific testChild", groupFigure.getChildren().contains(testChild));
    }

    @Test
    public void testGroupDifferentFigureTypes() {
        groupFigure.basicAdd(testChild);
        groupFigure.basicAdd(testChild2);

        assertEquals("Group should hold exactly 2 children", 2, groupFigure.getChildCount());
        assertTrue("Group must contain the rectangle figure", groupFigure.getChildren().contains(testChild));
        assertTrue("Group must contain the text figure", groupFigure.getChildren().contains(testChild2));
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

    @Test
    public void testNestedGrouping() {
        GroupFigure innerGroup = new GroupFigure();
        innerGroup.basicAdd(testChild);
        
        GroupFigure outerGroup = new GroupFigure();
        outerGroup.basicAdd(innerGroup);
        
        assertEquals("Outer group should contain exactly 1 child (the inner group)", 1, outerGroup.getChildCount());
        assertTrue("Outer group must contain the inner group", outerGroup.getChildren().contains(innerGroup));
        
        GroupFigure retrievedInnerGroup = (GroupFigure) outerGroup.getChildren().iterator().next();
        assertTrue("Inner group must still contain the original shape", retrievedInnerGroup.getChildren().contains(testChild));
    }

    @Test
    public void testChopMethodCalculatesGeometricIntersection() {
        RectangleFigure rect1 = new RectangleFigure();
        rect1.setBounds(new Point2D.Double(0, 0), new Point2D.Double(10, 10));

        RectangleFigure rect2 = new RectangleFigure();
        rect2.setBounds(new Point2D.Double(100, 100), new Point2D.Double(110, 110));

        groupFigure.basicAdd(rect1);
        groupFigure.basicAdd(rect2);

        Point2D.Double fromPoint = new Point2D.Double(500, 500);

        Point2D.Double result = groupFigure.chop(fromPoint);

        assertNotNull("Chop should return a valid mathematical intersection point", result);

        assertTrue("Chop should snap to the closest child geometry", result.x > 50);
    }
}