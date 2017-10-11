package pojahn.game.desktop.redguyruns.util;

import com.badlogic.gdx.audio.Sound;
import org.lwjgl.openal.AL10;
import pojahn.game.core.Entity;
import pojahn.game.core.PlayableEntity;
import pojahn.game.entities.Particle;
import pojahn.game.entities.mains.Flipper;
import pojahn.game.entities.mains.FlyMan;
import pojahn.game.entities.mains.GravityMan;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.Controller;
import pojahn.game.essentials.EntityBuilder;
import pojahn.game.essentials.Factory;
import pojahn.game.essentials.Image2D;
import pojahn.game.essentials.LaserBeam;
import pojahn.game.essentials.ResourceManager;

public class ResourceUtil {

    public static FlyMan getFlyMan(final ResourceManager res) {
        final FlyMan fly = new FlyMan();
        setMainAttributes(res, fly);

        return fly;
    }

    public static Flipper getFlipper(final ResourceManager res) {
        final Flipper flipper = new Flipper();
        setMainAttributes(res, flipper);
        flipper.setJumpSound(res.getSound("jump.wav"));
        flipper.addEvent(() -> flipper.flipY = flipper.isFlipped());
        flipper.setLandingSound(res.getSound("land.wav"));
        flipper.addEvent(Factory.keepGravityManInBounds(flipper));

        return flipper;
    }

    public static GravityMan getGravityMan(final ResourceManager res) {
        final GravityMan man = new GravityMan();
        setMainAttributes(res, man);
        man.setJumpSound(res.getSound("jump.wav"));
        man.setLandingSound(res.getSound("land.wav"));
        man.addEvent(Factory.keepGravityManInBounds(man));

        return man;
    }

    public static Entity getHearth(final ResourceManager res, final PlayableEntity play) {
        final Entity hearth = new Entity();
        hearth.setImage(5, res.getAnimation("health"));
        hearth.addEvent(() -> {
            if (hearth.collidesWith(play)) {
                res.getSound("health.wav").play();
                play.touch(1);
                play.getLevel().discard(hearth);
            }
        });

        return hearth;
    }

    private static void setMainAttributes(final ResourceManager res, final PlayableEntity play) {
        play.setImage(4, res.getAnimation("main"));
        play.healthHud = new Animation<>(3, res.getImage("hearth.png"));
        play.deathImage = new EntityBuilder().image(4, res.getAnimation("maindeath")).build(Particle.class);
        play.setController(Controller.DEFAULT_CONTROLLER);
        play.addTileEvent(Factory.crushable(play));
        play.addTileEvent(Factory.completable(play));
        play.addTileEvent(Factory.hurtable(play, -1));
        play.setHurtSound(res.getSound("mainhit.wav"));
        play.setDieSound(res.getSound("maindie.wav"));
        play.addEvent(play::face);
        play.setFacings(2);
    }

    public static LaserBeam getFiringLaser(final ResourceManager res) {
        final Animation<Image2D> laserFront = new Animation<>(5, res.getAnimation("laser_front"));
        final Animation<Image2D> laserBeam = new Animation<>(4, res.getAnimation("laser"));
        final Animation<Image2D> laserEnd = new Animation<>(4, res.getAnimation("laser_end"));
        laserEnd.pingPong(true);

        return Factory.threeStageLaser(laserFront, laserBeam, laserEnd);
    }

    public static LaserBeam getChargingLaser(final ResourceManager res) {
        return Factory.threeStageLaser(null, new Animation<>(4, res.getAnimation("charge")), null);
    }
}
