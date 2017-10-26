package pojahn.game.desktop.redguyruns.levels.orbit;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.BaseLogic;
import pojahn.game.core.Entity;
import pojahn.game.core.MobileEntity;
import pojahn.game.desktop.redguyruns.util.GFX;
import pojahn.game.desktop.redguyruns.util.ResourceUtil;
import pojahn.game.entities.BackgroundImage;
import pojahn.game.entities.BigImage;
import pojahn.game.entities.Bullet;
import pojahn.game.entities.Collectable;
import pojahn.game.entities.EvilDog;
import pojahn.game.entities.Missile;
import pojahn.game.entities.Particle;
import pojahn.game.entities.PathDrone;
import pojahn.game.entities.RotatingCannon;
import pojahn.game.entities.Shuttle;
import pojahn.game.entities.SolidPlatform;
import pojahn.game.entities.TargetLaser;
import pojahn.game.entities.TransformablePlatform;
import pojahn.game.entities.Weapon;
import pojahn.game.entities.mains.GravityMan;
import pojahn.game.essentials.Direction;
import pojahn.game.essentials.EntityBuilder;
import pojahn.game.essentials.Factory;
import pojahn.game.essentials.Hitbox;
import pojahn.game.essentials.Image2D;
import pojahn.game.essentials.ResourceManager;
import pojahn.game.essentials.Utils;
import pojahn.game.essentials.Vibrator;
import pojahn.game.essentials.geom.Size;
import pojahn.game.essentials.stages.TileBasedLevel;
import pojahn.lang.Bool;
import pojahn.lang.Int32;
import pojahn.lang.PingPongFloat;

import java.io.Serializable;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static pojahn.game.core.BaseLogic.rectanglesCollide;

public class OrbitalStation extends TileBasedLevel {

    private ResourceManager res;
    private GravityMan man;
    private Particle exp;
    private HugeCrusher hugeCrusher;
    private Music music, spaceMusic, lavaMusic;

    @Override
    public void init(final Serializable meta) throws Exception {
        res = new ResourceManager();
        res.loadContentFromDirectory(Gdx.files.internal("res/data"));
        res.loadContentFromDirectory(Gdx.files.internal("res/general"));
        res.loadContentFromDirectory(Gdx.files.internal("res/orbit"));
        res.loadAnimation(Gdx.files.internal("res/clubber/trailer"));
        res.loadSound(Gdx.files.internal("res/cave/slam.wav"));
        res.loadMusic(Gdx.files.internal("res/cave/collapsing_music.wav"));

        Stream.of(res.getAnimation("main")).forEach(Image2D::createPixelData);
        Stream.of(res.getAnimation("mine")).forEach(Image2D::createPixelData);
        Stream.of(res.getAnimation("fireball")).forEach(Image2D::createPixelData);
        Stream.of(res.getAnimation("guard")).forEach(Image2D::createPixelData);
        res.getImage("dangerzone.png").createPixelData();

        parse(res.getTiledMap("map.tmx"));
        getEngine().timeFont = res.getFont("sansserif32.fnt");

        music = res.getMusic("music.ogg");
        lavaMusic = res.getMusic("music_mines.ogg");
        spaceMusic = res.getMusic("music_space.ogg");
        res.getMusic("collapsing_music.wav").setLooping(true);

        Utils.playMusic(music, 1.63f, .7f);
        Utils.playMusic(lavaMusic, 14.95f, .7f);
        Utils.playMusic(spaceMusic, 4.48f, .7f);

        final Size mainSize = Size.from(res.getAnimation("main")[0]);
        getCheckpointHandler().appendCheckpoint(center(40, 47, mainSize), getRectangle(34, 46, 2, 2));
        getCheckpointHandler().appendCheckpoint(center(66, 76, mainSize), getRectangle(66, 76, 1, 1));
        getCheckpointHandler().setReachEvent(() -> GFX.renderCheckpoint(res, this));
    }

    @Override
    public void build() {
        final boolean reached1 = getCheckpointHandler().reached(0);

        /*
         * Main Character
         */
        man = ResourceUtil.getGravityMan(res);
        man.face(Direction.W);
        man.move(87 * getTileWidth(), 100 * getTileHeight());
        add(man);
        runOnceAfter(() -> man.bounds.pos.x--, 1);

        /*
         * Music
         */
        res.getMusic("collapsing_music.wav").stop();
        music.setVolume(.7f);
        lavaMusic.setVolume(0);
        lavaMusic.play();
        spaceMusic.setVolume(0);
        spaceMusic.play();

        final Rectangle room1 = new Rectangle(0, 3821, 3040, 339);
        final Rectangle room2 = new Rectangle(0, 0, 3040, 600);
        final float fadeSpeed = .01f;
        final float maxVolume = .7f;

        add(() -> {
            if (rectanglesCollide(room1, man.bounds.toRectangle())) {
                lavaMusic.setVolume(Math.min(lavaMusic.getVolume() + fadeSpeed, maxVolume));
                music.setVolume(Math.max(0, music.getVolume() - fadeSpeed));
            } else if (rectanglesCollide(room2, man.bounds.toRectangle())) {
                spaceMusic.setVolume(Math.min(spaceMusic.getVolume() + fadeSpeed, maxVolume));
                music.setVolume(Math.max(0, music.getVolume() - fadeSpeed));
            } else {
                music.setVolume(Math.min(music.getVolume() + fadeSpeed, maxVolume));
                spaceMusic.setVolume(Math.max(0, spaceMusic.getVolume() - fadeSpeed));
                lavaMusic.setVolume(Math.max(0, lavaMusic.getVolume() - fadeSpeed));
            }
        });

        /*
         * Background and foreground
         */
        final Entity worldImage = getWorldImage();
        worldImage.zIndex(1000);
        add(worldImage);
        add(new EntityBuilder().image(res.getImage("background.png")).zIndex(-100).build(BigImage.class, BigImage.RenderStrategy.PARALLAX_REPEAT));
        add(new EntityBuilder().image(res.getImage("separator.png")).zIndex(-99).move(680, 480).build());
        add(new EntityBuilder().image(res.getImage("separator.png")).zIndex(-99).move(2729, 480).build());
        final Entity space = BackgroundImage.parallaxImage(.2f, 0, false);
        space.zIndex(-50);
        space.setImage(res.getImage("space.png"));
        add(space);

        /*
         * Heat Overlay
         */
        final PingPongFloat overlayAlpha = new PingPongFloat(0, 0, .005f);
        final BigImage overlay = new BigImage(BigImage.RenderStrategy.FIXED);
        overlay.zIndex(5000);
        overlay.setImage(res.getImage("alphaImage.png"));
        overlay.addEvent(() -> overlay.tint.a = overlayAlpha.get());
        add(overlay);
        add(() -> {
            if (man.y() > 112 * getTileHeight()) {
                final float diff = man.y() - (112 * getTileHeight());
                overlayAlpha.setMax(Math.min(diff / 600, .7f));
                overlayAlpha.setMin(Math.max(diff / 900, 0));
            }
        });
        final Int32 heathExposureCounter = new Int32();
        final Bool suited = new Bool(false);
        runOnceWhen(man::lose, () -> !suited.value && man.y() > 121 * getTileHeight() && ++heathExposureCounter.value > 330);

        /*
         * Turrets
         */
        final Particle bulletExp = new Particle();
        bulletExp.setImage(3, res.getAnimation("smallexp"));
        bulletExp.setIntroSound(res.getSound("gmexplode.wav"));
        bulletExp.sounds.maxVolume = .5f;
        bulletExp.sounds.useFalloff = true;
        bulletExp.sounds.maxDistance = 580;
        bulletExp.sounds.power = 10;

        final Bullet bullet = new Bullet(man);
        bullet.setImage(res.getImage("bullet.png"));
        bullet.setImpact(bulletExp);
        bullet.setMoveSpeed(5);

        addTurret(800, 2048, bullet);
        addTurret(1141, 2048, bullet);

        /*
         * Lava
         */
        for (int i = 0; i < 190; i++) {
            add(new EntityBuilder().move(16 * i, 4112).image(5, res.getAnimation("lava")).zIndex(80).build());
        }
        final Entity otherlava = new EntityBuilder().move(0, 4128).image(res.getImage("otherlava.png")).zIndex(80).build();
        otherlava.bounds.size.width = 3040;
        otherlava.zIndex(75);
        add(otherlava);
        runOnceWhen(man::lose, () -> rectanglesCollide(man.bounds.toRectangle(), 0, 4128, 3040, 100));

        /*
         * Lava Platform
         */
        final SolidPlatform platform = new SolidPlatform(2644, 4095/* - 6*/, man);
        platform.setImage(res.getImage("platform.png"));
        platform.setMoveSpeed(1.2f);
        platform.appendPath(0, platform.y());
        platform.freeze();
        platform.zIndex(70);
        add(platform);
        runOnceWhen(platform::unfreeze, () -> rectanglesCollide(man.bounds.toRectangle(), platform.x(), platform.y() - 5, platform.width(), 5));

        /*
         * Lava Mines
         */
        final PathDrone mine1 = getMine(1984, 3872);
        mine1.appendPath(2104, 3872);
        mine1.appendPath(2104, 4056);
        mine1.appendPath(1984, 4056);

        final PathDrone mine2 = getMine(2104, 4056);
        mine2.appendPath(1984, 4056);
        mine2.appendPath(1984, 3872);
        mine2.appendPath(2104, 3872);

        final PathDrone mine3 = getMine(1196, 3992);
        mine3.appendPath(mine3.x(), 3872);
        mine3.setMoveSpeed(1.65f);

        final PathDrone mine4 = getMine(960, 3992);
        mine4.appendPath(1048, mine4.y());

        final PathDrone mine5 = getMine(1048, 3912 - 17);
        mine5.appendPath(960, mine5.y());

        final PathDrone mine6 = getMine(768, 3961);
        mine6.appendPath(mine6.x(), 3872);
        mine6.setMoveSpeed(.4f);

        add(mine1);
        add(mine2);
        add(mine3);
        add(mine4);
        add(mine5);
//        add(mine6);

        /*
         * Fireballs
         */
        add(getFireball(1502, 4128 + 250, 650));
        addAfter(getFireball(1702, 4128 + 250, 650), 30);

        add(getFireball(252, 4128 + 250, 650));
        addAfter(getFireball(252 + 32 + 8, 4128 + 250, 650), 7);
        addAfter(getFireball(252 + 32 + 32 + 16, 4128 + 250, 650), 14);

        /*
         * Gem
         */

        final Collectable gem = new Collectable(0, 4058 - 30, man);
        gem.setImage(3, res.getAnimation("gem"));
        gem.setCollectSound(res.getSound("collect1.wav"));
        gem.setCollectEvent(collector -> man.win());
        add(gem);

        /*
         * Space Gravity
         */
        add(() -> {
            if (480 >= man.y()) {
                man.gravity = -250;
            } else {
                man.gravity = -500;
            }
        });

        /*
         * Space Traps
         */
        final SolidPlatform trap1 = new SolidPlatform(704, 723, man);
        trap1.setImage(res.getImage("elevator.png"));
        trap1.setMoveSpeed(5);
        trap1.appendPath(trap1.x(), 480, 0, false, () -> {
            res.getSound("elevator.wav").stop();
            res.getSound("shut.wav").play();
        });
        trap1.freeze();
        runOnceWhen(() -> {
            trap1.unfreeze();
            res.getSound("elevator.wav").loop(.6f);
        }, () -> rectanglesCollide(man.bounds.toRectangle(), 704, 573, 64, 64));
        add(trap1);

        final SolidPlatform trap2 = new SolidPlatform(2656, 480, man);
        trap2.setMoveSpeed(5);
        trap2.setImage(res.getImage("spaceGate.png"));
        trap2.appendPath(2752, trap2.y());
        trap2.freeze();
        add(trap2);

        /*
         * Hearths
         */
        add(ResourceUtil.getHearth(res, man).move(12, 457));

        /*
         * Space Guards
         */
        final EvilDog guard1 = getGuard(556, 278);
        final EvilDog guard2 = getGuard(876, 278);

        add(guard1);
        add(guard2);

        runOnceWhen(() -> {
            guard1.unfreeze();
            guard2.unfreeze();
        }, () -> rectanglesCollide(man.bounds.toRectangle(), 504, 0, 5, 530) || rectanglesCollide(man.bounds.toRectangle(), 944, 0, 5, 530));

        runOnceWhen(() -> {
            guard1.freeze();
            guard2.freeze();
            trap2.unfreeze();
        }, () -> rectanglesCollide(man.bounds.toRectangle(), 2752, 512, 96, 100));

        /*
         * Crushers
         */
        add(getCrusher(1856, 1024, 2208, 1024, Direction.E));
        add(getCrusher(2848, 1280, 2464, 1280, Direction.W));
        add(getCrusher(2784, 2048, 2784, 1696, Direction.N));
        add(getCrusher(2560, 1890, 2560, 1952, Direction.S));
        add(getCrusher(2496, 1890, 2496, 1952, Direction.S));
        add(getCrusher(2496 - 64, 1890, 2496 - 64, 1952, Direction.S));
        add(getCrusher(2496 - (64 * 3), 1890, 2496 - (64 * 3), 1952, Direction.S));
        add(getCrusher(2496 - (64 * 4), 1890, 2496 - (64 * 4), 1952, Direction.S));
        add(getCrusher(2496 - (64 * 5), 1890, 2496 - (64 * 5), 1952, Direction.S));
        final SolidPlatform crusher = getCrusher(2496 - (64 * 2), 1890, 2496 - (64 * 2), 1952, Direction.S);
        crusher.clearData();
        crusher.setMoveSpeed(.8f);
        crusher.appendPath(crusher.x(), crusher.y() + 30, 120, false, null);
        crusher.appendPath(crusher.x(), crusher.y(), 150, false, null);
        add(crusher);

        /*
         * Elevator
         */
        final SolidPlatform elevator;
        if (reached1) {
            elevator = new SolidPlatform(1472, 1536, man);
        } else {
            elevator = new SolidPlatform(1472, 2336, man);
            elevator.setMoveSpeed(5);
            elevator.appendPath(elevator.x(), 1536, 0, false, () -> {
                res.getSound("elevator.wav").stop();
                res.getSound("shut.wav").play();
            });
            elevator.freeze();
            runOnceWhen(() -> {
                res.getSound("elevator.wav").loop(.6f);
                elevator.unfreeze();
            }, () -> rectanglesCollide(man.bounds.toRectangle(), 1472, 2157, 64, 112));
        }
        elevator.setImage(res.getImage("elevator.png"));
        add(elevator);

        /*
         * Battle Elevator
         */
        final SolidPlatform trap3;
        if (reached1) {
            trap3 = new SolidPlatform(544, 2880 - 32, man);
        } else {
            trap3 = new SolidPlatform(544, 2880, man);
            trap3.appendPath(trap3.x(), trap3.y() - trap3.height());
            trap3.setMoveSpeed(1);
            trap3.freeze();
        }
        trap3.setImage(res.getImage("trap2.png"));
        add(trap3);

        final SolidPlatform trap4 = new TransformablePlatform(39 * getTileWidth(), 96 * getTileHeight(), man);
        trap4.setImage(res.getImage("trapblock.png"));
        trap4.appendPath(trap4.x(), trap4.y() - trap4.height());
        trap4.freeze();
        add(trap4);

        final TransformablePlatform battleElevator;
        if (reached1) {
            battleElevator = new TransformablePlatform(192, 2336, man);
        } else {
            battleElevator = new TransformablePlatform(192, 2880, man);
            battleElevator.appendPath(battleElevator.x(), 2336);
            battleElevator.setMoveSpeed(.6f);
            battleElevator.freeze();
            runOnceWhen(() -> {
                battleElevator.unfreeze();
                trap3.unfreeze();
            }, () -> rectanglesCollide(man.bounds.toRectangle(), 152, 2682, 352, 198) || rectanglesCollide(man.bounds.toRectangle(), 233, 2616, 352, 196));
        }
        battleElevator.setImage(res.getImage("battle-elevator.png"));
        add(battleElevator);

        /*
         * Turrets in elevator room
         */
        final Particle missileExp = Particle.from(3, res.getAnimation("missileexp"));
        missileExp.setIntroSound(res.getSound("missileexp.wav"));

        final Missile missile = new Missile(0, 0, man);
        missile.setImage(res.getImage("missile.png"));
        missile.fastVeryFloaty();
        missile.setImpact(missileExp);
        missile.setTrailer(Particle.from(2, res.getAnimation("trailer")));

        final Particle gunfire = new Particle();
        gunfire.scaleX = gunfire.scaleY = .5f;
        gunfire.zIndex(10);
        gunfire.setImage(5, res.getAnimation("gunfire"));
        gunfire.setIntroSound(res.getSound("missile_launch.wav"));
//        gunfire.sounds.maxVolume = .7f;

        final Supplier<Weapon> weaponSupplier = () -> {
            final Weapon weapon = new Weapon(0, 0, 1, 100, 200, man);
            weapon.setImage(res.getImage("turret.png"));
            weapon.setProjectile(missile);
            weapon.setAlwaysRotate(false);
            weapon.setFrontFire(true);
            weapon.setRotationSpeed(.05f);
            weapon.setRotateWhileRecover(false);
            weapon.setFiringParticle(gunfire);
            runOnceWhen(() -> weapon.activate(false), () -> rectanglesCollide(battleElevator.bounds.toRectangle(), getRectangle(6, 75, 11, 1)));

            return weapon;
        };


        //add(weaponSupplier.get().move(437, 2181 + 60));
        add(weaponSupplier.get().move(227, 2181 + 60));

        /*
         * Zapper
         */
        addZapper(9, 86);
        addZapper(13, 86);

        addZapper(7, 82);
        addZapper(11, 82);
        addZapper(15, 82);

        addZapper(8, 79);
        addZapper(9, 79);
        addZapper(13, 79);
        addZapper(14, 79);

        addZapper(7, 77);
        addZapper(15, 77);

        addZapper(7, 75);
        addZapper(8, 75);
//        addZapper(9, 75);
        addZapper(10, 75);
        addZapper(12, 75);
//        addZapper(13, 75);
        addZapper(14, 75);
        addZapper(15, 75);

        /*
         * Small Guard
         */
        addSmallGuard(529);
        addSmallGuard(192);

        /*
         * Danger Zone
         */
        hugeCrusher = new HugeCrusher(2281 + 60, 2784, man);
        hugeCrusher.setImage(res.getImage("hugecrusher.png"));
        hugeCrusher.setMoveSpeed(9);
        hugeCrusher.freeze();
        hugeCrusher.zIndex(100);
        hugeCrusher.setFollowMode(SolidPlatform.FollowMode.STRICT);
        hugeCrusher.setSlamSound(res.getSound("slam.wav"));
        hugeCrusher.setCollapseSound(res.getMusic("collapsing_music.wav"));
        add(hugeCrusher);

        /*
         * Heat Item
         */
        final Collectable heatItem = new Collectable(2085, 2468, man);
        heatItem.setImage(res.getImage("heatitem.png"));
        heatItem.setCollectSound(res.getSound("collect1.wav"));
        heatItem.setCollectEvent(collector -> suited.value = true);
        add(heatItem);

        /*
         * Lasers
         */
        exp = new Particle();
        exp.setIntroSound(res.getSound("gmexplode.wav"));
        exp.setImage(3, res.getAnimation("exp"));
        exp.zIndex(1000);

        final Shuttle target1 = new Shuttle(2064, 3500);
        target1.appendPath(2064 + 100, 3500);
        target1.appendPath(2064 - 100, 3500);
        target1.thrust = 100;

        final TargetLaser laser1 = getTargetLaser(2043, 2918, target1);
        laser1.move(2043, 2918);
        add(laser1);
        add(target1);

        final Entity target2 = new EntityBuilder().move(1669 + laser1.halfWidth(), 2913 + 100).build();
        final Int32 laser2Counter = new Int32();

        final TargetLaser laser2 = getTargetLaser(1669, 2913, target2);
        laser2.move(1669, 2913);
        laser2.addEvent(() -> {
            if (++laser2Counter.value % 40 == 0) {
                laser2.stop(!laser2.stopped());
            }
        });
        add(laser2);
        add(target2);

        final Particle magicTrailer = Particle.from(2, res.getAnimation("magicbullet"));
        magicTrailer.scaleX = magicTrailer.scaleY = .5f;

        final Bullet magicBullet = new Bullet(man);
        magicBullet.setTrailer(magicTrailer);
        magicBullet.bounds.size.set(10, 10);
        magicBullet.setTrailerDelay(3);
        magicBullet.setGunfire(Particle.fromSound(res.getSound("magicfire.wav")));

        final Weapon weapon = new Weapon(1322, 2793, 2, 15, 60, man);
        weapon.setProjectile(magicBullet);
        weapon.setImage(res.getImage("laserbeam.png"));
        weapon.setRotateWhileRecover(true);
        weapon.setFrontFire(true);
        weapon.setAlwaysRotate(true);
        weapon.setRotationSpeed(.05f);
        weapon.setFrontFire(true);
        weapon.ifCollides(hugeCrusher).thenRunOnce(() -> {
            discard(weapon);
            add(exp.getClone().center(weapon));
        });
        add(weapon);

        /*
         * Action Event
         */
        man.setActionEvent(hitter -> {
            if (hitter.isCloneOf(missile) || hitter.isCloneOf(bullet)) {
                man.touch(-1);
            } else if (hitter == laser1 || hitter == laser2 || hitter.isCloneOf(magicBullet)) {
                trap4.unfreeze();
                hugeCrusher.unfreeze();
                res.getMusic("collapsing_music.wav").play();
            }
        });
    }

    private TargetLaser getTargetLaser(final float x, final float y, final Entity target) {
        final TargetLaser laser = new TargetLaser(x, y, target, man);
        laser.setImage(res.getImage("laserbeam.png"));
        laser.infiniteBeam(true);
        laser.faceTarget(true);
        laser.frontFire(true);
        laser.setLaserTint(Color.GREEN);
        laser.setLaserBeam(ResourceUtil.getFiringLaser(res));
        laser.addEvent(() -> {
            if (laser.collidesWith(hugeCrusher)) {
                discard(laser);
                add(exp.getClone().center(laser));
            }
        });

        return laser;
    }

    private void addSmallGuard(final float x) {
        final MobileEntity smallGuard = new MobileEntity();
        smallGuard.move(x, 2337);
        smallGuard.setMoveSpeed(10);
        smallGuard.setImage(res.getAnimation("smallguard"));
        smallGuard.addEvent(() -> {
            smallGuard.bounds.pos.y = man.y();
            if (smallGuard.y() < 2337)
                smallGuard.bounds.pos.y = 2337;
            else if (smallGuard.y() > 2832) {
                smallGuard.bounds.pos.y = 2832;
            }
        });
        smallGuard.addEvent(Factory.hitMain(smallGuard, man, -1));
        add(smallGuard);
    }

    private void addZapper(final float tileX, final float tileY) {
        final int freq = MathUtils.random(80, 130);
        final Int32 c = new Int32();

        final Particle particle = new Particle();
        particle.setIntroSound(res.getSound("zap.wav"));
        particle.setImage(res.getAnimation("thunder"));
        particle.sounds.useFalloff = true;
        particle.sounds.maxDistance = 500;
        particle.sounds.maxVolume = .6f;

        final Entity zapper = new Entity();
        zapper.move(tileX * getTileWidth() + 8, tileY * getTileHeight() + 8);
        zapper.setImage(4, res.getAnimation("zapper"));
        zapper.addEvent(Factory.hitMain(zapper, man, -1));
        zapper.addEvent(() -> {
            if (++c.value % freq == 0) {
                add(particle.getClone().center(zapper));
            }
        });

        add(zapper);
    }

    private SolidPlatform getCrusher(final float x1, final float y1, final float x2, final float y2, final Direction direction) {
        final SolidPlatform crusher = new SolidPlatform(x1, y1, man);

        Vibrator.VibDirection vibDir = null;
        if (direction.isEast()) {
            vibDir = Vibrator.VibDirection.RIGHT;
            crusher.setImage(res.getImage("crusher-left.png"));
        } else if (direction.isWest()) {
            vibDir = Vibrator.VibDirection.LEFT;
            crusher.setImage(res.getImage("crusher-right.png"));
        } else if (direction.isNorth()) {
            vibDir = Vibrator.VibDirection.TOP;
            crusher.setImage(res.getImage("crusher-up.png"));
        } else if (direction.isSouth()) {
            vibDir = Vibrator.VibDirection.BOTTOM;
            crusher.setImage(res.getImage("crusher-down.png"));
        }

        final Vibrator vibrator = new Vibrator(this, crusher, vibDir, man);
        vibrator.setStrength(10);
        vibrator.setRadius(500);
        vibrator.setVibrateSound(res.getSound("slam2.wav"));

        final int freezeTime = 40;
        final float attackSpeed = 7;
        final float fallbackSpeed = 2;

        crusher.setMoveSpeed(attackSpeed);
        crusher.sounds.useFalloff = true;
        crusher.appendPath(x1, y1, freezeTime, false, () -> crusher.setMoveSpeed(attackSpeed));
        crusher.appendPath(x2, y2, freezeTime, false, () -> {
            crusher.setMoveSpeed(fallbackSpeed);
            vibrator.vibrate();
        });

        return crusher;
    }

    private EvilDog getGuard(final float x, final float y) {
        final int talkDelay = MathUtils.random(120, 300);
        final Int32 counter = new Int32();

        final EvilDog guard = new EvilDog(x, y, -1, man);
        guard.setImage(4, res.getAnimation("guard"));
        guard.freeze();
        guard.setHitbox(Hitbox.PIXEL);
        guard.addEvent(Factory.hitMain(guard, man, -1));
        guard.zIndex(2000);
        runWhile(() -> res.getSound("guarding.wav").play(), () -> !guard.isFrozen() && ++counter.value % talkDelay == 0);

        return guard;
    }

    private Fireball getFireball(final float x, final float y, final float flyPower) {
        final Fireball fireball = new Fireball(x, y, flyPower);
        fireball.setImage(5, res.getAnimation("fireball"));
        fireball.setHitbox(Hitbox.PIXEL);
        fireball.addEvent(Factory.hitMain(fireball, man, -1));
        fireball.zIndex(71);

        return fireball;
    }

    private PathDrone getMine(final float x, final float y) {
        final PathDrone mine = new PathDrone(x, y);
        mine.setImage(3, res.getAnimation("mine"));
        mine.setMoveSpeed(1.2f);
        mine.appendPath();
        mine.setHitbox(Hitbox.PIXEL);
        mine.addEvent(() -> {
            if (man.isAlive() && mine.collidesWith(man)) {
                man.lose();
                discard(mine);

                final Particle exp = new Particle();
                exp.setIntroSound(res.getSound("gmexplode.wav"));
                exp.setImage(3, res.getAnimation("exp"));
                add(exp.center(mine));
            }
        });

        return mine;
    }

    private void addTurret(final float x, final float y, final Bullet bullet) {
        final Entity turret = new EntityBuilder().move(x, y).image(res.getImage("cannonturret.png")).zIndex(20).build();

        final Particle firingAnim = new Particle();
        firingAnim.zIndex(30);
        firingAnim.setImage(3, res.getAnimation("fireanim"));
        firingAnim.setIntroSound(res.getSound("gmfire.wav"));
        firingAnim.sounds.maxVolume = .5f;
        firingAnim.sounds.useFalloff = true;
        firingAnim.sounds.maxDistance = 580;
        firingAnim.sounds.power = 10;

        final RotatingCannon rcannon = new RotatingCannon(x - 7, y + 9, bullet);
        rcannon.setImage(res.getImage("gunmachine.png"));
        rcannon.zIndex(10);
        rcannon.setFireAnimation(firingAnim);

        add(turret);
        add(rcannon);
    }

    @Override
    public void dispose() {
        res.disposeAll();
    }

    @Override
    public Music getStageMusic() {
        return music;
    }

    @Override
    public String getLevelName() {
        return "Orbital Station";
    }
}
