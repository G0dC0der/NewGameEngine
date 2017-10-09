package pojahn.game.entities;

import com.badlogic.gdx.audio.Sound;
import pojahn.game.core.MobileEntity;
import pojahn.game.essentials.Image2D;

public class Particle extends MobileEntity {

    Sound introSound;
    private boolean soundPlayed;

    public Particle() {
    }

    public static Particle fromSound(final Sound sound) {
        final Particle particle = new Particle();
        particle.setIntroSound(sound);

        return particle;
    }

    public Particle getClone() {
        final Particle clone = new Particle();
        copyData(clone);
        if (cloneEvent != null)
            cloneEvent.handleClonded(clone);

        return clone;
    }

    public void setIntroSound(final Sound introSound) {
        this.introSound = introSound;
    }

    @Override
    public void logistics() {
        if (!soundPlayed && introSound != null) {
            soundPlayed = true;
            sounds.play(introSound);
        }

        if (completed())
            getLevel().discard(this);
    }

    protected boolean completed() {
        return !isVisible() || getImage().hasEnded();
    }

    public static Particle from(final int animationSpeed, final Image2D[] image) {
        final Particle particle = new Particle();
        particle.setImage(animationSpeed, image);
        return particle;
    }

    protected void copyData(final Particle clone) {
        super.copyData(clone);
        clone.introSound = introSound;
    }
}