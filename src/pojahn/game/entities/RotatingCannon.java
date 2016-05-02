package pojahn.game.entities;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.Collisions;
import pojahn.game.core.Entity;
import pojahn.game.core.Level;
import pojahn.game.essentials.Direction;

public class RotatingCannon extends Entity {
    
    private float rotationSpeed, targetRotation;
    private int reload, reloadCounter;
    private boolean disabled, diagonalFire, rotating, fireAll;
    private Projectile proj;
    private Particle firingAnim;
    private Sound firingSound;

    public RotatingCannon(float x, float y, Projectile proj) {
        move(x, y);
        this.proj = proj;
        rotationSpeed = 3;
        reload = 50;
    }
    
    public void setReloadTime(int reload) {
        this.reload = reload;
    }

    public void setRotationSpeed(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    public void setFireAnimation(Particle firingAnim) {
        this.firingAnim = firingAnim;
    }

    public void setFiringSound(Sound sound) {
        firingSound = sound;
    }
    
    public void disable(boolean disable) {
        this.disabled = disable;
    }

    public void fireAll(boolean fireAll) {
        this.fireAll = fireAll;
    }

    @Override
    public void logistics() {
        super.logistics();

        if (!disabled && --reloadCounter < 0) {
            if (!rotating) {
                Level l = getLevel();

                if (diagonalFire || fireAll) {
                    Vector2 spawn;

                    spawn = Collisions.rotatePoint(x() + width(), centerY(), centerX(), centerY(), 45);
                    Projectile proj1 = proj.getClone();
                    proj1.move(spawn.x - proj.halfWidth(), spawn.y - proj.halfHeight());
                    proj1.setManualTarget(getTarget(Direction.SE));
                    proj1.setRotation(45);
                    if (firingAnim != null)
                        l.add(firingAnim.getClone().move(spawn.x - firingAnim.halfWidth(), spawn.y - firingAnim.halfHeight()));

                    spawn = Collisions.rotatePoint(x() + width(), centerY(), centerX(), centerY(), 135);
                    Projectile proj2 = proj.getClone();
                    proj2.move(spawn.x - proj.halfWidth(), spawn.y - proj.halfHeight());
                    proj2.setManualTarget(getTarget(Direction.SW));
                    proj2.setRotation(135);
                    if (firingAnim != null)
                        l.add(firingAnim.getClone().move(spawn.x - firingAnim.halfWidth(), spawn.y - firingAnim.halfHeight()));

                    spawn = Collisions.rotatePoint(x() + width(), centerY(), centerX(), centerY(), 225);
                    Projectile proj3 = proj.getClone();
                    proj3.move(spawn.x - proj.halfWidth(), spawn.y - proj.halfHeight());
                    proj3.setManualTarget(getTarget(Direction.NW));
                    proj3.setRotation(225);
                    if (firingAnim != null)
                        l.add(firingAnim.getClone().move(spawn.x - firingAnim.halfWidth(), spawn.y - firingAnim.halfHeight()));

                    spawn = Collisions.rotatePoint(x() + width(), centerY(), centerX(), centerY(), 315);
                    Projectile proj4 = proj.getClone();
                    proj4.move(spawn.x - proj.halfWidth(), spawn.y - proj.halfHeight());
                    proj4.setManualTarget(getTarget(Direction.NE));
                    proj4.setRotation(315);
                    if (firingAnim != null)
                        l.add(firingAnim.getClone().move(spawn.x - firingAnim.halfWidth(), spawn.y - firingAnim.halfHeight()));

                    l.add(proj1);
                    l.add(proj2);
                    l.add(proj3);
                    l.add(proj4);
                }

                if (!diagonalFire || fireAll) {
                    Projectile proj1 = proj.getClone();
                    proj1.move(centerX() - (proj.width() / 2), y() - (proj.height() / 2));
                    proj1.setManualTarget(centerX() - (proj.width() / 2), 0);
                    proj1.setRotation(270);
                    if (firingAnim != null)
                        l.add(firingAnim.getClone().move(centerX() - firingAnim.width() / 2, y() - firingAnim.height() / 2));

                    Projectile proj2 = proj.getClone();
                    proj2.move(x() + width() - (proj.width() / 2), centerY() - (proj.height() / 2));
                    proj2.setManualTarget(l.getWidth(), centerY() - (proj.height() / 2));
                    proj2.setRotation(0);
                    if (firingAnim != null)
                        l.add(firingAnim.getClone().move(x() + width() - (firingAnim.width() / 2), centerY() - firingAnim.height() / 2));

                    Projectile proj3 = proj.getClone();
                    proj3.move(centerX() - (proj.width() / 2), y() + height() - (proj.height() / 2));
                    proj3.setManualTarget(centerX() - (proj.width() / 2), l.getWidth());
                    proj3.setRotation(90);
                    if (firingAnim != null)
                        l.add(firingAnim.getClone().move(centerX() - firingAnim.width() / 2, y() + height() - firingAnim.height() / 2));

                    Projectile proj4 = proj.getClone();
                    proj4.move(x() - (proj.width() / 2), centerY() - (proj.height() / 2));
                    proj4.setManualTarget(0, centerY() - (proj.height() / 2));
                    proj4.setRotation(180);
                    if (firingAnim != null)
                        l.add(firingAnim.getClone().move(x() - (firingAnim.width() / 2), centerY() - firingAnim.height() / 2));

                    l.add(proj1);
                    l.add(proj2);
                    l.add(proj3);
                    l.add(proj4);
                }

                if(firingSound != null)
                    firingSound.play(sounds.calc());
                    
                reloadCounter = reload;
                rotating = true;
                targetRotation = bounds.rotation + 45;

                if (bounds.rotation > 360) {
                    bounds.rotation = 0;
                    targetRotation = 45;
                }
            } else {
                bounds.rotation += rotationSpeed;
                if (bounds.rotation > targetRotation)
                    bounds.rotation = targetRotation;

                if (bounds.rotation == targetRotation) {
                    bounds.rotation = bounds.rotation >= 360 ? 0 : bounds.rotation;

                    rotating = false;
                    diagonalFire = !diagonalFire;
                }
            }
        }
    }

    private Vector2 getTarget(Direction dir) {
        final Level l = getLevel();
        float middleX = centerX();
        float middleY = centerY();
        float x, y;

        switch (dir) {
            case NW:
                x = middleX - 1;
                y = middleY - 1;
                return Collisions.findEdgePoint(middleX, middleY, x, y, l);

            case N:
                x = middleX;
                y = middleY - 1;
                return Collisions.findEdgePoint(middleX, middleY, x, y, l);

            case NE:
                x = middleX + 1;
                y = middleY - 1;
                return Collisions.findEdgePoint(middleX, middleY, x, y, l);

            case E:
                x = middleX + 1;
                y = middleY;
                return Collisions.findEdgePoint(middleX, middleY, x, y, l);

            case SE:
                x = middleX + 1;
                y = middleY + 1;
                return Collisions.findEdgePoint(middleX, middleY, x, y, l);

            case S:
                x = middleX;
                y = middleY + 1;
                return Collisions.findEdgePoint(middleX, middleY, x, y, l);

            case SW:
                x = middleX - 1;
                y = middleY + 1;
                return Collisions.findEdgePoint(middleX, middleY, x, y, l);

            case W:
                x = middleX - 1;
                y = middleY;
                return Collisions.findEdgePoint(middleX, middleY, x, y, l);

            default:
                return null;
        }
    }
}