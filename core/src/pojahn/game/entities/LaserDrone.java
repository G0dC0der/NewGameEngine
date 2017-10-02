package pojahn.game.entities;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.Collisions;
import pojahn.game.core.Entity;
import pojahn.game.core.Level.Tile;
import pojahn.game.essentials.LaserBeam;

import java.util.stream.Stream;

public class LaserDrone extends PathDrone {

    private float targetX, targetY;
    private int laserStartup, laserDuration, reload, sucounter, ducounter, reloadCounter;
    private Tile stopTile;
    private boolean fireAtVisible, firing, allowFiringSound, ignoreInactive;
    private Particle exp;
    private Color laserTint;
    private final Entity[] targets;
    private LaserBeam firingBeam, chargeBeam;
    private Sound startupSound, firingSound;

    public LaserDrone(final float x, final float y, final int laserStartup, final int laserDuration, final int reload, final Entity... targets) {
        super(x, y);
        this.laserStartup = laserStartup;
        this.laserDuration = laserDuration;
        this.targets = targets;
        this.reload = reload;
        this.fireAtVisible = true;
        targetX = targetY = -1;
        sucounter = ducounter = reloadCounter = 0;
        stopTile = Tile.SOLID;
        laserTint = Color.valueOf("CC0000FF");
        ignoreInactive = true;
    }

    public void setStartupSound(final Sound startupSound) {
        this.startupSound = startupSound;
    }

    public void setFiringSound(final Sound firingSound) {
        this.firingSound = firingSound;
    }

    public void setExplosion(final Particle exp) {
        this.exp = exp;
    }

    public boolean haveTarget() {
        return targetX != -1;
    }

    public void setStopTile(final Tile stopTile) {
        this.stopTile = stopTile;
    }

    public void fireAtVisible(final boolean fireAtVisible) {
        this.fireAtVisible = fireAtVisible;
    }

    public void setLaserTint(final Color tint) {
        this.laserTint = tint;
    }

    public LaserBeam getFiringBeam() {
        return firingBeam;
    }

    public void setFiringBeam(final LaserBeam firingBeam) {
        this.firingBeam = firingBeam;
    }

    public LaserBeam getChargeBeam() {
        return chargeBeam;
    }

    public void setChargeBeam(final LaserBeam chargeBeam) {
        this.chargeBeam = chargeBeam;
    }

    public void setIgnoreInactive(final boolean ignoreInactive) {
        this.ignoreInactive = ignoreInactive;
    }

    @Override
    public void logistics() {
        if (--reloadCounter > 0) {
            if (!fireAtVisible)
                super.logistics();
            return;
        }

        if (!haveTarget()) {
            allowFiringSound = true;
            Entity target = null;
            if (fireAtVisible)
                target = Collisions.findClosestSeeable(this, getTargets());
            else
                target = Collisions.findClosest(this, getTargets());

            if (target != null) {
                final int x1 = (int) (x() + width() / 2);
                final int y1 = (int) (y() + height() / 2);
                final int x2 = (int) (target.x() + target.width() / 2);
                final int y2 = (int) (target.y() + target.height() / 2);

                Vector2 wallPoint = Collisions.searchTile(x1, y1, x2, y2, true, stopTile, getLevel());
                if (wallPoint == null)
                    wallPoint = Collisions.findEdgePoint(x1, y1, x2, y2, getLevel());

                targetX = wallPoint.x;
                targetY = wallPoint.y;

                if (startupSound != null)
                    startupSound.play(sounds.calc());
            } else
                super.logistics();
        }
        if (haveTarget()) {
            if (!fireAtVisible)
                super.logistics();

            if (!firing && ++sucounter % laserStartup == 0) {
                firing = true;

                if (exp != null)
                    getLevel().add(exp.getClone().move(targetX - exp.width() / 2, targetY - exp.height() / 2));
            }
            if (firing) {
                firingBeam.fireAt(x() + width() / 2, y() + height() / 2, targetX, targetY, 1);

                if (startupSound != null)
                    startupSound.stop();

                if (firingSound != null && allowFiringSound) {
                    firingSound.play(sounds.calc());
                    allowFiringSound = false;
                }

                for (final Entity entity : targets)
                    if (entity.hasActionEvent() && Collisions.lineRectangle((int) x(), (int) y(), (int) targetX, (int) targetY, entity.bounds.toRectangle()))
                        entity.runActionEvent(this);

                if (++ducounter % laserDuration == 0) {
                    targetX = targetY = -1;
                    reloadCounter = reload;
                    firing = false;
                }
            } else if (chargeBeam != null)
                chargeBeam.fireAt(x() + width() / 2, y() + height() / 2, targetX, targetY, 1);
        }
    }

    private Entity[] getTargets() {
        return Stream.of(targets)
                .filter(entity -> !ignoreInactive || entity.isActive())
                .toArray(Entity[]::new);
    }

    @Override
    public void render(final SpriteBatch b) {
        super.render(b);

        final Color defaultColor = b.getColor();

        if (laserTint != null)
            b.setColor(laserTint);

        if (chargeBeam != null)
            chargeBeam.drawLasers(b);
        firingBeam.drawLasers(b);

        b.setColor(defaultColor);
    }
}