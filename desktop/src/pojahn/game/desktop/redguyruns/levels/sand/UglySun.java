package pojahn.game.desktop.redguyruns.levels.sand;

import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.BaseLogic;
import pojahn.game.core.MobileEntity;
import pojahn.game.core.PlayableEntity;
import pojahn.game.desktop.redguyruns.util.ResourceUtil;
import pojahn.game.entities.enemy.weapon.AcceleratingBullet;
import pojahn.game.entities.enemy.LaserDrone;
import pojahn.game.entities.particle.Particle;
import pojahn.game.entities.enemy.weapon.Projectile;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.Factory;
import pojahn.game.essentials.Image2D;
import pojahn.game.essentials.ResourceManager;
import pojahn.game.essentials.Vitality;
import pojahn.lang.Int32;

public class UglySun extends MobileEntity {

    private ResourceManager res;
    private PlayableEntity target;
    private Animation<Image2D> angryImage, pissedImage;
    private int damageTaken;
    private LaserDrone laserDrone;
    private AcceleratingBullet bullet;

    public UglySun(final PlayableEntity target) {
        this.target = target;
    }

    @Override
    public void init() {
        laserDrone = new LaserDrone(x(), y(), 100, 5, 70, target);
        laserDrone.addEvent(Factory.follow(this, laserDrone, halfWidth(), halfHeight()));
        laserDrone.setChargeBeam(ResourceUtil.getChargingLaser(res));
        laserDrone.setFiringBeam(ResourceUtil.getFiringLaser(res));
        laserDrone.setStartupSound(res.getSound("lasercharge.wav"));
        laserDrone.setFiringSound(res.getSound("laserattack.wav"));

        final Particle trailer = Particle.shrinkingParticle(.05f);
        trailer.setImage(res.getAnimation("fireball"));
        trailer.scaleX = trailer.scaleY = .85f;

        final Particle soundParticle = Particle.fromSound(res.getSound("firesound.wav"));
        soundParticle.sounds.maxVolume = .65f;

        bullet = new AcceleratingBullet(target);
        bullet.setImage(res.getAnimation("fireball"));
        bullet.setTrailer(trailer);
        bullet.zIndex(50);
        bullet.setMoveSpeed(10);
        bullet.setTrailerDelay(3);
        bullet.setGunfire(soundParticle);

        target.setActionEvent(hitter -> {
            if (hitter == laserDrone || hitter.isCloneOf(bullet)) {
                target.touch(-1);
            }
        });
    }

    public void setHappyImage(final Animation<Image2D> happyImage) {
        setImage(happyImage);
    }

    public void setAngryImage(final Animation<Image2D> angryImage) {
        this.angryImage = angryImage;
    }

    public void setPissedImage(final Animation<Image2D> pissedImage) {
        this.pissedImage = pissedImage;
    }

    public void setRes(final ResourceManager res) {
        this.res = res;
    }

    public void hit() {
        getLevel().temp(Factory.spazz(this, 2, 0), 40, () -> offsetX = offsetY = 0);
        //TODO: Hit sound

        if (++damageTaken == 1) {
            super.setImage(angryImage);
            getLevel().add(laserDrone);
            setMoveSpeed(2.7f);
        } else if (damageTaken == 2) {
            super.setImage(pissedImage);
            setMoveSpeed(3.2f);

            final Int32 c = new Int32();
            addEvent(() -> {
                if (target.isAlive() && ++c.value % 75 == 0) {
                    final Projectile bullet = this.bullet.getClone();
                    bullet.setTarget(target);
                    bullet.center(this);
                    getLevel().add(bullet);
                }
            });
        } else if (damageTaken == 3) {
            getLevel().discard(this);

            final Particle deathImg = Particle.imageParticle(2, res.getAnimation("bossexp"));
            deathImg.setIntroSound(res.getSound("bossdie.wav"));
            getLevel().add(deathImg);
            getLevel().runOnceAfter(() -> target.setState(Vitality.COMPLETED), 120);
        }
    }

    @Override
    public void logistics() {
        final Vector2 dest = new Vector2(target.x(), getEngine().ty() - (getEngine().getScreenSize().height / 2) + 20);

        if (10 < BaseLogic.distance(x(), y(), dest.x, dest.y)) {
            moveTowards(dest.x, dest.y);
        }
    }
}
