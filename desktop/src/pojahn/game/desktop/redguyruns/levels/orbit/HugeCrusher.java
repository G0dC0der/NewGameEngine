package pojahn.game.desktop.redguyruns.levels.orbit;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import pojahn.game.core.MobileEntity;
import pojahn.game.entities.SolidPlatform;
import pojahn.game.essentials.CameraEffects;

class HugeCrusher extends SolidPlatform {

    private float vx, deacceleration, goal;
    private boolean reached;
    private Sound slamSound;
    private Music collapseSound;
    private int c;

    HugeCrusher(float x, float y, MobileEntity... subjects) {
        super(x, y, subjects);
        deacceleration = 3500;
        goal = 1312;
    }

    @Override
    public void logistics() {
        if (isFrozen())
            return;

        if (!reached) {
            if (++c % 5 == 0)
                getLevel().temp(CameraEffects.vibration(2), 5);

            moveTowards(goal, y());
            if (goal > x()){
                collapseSound.stop();
                slamSound.play();
                reached = true;
                vx = -600;
                getLevel().temp(CameraEffects.vibration(5), 60);
            }
        } else if (vx != 0) {
            float nextX = bounds.pos.x - vx * getEngine().delta;
            move(nextX, y());

            if (vx > 0) {
                vx -= deacceleration * getEngine().delta;
                if (vx < 0)
                    vx = 0;
            } else if (vx < 0) {
                vx += deacceleration * getEngine().delta;
                if (vx > 0)
                    vx = 0;
            }
        }

        super.logistics();
    }

    public void setSlamSound(Sound slamSound) {
        this.slamSound = slamSound;
    }

    public void setCollapseSound(Music collapseSound) {
        this.collapseSound = collapseSound;
    }
}
