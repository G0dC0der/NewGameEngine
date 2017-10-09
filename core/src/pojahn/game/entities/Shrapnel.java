package pojahn.game.entities;

import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.BaseLogic;
import pojahn.game.core.Level;

public class Shrapnel extends Particle {
    private final Projectile split;
    private boolean once;

    public Shrapnel(final Projectile shrapnel) {
        this.split = shrapnel;
    }

    @Override
    public Shrapnel getClone() {
        final Shrapnel s = new Shrapnel(split);
        copyData(s);
        if (cloneEvent != null)
            cloneEvent.handleClonded(s);

        return s;
    }

    @Override
    public void logistics() {
        if (!once) {
            once = true;

            final Vector2[] edgePoints = getEightDirection();
            final Level l = getLevel();

            for (final Vector2 edgePoint : edgePoints) {
                final Projectile proj = split.getClone();
                proj.center(this);
                proj.setTarget(edgePoint);
                l.add(proj);
            }

        }

        super.logistics();
    }

    private Vector2[] getEightDirection() {
        final float middleX = centerX();
        final float middleY = centerY();
        float x;
        float y;

        //NW Point
        x = middleX - 1;
        y = middleY - 1;
        final Vector2 p1 = BaseLogic.findEdgePoint(middleX, middleY, x, y, getLevel());

        //N Point
        x = middleX;
        y = middleY - 1;
        final Vector2 p2 = BaseLogic.findEdgePoint(middleX, middleY, x, y, getLevel());

        //NE Point
        x = middleX + 1;
        y = middleY - 1;
        final Vector2 p3 = BaseLogic.findEdgePoint(middleX, middleY, x, y, getLevel());

        //E Point
        x = middleX + 1;
        y = middleY;
        final Vector2 p4 = BaseLogic.findEdgePoint(middleX, middleY, x, y, getLevel());

        //SE Point
        x = middleX + 1;
        y = middleY + 1;
        final Vector2 p5 = BaseLogic.findEdgePoint(middleX, middleY, x, y, getLevel());

        //S Point
        x = middleX;
        y = middleY + 1;
        final Vector2 p6 = BaseLogic.findEdgePoint(middleX, middleY, x, y, getLevel());

        //SW Point
        x = middleX - 1;
        y = middleY + 1;
        final Vector2 p7 = BaseLogic.findEdgePoint(middleX, middleY, x, y, getLevel());

        //W Point
        x = middleX - 1;
        y = middleY;
        final Vector2 p8 = BaseLogic.findEdgePoint(middleX, middleY, x, y, getLevel());

        return new Vector2[]{p1, p2, p3, p4, p5, p6, p7, p8};
    }
}