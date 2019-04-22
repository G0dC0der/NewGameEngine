package pojahn.game.desktop.redguyruns.levels.sprit;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import pojahn.game.core.BaseLogic;
import pojahn.game.core.Entity;
import pojahn.game.entities.movement.PathDrone;
import pojahn.game.essentials.Factory;
import pojahn.game.essentials.HUDMessage;

class Ghost extends PathDrone {

    private BitmapFont font;
    private Entity follower, reward;
    private int distanceCheck = 150;
    private boolean firstEncounter, used, stop;
    private Sound talkSound, successSound;
    public boolean reachedDest;

    public Ghost(final float x, final float y, final Entity follower, final Entity reward, final BitmapFont font, final Sound talkSound, final Sound successSound) {
        super(x, y);
        this.font = font;
        this.follower = follower;
        this.reward = reward;
        this.talkSound = talkSound;
        this.successSound = successSound;
        firstEncounter = true;
        setMoveSpeed(0);
    }

    @Override
    public void logistics() {
        super.logistics();

        if (!stop && BaseLogic.distance(this, follower) < distanceCheck) {
            if (firstEncounter) {
                firstEncounter = false;
                talkSound.play();
                getLevel().temp(Factory.drawText(HUDMessage.getMessage("Keep a close proximity and I shall give you a present.", x() - 120, y() - 20, Color.PURPLE), font), 200);
                getLevel().runOnceAfter(() -> {
                    distanceCheck = 430;
                    setMoveSpeed(1.8f);
                }, 200);
            } else {
                if (reachedDest) {
                    getLevel().temp(() -> tint.a -= .02f, () -> tint.a == 0.0f);
                    getLevel().add(reward.center(this));
                    activate(false);
                    successSound.play();
                }
            }
        } else if (!firstEncounter) {
            stop = true;
        }

        if (stop && !firstEncounter && !used && reachedDest && BaseLogic.distance(this, follower) < 150) {
            used = true;
            talkSound.play();
            getLevel().temp(Factory.drawText(HUDMessage.getMessage("You didn't stay close enough...", x() - 100, y() - 20, Color.PURPLE), font), 200);
        }
    }
}