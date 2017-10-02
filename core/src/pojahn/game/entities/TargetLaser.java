package pojahn.game.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.Collisions;
import pojahn.game.core.Entity;
import pojahn.game.core.Level;
import pojahn.game.core.Level.Tile;
import pojahn.game.essentials.LaserBeam;

public class TargetLaser extends PathDrone {

    private boolean stop, faceTarget, infBeam, frontFire, highLaserPrio;
    private int delay, delayCounter;
    private Tile stopTile;
    private Entity targets[], laserTarget;
    private Particle impact;
    private LaserBeam beam;
    private Color laserTint;

    public TargetLaser(final float x, final float y, final Entity... targets) {
        super(x, y);

        this.targets = targets;
        this.laserTarget = laserTarget;
        delay = 1;
        stopTile = Tile.SOLID;
        infBeam = true;
        delayCounter = 10;
        laserTint = Color.valueOf("CC0000FF");
    }

    @Override
    public TargetLaser getClone() {
        final TargetLaser clone = new TargetLaser(x(), y(), targets);
        copyData(clone);
        if (cloneEvent != null)
            cloneEvent.handleClonded(clone);

        return clone;
    }

    @Override
    public void logistics() {
        super.logistics();

        if (stop || !laserTarget.isActive())
            return;

        float cx = centerX();
        float cy = centerY();
        final float tcx = laserTarget.centerX();
        final float tcy = laserTarget.centerY();
        final Level l = getLevel();
        Vector2 finalTarget;

        if (infBeam) {
            finalTarget = Collisions.searchTile((int) cx, (int) cy, (int) tcx, (int) tcy, true, stopTile, l);
            if (finalTarget == null)
                finalTarget = Collisions.findEdgePoint(cy, cy, tcx, tcy, l);
        } else
            finalTarget = new Vector2(tcx, tcy);

        if (impact != null && ++delayCounter % delay == 0)
            l.add(impact.getClone().move(finalTarget.x - impact.halfWidth(), finalTarget.y - impact.halfHeight()));

        for (final Entity entity : targets)
            if (entity.hasActionEvent() && Collisions.lineRectangle((int) cx, (int) cy, (int) finalTarget.x, (int) finalTarget.y, entity.bounds.toRectangle()))
                entity.runActionEvent(this);

        if (faceTarget)
            bounds.rotation = (float) Collisions.getAngle(cx, cy, finalTarget.x, finalTarget.y);

        if (frontFire) {
            final Vector2 front = getFrontPosition();
            cx = front.x;
            cy = front.y;
        }

        beam.fireAt(cx, cy, finalTarget.x, finalTarget.y, 1);
    }

    public void setLaserBeam(final LaserBeam beam) {
        this.beam = beam;
    }

    public void setLaserTarget(final Entity laserTarget) {
        this.laserTarget = laserTarget;
    }

    public void setStopTile(final Tile stopTile) {
        this.stopTile = stopTile;
    }

    public void setExplosion(final Particle impact) {
        this.impact = impact;
    }

    public void infiniteBeam(final boolean infBeam) {
        this.infBeam = infBeam;
    }

    public void frontFire(final boolean frontFire) {
        this.frontFire = frontFire;
    }

    public void setExplosionDelay(final int delay) {
        if (delay <= 0)
            throw new IllegalArgumentException("delay must be exceed 0: " + delay);

        this.delay = delay;
    }

    public void faceTarget(final boolean specEffect) {
        this.faceTarget = specEffect;
    }

    public void stop(final boolean stop) {
        this.stop = stop;
    }

    public boolean stopped() {
        return stop;
    }

    public void setLaserTint(final Color tint) {
        this.laserTint = tint;
    }

    public void highLaserPrio(final boolean highLaserPrio) {
        this.highLaserPrio = highLaserPrio;
    }

    @Override
    public void render(final SpriteBatch b) {
        final Color defaultColor = b.getColor();

        if (highLaserPrio) {
            super.render(b);

            if (laserTint != null)
                b.setColor(laserTint);
            beam.drawLasers(b);
            b.setColor(defaultColor);
        } else {
            if (laserTint != null)
                b.setColor(laserTint);
            beam.drawLasers(b);
            b.setColor(defaultColor);

            super.render(b);
        }
    }

    protected void copyData(final TargetLaser clone) {
        super.copyData(clone);
        clone.stop = stop;
        clone.faceTarget = faceTarget;
        clone.infBeam = infBeam;
        clone.frontFire = frontFire;
        clone.highLaserPrio = highLaserPrio;
        clone.stopTile = stopTile;
        clone.laserTarget = laserTarget;
        clone.impact = impact;
        clone.beam = beam;
        clone.laserTint = laserTint;
    }
}