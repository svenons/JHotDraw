package org.jhotdraw.draw.figure;

import java.awt.geom.*;
import org.jhotdraw.geom.Geom;

public class GroupFigure extends AbstractCompositeFigure {

    private static final long serialVersionUID = 1L;

    public GroupFigure() {
        setConnectable(false);
    }
    
    public Point2D.Double chop(Point2D.Double from) {
        Point2D.Double closestPoint = null;
        double minDistance = Double.MAX_VALUE;

        for (Figure child : getChildren()) {
            Rectangle2D.Double r = child.getBounds();
            Point2D.Double chopped = Geom.angleToPoint(r, Geom.pointToAngle(r, from));
            double distance = chopped.distanceSq(from); 
            
            if (distance < minDistance) {
                minDistance = distance;
                closestPoint = chopped;
            }
        }

        if (closestPoint == null) {
            Rectangle2D.Double r = getBounds();
            return Geom.angleToPoint(r, Geom.pointToAngle(r, from));
        }

        return closestPoint;
    }

    @Override
    public boolean isTransformable() {
        for (Figure f : children) {
            if (!f.isTransformable()) {
                return false;
            }
        }
        return true;
    }
}