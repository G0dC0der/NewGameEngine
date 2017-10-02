package pojahn.game.entities;

import com.badlogic.gdx.audio.Sound;
import pojahn.game.core.Collisions;
import pojahn.game.core.Entity;
import pojahn.game.core.MobileEntity;
import pojahn.game.entities.mains.GravityMan;
import pojahn.game.essentials.Direction;
import pojahn.lang.Entry;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Bouncer extends MobileEntity {

    private GravityMan[] targets;
    private Sound bounceSound;
    private Direction dir;
    private List<Entry<Integer, Entity>> soundControl;
    private float power;

    public Bouncer(final float x, final float y, final GravityMan... targets) {
        move(x, y);
        this.targets = targets;
        power = 150;
        soundControl = Stream
                .of(targets)
                .map(entity -> new Entry<Integer, Entity>(0, entity))
                .collect(Collectors.toList());
    }

    public void setPower(final float power) {
        this.power = power;
    }

    public void setBounceSound(final Sound bounceSound) {
        this.bounceSound = bounceSound;
    }

    public void setBouncingDirection(final Direction dir) {
        this.dir = dir;
    }

    @Override
    public void logistics() {
        for (final GravityMan man : targets) {
            if (collidesWith(man)) {
                final Direction dir;
                if (this.dir != null) {
                    dir = this.dir;
                } else {
                    dir = Collisions.getDirection(Collisions.normalize(centerX(), centerY(), man.prevX() - man.halfWidth(), man.prevY() - man.halfHeight()));
//                    dir = Direction.invert(dir);
                }

                switch (dir) {
                    case N:
                        man.vel.y = power;
                        break;
                    case NE:
                        man.vel.y = power;
                        man.vel.x = -power;
                        break;
                    case E:
                        man.vel.x = -power;
                        break;
                    case SE:
                        man.vel.y = -power;
                        man.vel.x = -power;
                        break;
                    case S:
                        man.vel.y = -power;
                        break;
                    case SW:
                        man.vel.y = -power;
                        man.vel.x = power;
                        break;
                    case W:
                        man.vel.x = power;
                        break;
                    case NW:
                        man.vel.y = power;
                        man.vel.x = power;
                        break;
                }
                if (bounceSound != null && soundAllowed(man)) {
                    bounceSound.play(sounds.calc());
                    addDelay(man, 40);
                }
            }
        }
        soundControl.forEach(entry -> entry.key--);
    }

    private boolean soundAllowed(final GravityMan man) {
        for (final Entry<Integer, Entity> entry : soundControl) {
            if (entry.key < 0 && entry.value == man)
                return true;
        }
        return false;
    }

    private void addDelay(final GravityMan man, final int delay) {
        for (final Entry<Integer, Entity> entry : soundControl) {
            if (entry.value == man) {
                entry.key = delay;
                return;
            }
        }
    }
}