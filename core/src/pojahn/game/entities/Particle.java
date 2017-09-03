package pojahn.game.entities;

import com.badlogic.gdx.audio.Sound;
import pojahn.game.core.MobileEntity;
import pojahn.game.essentials.Image2D;

public class Particle extends MobileEntity {

    protected Sound introSound;
    private boolean soundPlayed;

    public Particle() {
    }

    public static Particle fromSound(Sound sound) {
        Particle particle = new Particle();
        particle.setIntroSound(sound);

        return particle;
    }

    public Particle getClone() {
        Particle clone = new Particle();
        copyData(clone);
        if (cloneEvent != null)
            cloneEvent.handleClonded(clone);

        return clone;
    }

    public void setIntroSound(Sound introSound) {
        this.introSound = introSound;
    }

    @Override
    public void logistics() {
        if (!soundPlayed && introSound != null) {
            soundPlayed = true;
            introSound.play(sounds.calc());
        }

        if (completed())
            getLevel().discard(this);
    }

    protected boolean completed() {
        return !isVisible() || getImage().hasEnded();
    }

    public static Particle from(int animationSpeed, Image2D[] image) {
        Particle particle = new Particle();
        particle.setImage(animationSpeed, image);
        return particle;
    }

    protected void copyData(Particle clone) {
        super.copyData(clone);
        clone.introSound = introSound;
    }
}