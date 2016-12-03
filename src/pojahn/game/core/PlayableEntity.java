package pojahn.game.core;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import pojahn.game.entities.Particle;
import pojahn.game.essentials.*;

import java.util.LinkedList;
import java.util.List;

public abstract class PlayableEntity extends MobileEntity {

    public Animation<Image2D> healthHud;
    public Particle deathImage;

    private Sound hurtSound, dieSound;
    private Keystrokes keysDown;
    private Vitality state;
    private Controller controller;
    private List<Keystrokes> replayData;
    private int replayDataCounter, hp, hurtCounter, counter;
    private boolean isGhost;

    public PlayableEntity() {
        hp = 1;
        state = Vitality.ALIVE;
        replayData = new LinkedList<>();
    }

    public int getHP() {
        return hp;
    }

    public void touch(int strength) {
        if (strength >= 0) {
            hp += strength;
            hurtCounter = 0;
        } else if (hurtCounter < 0) {
            hp += strength;
            hurtCounter = 100;
            if (hurtSound != null && hp > 0)
                hurtSound.play(sounds.calc());
        }

        if (0 >= hp && !isDead())
            setState(Vitality.DEAD);
    }

    public boolean isHurt() {
        return hurtCounter > 0;
    }

    public Controller getController() {
        return controller;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void setHurtSound(Sound hurtSound) {
        this.hurtSound = hurtSound;
    }

    public void setDieSound(Sound dieSound) {
        this.dieSound = dieSound;
    }

    public boolean isGhost() {
        return isGhost;
    }

    public void setGhostData(List<Keystrokes> replayData) {
        this.replayData = replayData;
        isGhost = true;
    }

    public void win() {
        setState(Vitality.COMPLETED);
    }

    public void lose() {
        if (getState() != Vitality.DEAD)
            setState(Vitality.DEAD);
    }

    public void setState(Vitality state) {
        if (state == Vitality.ALIVE && this.state == Vitality.COMPLETED)
            throw new IllegalArgumentException("Can not set to state to " + Vitality.ALIVE + " when the current state is " + Vitality.COMPLETED);
        if (state == Vitality.DEAD && this.state == Vitality.DEAD)
            throw new IllegalArgumentException("Can not kill an already dead character.");

        if (this.state != state) {
            if (state == Vitality.ALIVE && this.state == Vitality.DEAD)
                revive();
            else if (state == Vitality.DEAD)
                deathAction();

            this.state = state;
        }
    }

    public Vitality getState() {
        return state;
    }

    public boolean isAlive() {
        return getState() == Vitality.ALIVE;
    }

    public boolean isDead() {
        return getState() == Vitality.DEAD;
    }

    public boolean isDone() {
        return getState() == Vitality.COMPLETED;
    }

    public Keystrokes getKeysDown() {
        return keysDown;
    }

    @Override
    public void render(SpriteBatch batch) {
        if (--hurtCounter > 0 && ++counter % 5 == 0)
            return;

        super.render(batch);
    }

    void setKeysDown(Keystrokes keysDown) {
        this.keysDown = keysDown;
    }

    private void deathAction() {
        activate(false);
        setVisible(false);

        if (deathImage != null)
            getLevel().add(deathImage.getClone().center(this));
        if (dieSound != null)
            dieSound.play(sounds.calc());
    }

    private void revive() {
        activate(true);
        setVisible(true);
    }

    Keystrokes nextInput() {
        if (isGhost) {
            return replayDataCounter > replayData.size() - 1 ? Keystrokes.AFK : replayData.get(replayDataCounter++);
        } else {
            throw new IllegalStateException("This method can only be called if the entity is a ghost or replaying.");
        }
    }
}
