package pojahn.game.entities.particle;

import com.badlogic.gdx.audio.Sound;
import pojahn.game.core.MobileEntity;
import pojahn.game.essentials.Image2D;

import java.util.Collections;
import java.util.List;

public abstract class Particle extends MobileEntity {

    private Sound introSound;
    private boolean erupted;
    private List<Particle> introBits, outroBits;

    public Particle() {
        introBits = Collections.emptyList();
        outroBits = Collections.emptyList();
    }

    @Override
    public final void logistics() {
        if (!erupted) {
            erupted = true;
            sounds.play(introSound);
            erupt();

            introBits.forEach(bit -> getLevel().add(bit.getClone().center(this)));
        }

        step();

        if (completed()) {
            outroBits.forEach(bit -> getLevel().add(bit.getClone().center(this)));
            outro();
            getLevel().discard(this);
        }
    }

    protected abstract boolean completed();

    protected void erupt() {
    }

    protected void step() {
    }

    protected void outro() {
    }

    public abstract Particle getClone();

    public void setIntroSound(final Sound introSound) {
        this.introSound = introSound;
    }

    public void setIntroBits(final Particle... introBits) {
        this.introBits = List.of(introBits);
    }

    public void setOutroBits(final Particle... outroBits) {
        this.outroBits = List.of(outroBits);
    }

    protected void copyData(final Particle clone) {
        super.copyData(clone);
        clone.introSound = introSound;
        clone.introBits = introBits;
        clone.outroBits = outroBits;
    }

    public static Particle fadingParticle(final float fadeSpeed) {
        return new Particle() {
            @Override
            protected boolean completed() {
                return tint.a <= 0.0f;
            }

            @Override
            protected void step() {
                tint.a -= fadeSpeed;
            }

            @Override
            public Particle getClone() {
                final Particle clone = fadingParticle(fadeSpeed);
                copyData(clone);
                if (cloneEvent != null) {
                    cloneEvent.handleClonded(clone);
                }

                return clone;
            }
        };
    }

    public static Particle shrinkingParticle(final float shrinkSpeed) {
        return new Particle() {
            @Override
            protected boolean completed() {
                return scaleX <= 0.0f;
            }

            @Override
            protected void step() {
                scaleX -= shrinkSpeed;
                scaleY -= shrinkSpeed;
            }

            @Override
            public Particle getClone() {
                final Particle clone = shrinkingParticle(shrinkSpeed);
                copyData(clone);
                if (cloneEvent != null) {
                    cloneEvent.handleClonded(clone);
                }

                return clone;
            }
        };
    }

    public static Particle fromSound(final Sound sound) {
        return fromSound(sound, false);
    }

    public static Particle fromSound(final Sound sound, final boolean soundFalloff) {
        final Particle particle = new Particle() {
            @Override
            protected boolean completed() {
                return true;
            }

            @Override
            public Particle getClone() {
                final Particle clone = fromSound(sound, soundFalloff);
                copyData(clone);
                if (cloneEvent != null) {
                    cloneEvent.handleClonded(clone);
                }

                return clone;
            }
        };
        particle.setIntroSound(sound);
        particle.sounds.useFalloff = soundFalloff;

        return particle;
    }

    public static Particle imageParticle(final int speed, final Image2D... images) {
        final Particle particle = new Particle() {
            @Override
            protected boolean completed() {
                return !isVisible() || getImage().hasEnded();
            }

            @Override
            public Particle getClone() {
                final Particle clone = imageParticle(speed, images);
                copyData(clone);
                if (cloneEvent != null) {
                    cloneEvent.handleClonded(clone);
                }

                return clone;
            }
        };
        particle.setImage(speed, images);

        return particle;
    }
}