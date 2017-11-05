package pojahn.game.entities.enemy;

import com.badlogic.gdx.audio.Sound;
import pojahn.game.entities.movement.PathDrone;
import pojahn.game.entities.main.GravityMan;
import pojahn.game.entities.particle.Particle;
import pojahn.lang.Obj;

import java.util.List;

public class VacuumDrone extends PathDrone {

    private float detectRadius, vacuumPower;
    private int vacuumShots, vacuumDelay, reload, reloadCounter, burstCounter, burstDelayCounter;
    private Particle vacuumParticle;
    private Sound vacuumSound;
    private List<GravityMan> targets;

    public VacuumDrone(final float x, final float y, final GravityMan... targets) {
        super(x, y);
        this.targets = Obj.requireNotEmpty(targets);
        detectRadius = 300;
        vacuumPower = 500;
        vacuumShots = 3;
        vacuumDelay = 20;
        reload = 120;
        burstDelayCounter = -1;
    }

    public void setDetectRadius(final float detectRadius) {
        this.detectRadius = detectRadius;
    }

    public void setVacuumPower(final float vacuumPower) {
        this.vacuumPower = vacuumPower;
    }

    public void setVacuumShots(final int vacuumShots) {
        this.vacuumShots = vacuumShots;
    }

    public void setVacuumDelay(final int vacuumDelay) {
        this.vacuumDelay = vacuumDelay;
    }

    public void setReload(final int reload) {
        this.reload = reload;
    }

    public void setVacuumParticle(final Particle vacuumParticle) {
        this.vacuumParticle = vacuumParticle;
    }

    public void setVacuumSound(final Sound vacuumSound) {
        this.vacuumSound = vacuumSound;
    }

    @Override
    public void logistics() {
        super.logistics();

        if (--reloadCounter < 0) {
            for (final GravityMan target : targets) {
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
                            sounds.play(vacuumSound);
                    }
                }
            }
        }
    }

    private void reloadTime() {
        reloadCounter = reload;
        burstCounter = 0;
    }

    private void vacuum(final GravityMan man) {
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
