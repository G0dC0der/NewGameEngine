package pojahn.game.entities;

import com.badlogic.gdx.audio.Sound;
import pojahn.game.entities.mains.GravityMan;

public class VacuumDrone extends PathDrone {

    private float detectRadius, vacuumPower;
    private int vacuumShots, vacuumDelay, reload, reloadCounter, burstCounter, burstDelayCounter;
    private Particle vacuumParticle;
    private Sound vacuumSound;
    private GravityMan[] targets;

    public VacuumDrone(float x, float y, GravityMan... targets) {
        super(x, y);
        this.targets = targets;
        detectRadius = 300;
        vacuumPower = 500;
        vacuumShots = 3;
        vacuumDelay = 20;
        reload = 120;
        burstDelayCounter = -1;
    }

    public void setDetectRadius(float detectRadius) {
        this.detectRadius = detectRadius;
    }

    public void setVacuumPower(float vacuumPower) {
        this.vacuumPower = vacuumPower;
    }

    public void setVacuumShots(int vacuumShots) {
        this.vacuumShots = vacuumShots;
    }

    public void setVacuumDelay(int vacuumDelay) {
        this.vacuumDelay = vacuumDelay;
    }

    public void setReload(int reload) {
        this.reload = reload;
    }

    public void setVacuumParticle(Particle vacuumParticle) {
        this.vacuumParticle = vacuumParticle;
    }

    public void setVacuumSound(Sound vacuumSound) {
        this.vacuumSound = vacuumSound;
    }

    @Override
    public void logistics() {
        super.logistics();

        if (--reloadCounter < 0) {
            for (GravityMan target : targets) {
                if (near(target, detectRadius)) {
                    if (burstCounter >= vacuumShots) {
                        reloadTime();
                        break;
                    } else if (++burstDelayCounter % vacuumDelay == 0) {
                        vacuum(target);
                        burstCounter++;

                        if (vacuumParticle != null)
                            getLevel().add(vacuumParticle.getClone().center(this));
                        if (vacuumSound != null)
                            vacuumSound.play(sounds.calc());
                    }
                }
            }
        }
    }

    private void reloadTime() {
        reloadCounter = reload;
        burstCounter = 0;
    }

    private void vacuum(GravityMan man) {
        if (centerX() > man.centerX())
            man.vel.x = -vacuumPower;
        else
            man.vel.x = vacuumPower;

        if (centerY() > man.centerY())
            man.vel.y = -vacuumPower;
        else
            man.vel.y = vacuumPower;
    }
}
