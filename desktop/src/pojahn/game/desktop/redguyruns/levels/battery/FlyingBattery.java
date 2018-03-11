package pojahn.game.desktop.redguyruns.levels.battery;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.ImmutableList;
import pojahn.game.core.BaseLogic;
import pojahn.game.core.Entity;
import pojahn.game.core.MobileEntity;
import pojahn.game.core.PlayableEntity;
import pojahn.game.desktop.redguyruns.util.GFX;
import pojahn.game.desktop.redguyruns.util.ResourceUtil;
import pojahn.game.entities.enemy.weapon.Bullet;
import pojahn.game.entities.enemy.weapon.Projectile;
import pojahn.game.entities.enemy.weapon.SimpleWeapon;
import pojahn.game.entities.image.RepeatingParallaxImage;
import pojahn.game.entities.image.StaticImage;
import pojahn.game.entities.main.GravityMan;
import pojahn.game.entities.movement.EarthDrone;
import pojahn.game.entities.movement.PathDrone;
import pojahn.game.entities.object.Collectable;
import pojahn.game.entities.object.Wind;
import pojahn.game.entities.particle.Debris;
import pojahn.game.entities.particle.EntityExplosion;
import pojahn.game.entities.particle.Particle;
import pojahn.game.entities.platform.EarthSolidPlatform;
import pojahn.game.entities.platform.SolidPlatform;
import pojahn.game.entities.platform.TilePlatform;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.CameraEffects;
import pojahn.game.essentials.Direction;
import pojahn.game.essentials.EntityBuilder;
import pojahn.game.essentials.Factory;
import pojahn.game.essentials.Hitbox;
import pojahn.game.essentials.Image2D;
import pojahn.game.essentials.ResourceManager;
import pojahn.game.essentials.Utils;
import pojahn.game.essentials.geom.Dimension;
import pojahn.game.essentials.geom.Size;
import pojahn.game.essentials.stages.TileBasedLevel;
import pojahn.lang.Bool;
import pojahn.lang.Int32;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static pojahn.game.core.BaseLogic.rectanglesCollide;

public class FlyingBattery extends TileBasedLevel {

    private ResourceManager res;
    private PlayableEntity play;
    private TilePlatform block;
    private boolean teslaActive;
    private Bool energyActive, energy2Active;
    private final float maxVolume = .5f;
    private final int teslaFreq = 350, teslaDuration = 110;

    @Override
    public void init(final Serializable meta) throws Exception {
        res = new ResourceManager();
        res.loadContentFromDirectory(Gdx.files.internal("res/data"));
        res.loadContentFromDirectory(Gdx.files.internal("res/general"));
        res.loadContentFromDirectory(Gdx.files.internal("res/flyingb"));
        res.loadAnimation(Gdx.files.internal("res/climb/wind"));
        res.loadMusic(Gdx.files.internal("res/cave/collapsing_music.wav"));
        res.loadSound(Gdx.files.internal("res/cave/slam.wav"));
        getEngine().timeFont = res.getFont("sansserif32.fnt");

        parse(res.getTiledMap("map.tmx"));
        Stream.of(res.getAnimation("main")).forEach(Image2D::createPixelData);
        Stream.of(res.getAnimation("flypbottom")).forEach(Image2D::createPixelData);
        Stream.of(res.getAnimation("bolter")).forEach(Image2D::createPixelData);
        Stream.of(res.getAnimation("turbine")).forEach(Image2D::createPixelData);
        Stream.of(res.getAnimation("flyingpfan")).forEach(Image2D::createPixelData);
        res.getImage("lethalpart.png").createPixelData();
        res.getImage("spikes.png").createPixelData();
        res.getImage("obj1.png").createPixelData();
        res.getImage("obj2.png").createPixelData();
        res.getImage("obj3.png").createPixelData();
        res.getImage("obj4.png").createPixelData();
        res.getImage("bolteridle.png").createPixelData();
        res.getImage("energy_spike.png").createPixelData();

        Utils.playMusic(res.getMusic("music.ogg"), 1.318f, maxVolume);
        Utils.playMusic(res.getMusic("rain_music.wav"), 0, .1f);

        final Size mainSize = Size.from(res.getAnimation("main")[0]);
        getCheckpointHandler().setReachEvent(() -> GFX.renderCheckpoint(res, this));
        getCheckpointHandler().appendCheckpoint(center(75, 3, mainSize), getRectangle(75, 3, 1, 1));
        getCheckpointHandler().appendCheckpoint(center(55, 29, mainSize), getRectangle(56, 22, 1, 8));
    }

    @Override
    public void build() {
        teslaActive = false;
        energyActive = new Bool(false);
        energy2Active = new Bool(false);
        res.getMusic("rain_music.wav").setVolume(.1f);
        res.getMusic("music.ogg").setVolume(maxVolume);

        final Music fanLoop = res.getMusic("fan_loop_music.wav");
        if (!fanLoop.isPlaying()) {
            fanLoop.setLooping(true);
            fanLoop.setVolume(0);
            fanLoop.play();
        }

        final Music helicopterLoop = res.getMusic("helicopter_loop_music.wav");
        if (!helicopterLoop.isPlaying()) {
            helicopterLoop.setLooping(true);
            helicopterLoop.setVolume(0);
            helicopterLoop.play();
        }

        final Music collapseMusic = res.getMusic("collapsing_music.wav");
        collapseMusic.setLooping(true);
        if (collapseMusic.isPlaying()) {
            collapseMusic.stop();
        }

        /*
         * Main Character
         */
        play = ResourceUtil.getFlipper(res);
        play.zIndex(1);
        play.move(128, 27 * 128 + (128 - play.height() - 1));
        add(play);

        /*
         * Backgrounds & Foreground
         */
        final Entity foreground = getWorldImage();
        foreground.zIndex(100);
        add(foreground);
        final RepeatingParallaxImage sky = new EntityBuilder().image(res.getImage("sky.png")).zIndex(-100).build(RepeatingParallaxImage.class);
        sky.setRateX(.2f);
        sky.setRateY(.2f);
        add(sky);

        final StaticImage rain = new StaticImage();
        rain.setImage(3, res.getAnimation("rain"));
        rain.zIndex(-90);
        rain.rotate(90);
        add(rain);

        final RepeatingParallaxImage background = new EntityBuilder().image(res.getImage("background.png")).zIndex(-50).build(RepeatingParallaxImage.class);
        background.addEvent(()-> background.setVisible(play.y() > 834 && play.y() < 4050 + 80));
        add(background);

        add(new EntityBuilder().move(1536, 512).image(res.getImage("bg-stuff.png")).zIndex(-10).build());
        add(new EntityBuilder().move(9728, 512).image(res.getImage("bg-stuff2.png")).zIndex(-10).build());
        add(new EntityBuilder().move(7296, 3841).image(res.getImage("bg-stuff3.png")).zIndex(-10).build());

        /*
         * Lightning effects
         */
        final Int32 lightningCounter = new Int32();
        runWhile(this::addLightningEffect, ()-> ++lightningCounter.value % 100 == 0);

        /*
         * Energy Effect
         */
        addEnergyEffect(4, 20, energyActive);
        addEnergyEffect(5, 20, energyActive);
        addEnergyEffect(6, 20, energyActive);
        addEnergyEffect(7, 20, energyActive);
        addEnergyEffect(8, 20, energyActive);
        addEnergyEffect(9, 20, energyActive);
        addEnergyEffect(10, 20, energyActive);
        addEnergyEffect(11, 20, energyActive);

        /*
         * Energy Platforms
         */
        addEnergyPlatform(679, 3575);
        addEnergyPlatform(935, 3575);
        addEnergyPlatform(1191, 3575);
        addEnergySoundEmitter(512, 2567, 1024);

        final Entity spikes = new Entity();
        spikes.move(512, 3584);
        spikes.setImage(res.getImage("spikes.png"));
        spikes.setHitbox(Hitbox.PIXEL);
        spikes.addEvent(Factory.hitMain(spikes, play, -1));
        add(spikes);

        /*
         * Fat Platform
         */
        final SolidPlatform fatP = new SolidPlatform(1472, 2688, play);
        fatP.setImage(res.getImage("fatplatform.png"));
        add(fatP);
        add(fatP.getClone().move(fatP.x() - fatP.width(), fatP.y()));

        /*
         * Flying Platforms
         */
        final EarthSolidPlatform fp1 = addFlyingPlatform(3850 - 60, 2409 - 10);
        fp1.addPath();
        fp1.addPath(3580, fp1.y());


        /*
         * Booster
         */
        final PathDrone booster = new PathDrone(2218, 2675);
        booster.setMoveSpeed(10);
        booster.appendPath(booster.x() + 200, booster.y());
        booster.freeze();
        booster.setImage(res.getImage("booster.png"));
        runOnceWhen(()-> {
            booster.unfreeze();
            ((GravityMan)play).vel.x = -600;
            res.getSound("booster.wav").play();
        }, ()-> rectanglesCollide(play.bounds.toRectangle(), 2234, 2684, 16, 4));
        add(booster);

        /*
         * Music Adjustment
         */
        final Music music = res.getMusic("music.ogg");
        final Music rainMusic = res.getMusic("rain_music.wav");
        add(()-> {
            if (play.y() > 834 && play.y() < 4050 + 80) {
                music.setVolume(MathUtils.clamp(music.getVolume() + .005f, .1f, maxVolume));
                rainMusic.setVolume(MathUtils.clamp(rainMusic.getVolume() - .05f, .1f, 1f));
            } else {
                music.setVolume(MathUtils.clamp(music.getVolume() - .005f, .1f, maxVolume));
                rainMusic.setVolume(MathUtils.clamp(rainMusic.getVolume() + .05f, .1f, 1f));
            }
        });

        /*
         * Moving Ball-thing
         */
        addStageObject(3186, 2128, 1);
        addStageObject(2268, 2031, 1);
        addStageObject(2268, 1913, 4);
        addStageObject(2456, 1897, 2);
        addStageObject(2456, 1678, 3);
        addStageObject(3170, 1791, 3);
        addStageObject(2792, 1791, 4);
        addStageObject(2776, 2031, 2);

        addBlobs();
        interval(this::addBlobs, 1000);
        addTeslaSystem(new Vector2(2396 - 30 - 30, 1718 - 60 - 30), new Vector2(4, 2), new Vector2(350, 350));

        final Entity teslaSoundEmitter = new Entity();
        teslaSoundEmitter.addEvent(()-> teslaSoundEmitter.move(
            MathUtils.clamp(play.x(), 2061, 2061 + 1514),
            MathUtils.clamp(play.y(), 1538, 1538 + 788)));
        add(teslaSoundEmitter);
        add(teslaSoundEmitter.sounds.dynamicVolume(res.getMusic("spark_music.wav")));

        interval(()-> {
            teslaActive = true;
            if (rectanglesCollide(play.bounds.toRectangle(), 2056, 1538, 1519, 1519)) {
                res.getMusic("spark_music.wav").play();
            }
            runOnceAfter(()-> {
                teslaActive = false;
                res.getMusic("spark_music.wav").stop();
            }, teslaDuration);
        }, teslaFreq);

        /*
         * Wall Spikes
         */
        addWallSpikes(new Vector2(2176, 2528), 28, false);
        addWallSpikes(new Vector2(3552, 2044), 17, true);

        /*
         * Turbine Platform
         */
        final EarthSolidPlatform turbinePlatform = new EarthSolidPlatform(1536, 1664 - 100, play);
        turbinePlatform.setImage(res.getImage("turbineplatform.png"));
        turbinePlatform.addPath();
        turbinePlatform.addPath(turbinePlatform.x(), 714 + 150);
        turbinePlatform.getGravityAware().maxY = -500;
        turbinePlatform.getGravityAware().gravity = -400;
        add(turbinePlatform);

        final Entity platformFan = new Entity();
        platformFan.setHitbox(Hitbox.PIXEL);
        platformFan.addEvent(Factory.hitMain(platformFan, play, -1));
        platformFan.setImage(1, res.getAnimation("turbine"));
        platformFan.flipX = true;
        platformFan.sounds.maxDistance = 900;
        platformFan.sounds.power = 40;
        turbinePlatform.addEvent(Factory.follow(turbinePlatform, platformFan, 23, 32));
        add(platformFan.sounds.dynamicVolume(fanLoop));
        add(platformFan);

        final Wind wind = new Wind(0, 0, 40, 400, Direction.N, (GravityMan) play);
        wind.setImage(1, res.getAnimation("wind"));
        turbinePlatform.addEvent(Factory.follow(turbinePlatform, wind, 0, -wind.height()));
        wind.flipY = true;
        wind.bounds.size.width = 128;
        add(wind);

        /*
         * Flying Platform
         */
        final EarthSolidPlatform flyingPlatform = new EarthSolidPlatform(1844, 470 - 3, play);
        flyingPlatform.addPath(9448, flyingPlatform.y());
        flyingPlatform.setImage(res.getImage("flyingplatform.png"));
        flyingPlatform.freeze();
        flyingPlatform.getGravityAware().maxX = 800;
        flyingPlatform.getGravityAware().accX = 450;
        flyingPlatform.sounds.useFalloff = true;
        add(flyingPlatform.sounds.dynamicVolume(helicopterLoop));
        add(flyingPlatform);

        final Entity flyingPlatformFan = new Entity();
        flyingPlatformFan.setImage(1, res.getAnimation("flyingpfan"));
        flyingPlatformFan.setHitbox(Hitbox.PIXEL);
        flyingPlatformFan.addEvent(Factory.hitMain(flyingPlatformFan, play, -1));
        flyingPlatform.addEvent(Factory.follow(flyingPlatform, flyingPlatformFan));
        add(flyingPlatformFan);
        runOnceWhen(
            flyingPlatform::unfreeze,
            ()-> rectanglesCollide(play.bounds.toRectangle(), flyingPlatform.x(), flyingPlatform.y() - 2, flyingPlatform.width(), flyingPlatform.height()));

        /*
         * Trap Door
         */
        final SolidPlatform trapDoor = new SolidPlatform(8448, 1408, play);
        trapDoor.setImage(res.getAnimation("trapDoor"));
        trapDoor.appendPath(8312, trapDoor.y(), ()-> {
            trapDoor.getImage().stop(true);
            res.getSound("door_shut.wav").play();
        });
        trapDoor.freeze();
        trapDoor.setMoveSpeed(4);
        runOnceWhenMainCollides(()-> {
            trapDoor.unfreeze();
            res.getSound("door_shutting.wav").play();
        }, 65, 11, 1, 1);
        add(trapDoor);

        /*
         * Crusher Trap
         */
        final Entity vibrationEffect = CameraEffects.vibration(2);

        final SolidPlatform crusher = new SolidPlatform(9808, 1152, play);
        crusher.appendPath(8320, crusher.y(), 0, false, ()-> {
            discard(vibrationEffect);
            collapseMusic.stop();
            res.getSound("slam.wav").play();
            temp(CameraEffects.vibration(5), 45);
        });
        crusher.freeze();
        crusher.setFollowMode(SolidPlatform.FollowMode.NONE);
        crusher.setImage(res.getImage("crusher.png"));
        crusher.setMoveSpeed(2);
        add(crusher);
        runOnceWhenMainCollides(()-> {
            crusher.unfreeze();
            collapseMusic.play();
            add(vibrationEffect);
        }, 75, 9, 1, 2);

        /*
         * Blocks
         */
        block = new TilePlatform(0,0, play);
        block.setImage(res.getImage("block.png"));
        block.sounds.useFalloff = true;
        block.setCloneEvent(clonie -> clonie.addEvent(()-> {
            if (clonie.collidesWith(crusher)) {
                discard(clonie);
                clonie.sounds.play(res.getSound("block_collapse.wav"));

                final List<Animation<Image2D>> debrisImages = ImmutableList.of(
                    new Animation<>(3, res.getImage("part1.png")),
                    new Animation<>(3, res.getImage("part2.png")),
                    new Animation<>(3, res.getImage("part3.png")),
                    new Animation<>(3, res.getImage("part4.png")));

                final EntityExplosion blockExplode = new EntityExplosion(clonie, 2, 2, debrisImages);
                add(blockExplode);
            }
        }));

        addBlocks(9596, 1376, 2);
        addBlocks(9596 - 32, 1376, 3);
        addBlocks(9454, 1376, 1);
        addBlocks(9346, 1376, 2);
        addBlocks(9346 - 32, 1376, 4);
        addBlocks(9154, 1376, 6);
        addBlocks(9054, 1248, 4);
        addBlocks(8894, 1376, 4);
        addBlocks(8894 - 32, 1376, 5);
        addBlocks(8894 - 64, 1376, 4);
        addBlocks(8830 - 64, 1312, 6);

        addBlocks(9454, 1280, 6);

        addBlocks(8642, 1376, 1);
        addBlocks(8642 - 32, 1376, 2);
        addBlocks(8642 - 64, 1376, 3);
        addBlocks(8642 - 64 - 32, 1376, 4);
        addBlocks(8642 - 64 - 64, 1376, 5);
        addBlocks(8642 - 128 - 32, 1376, 6);
        addBlocks(8642 - 128 - 64, 1376, 7);

        /*
         * Giant Platform & Chain
         */
        final Entity cameraVibration = CameraEffects.vibration(1);

        final SolidPlatform giantPlatform = new SolidPlatform(7296, 3840, play);
        giantPlatform.setImage(res.getImage("giant_platform.png"));
        giantPlatform.setMoveSpeed(.55f);
        giantPlatform.freeze();
        giantPlatform.appendPath(giantPlatform.x(), 3091, ()-> {
            discard(cameraVibration);
            collapseMusic.stop();
        });
        add(giantPlatform);

        final Entity giantChain = new Entity();
        giantChain.setImage(res.getImage("giant_chain.png"));
        giantChain.zIndex(-3);
        giantPlatform.addEvent(Factory.follow(giantPlatform, giantChain, giantPlatform.halfWidth() - giantChain.halfWidth(), -giantChain.height()));
        add(giantChain);

        /*
         * Later Energy Place
         */
        interval(()-> {
            energy2Active.value =  true;
            runOnceAfter(()-> energy2Active.value = false, 120);
        }, 270);
        addEnergyEffect(64, 28, energy2Active);
        addEnergyEffect(65, 28, energy2Active);
        addEnergyEffect(66, 28, energy2Active);
        addEnergyEffect(67, 28, energy2Active);
        addEnergyEffect(68, 28, energy2Active);
        addEnergyEffect(69, 28, energy2Active);
        addEnergyEffect(70, 28, energy2Active);

        addEnergySpike(64, 28);
        addEnergySpike(65, 28);
        addEnergySpike(66, 28);
//        addEnergySpike(67, 28);
        addEnergySpike(68, 28);
        addEnergySpike(69, 28);
        addEnergySpike(70, 28);

        final Int32 counter = new Int32();
        final Entity energySound = new Entity();
        energySound.addEvent(()-> {
            energySound.move(
                MathUtils.clamp(play.x(), 64 * getTileWidth(), 71 * getTileWidth()),
                MathUtils.clamp(play.y(), 28 * getTileHeight(), 29 * getTileHeight()));

            if (energy2Active.value && ++counter.value % 20 == 0) {
                energySound.sounds.play(res.getSound("magnet.wav"));
            }
        });
        energySound.sounds.useFalloff = true;
        add(energySound);

        /*
         * Cannon
         */
        final Particle firingAnim = Particle.imageParticle(3, res.getAnimation("fireanim"));
        firingAnim.zIndex(Integer.MAX_VALUE);

        final Bullet bullet = new Bullet(play);
        bullet.setImage(res.getImage("blob.png"));
        bullet.rotate(false);
        bullet.setCloneEvent(clonie -> {
            clonie.addEvent(()-> clonie.rotate(-20.0f));

            final EntityExplosion blobDie = new EntityExplosion(clonie, 3, 3, Utils.fromArray(res.getAnimation("blobparts")));
            blobDie.setVx(200);
            blobDie.setVy(200);
            blobDie.setToleranceX(100);
            blobDie.setToleranceY(100);
            blobDie.bounds.size.height = 32;
            blobDie.sounds.useFalloff = true;
            blobDie.setIntroSound(res.getSound("bloblexp.wav"));
            blobDie.setCallback(debris -> debris.addEvent(()-> debris.rotate(40)));
            ((Projectile)clonie).setImpact(blobDie);
        });
        bullet.setMoveSpeed(13);
        bullet.setGunfire(firingAnim);


        final SimpleWeapon weapon = new SimpleWeapon(8416, 2144, bullet, Direction.W, 150);
        weapon.zIndex(Integer.MAX_VALUE - 1);
        weapon.spawnOffset(0, 5);
        weapon.setImage(res.getImage("weapon.png"));
//        weapon.setFiringAnimation(firingAnim);
        weapon.setFiringSound(res.getSound("pipefire.wav"));
        weapon.sounds.useFalloff = true;
        weapon.sounds.maxDistance = 1000;
        play.addObstacle(weapon);
        add(weapon);

        /*
         * Platform above spikes
         */
        final SolidPlatform p = new SolidPlatform(8352, 1791, play);
        p.setImage(res.getImage("platform.png"));
        p.freeze();
        p.setMoveSpeed(1);
        p.setFollowMode(SolidPlatform.FollowMode.STRICT);
        p.appendPath(8322,2186);
        p.appendPath(6791,2186, p::freeze);
        add(p);
        runOnceWhenMainCollides(p::unfreeze, 65, 13, 1, 1);

        /*
         * Lots of Spikes
         */
        final Entity manySpikes = new EntityBuilder().image(res.getImage("lots_of_spikes.png")).move(6784, 2272).build();
        manySpikes.addEvent(Factory.hitMain(manySpikes, play, -1));
        add(manySpikes);

        /*
         * Big Button
         */
        final Entity buttonBottom = new EntityBuilder().move(9158, 3832).image(res.getImage("big_button_bottom.png")).zIndex(10).build();
        play.addObstacle(buttonBottom);
        add(buttonBottom);

        final SolidPlatform bigButton = new SolidPlatform(9150, 3776, play);
        bigButton.setMoveSpeed(.2f);
        bigButton.setImage(res.getImage("big_button.png"));
        bigButton.addEvent(()-> {
            if (rectanglesCollide(play.bounds.toRectangle(), bigButton.x(), bigButton.y() - 2, bigButton.width(), 4)) {
                bigButton.unfreeze();
            } else {
                 bigButton.freeze();
            }
        });
        bigButton.appendPath(bigButton.x(), 3824, ()-> {
            play.freeze();

            runOnceAfter(()-> {
                final Entity cameraObject = new Entity().center(giantPlatform);
                cameraObject.bounds.pos.y = giantPlatform.y();
                add(cameraObject);
                addFocusObject(cameraObject);

                runOnceAfter(()-> {
                    removeFocusObject(cameraObject);
                    play.unfreeze();
                }, 300);
            }, 60);
            runOnceAfter(()-> {
                giantPlatform.unfreeze();
                add(cameraVibration);

                collapseMusic.play();
            }, 180);
        });
        add(bigButton);

        /*
         * Final Platforms
         */
        final EarthSolidPlatform finalP1 = addFlyingPlatform(6756, 4992);
        finalP1.addPath();
        finalP1.addPath(7076, finalP1.y());

        final EarthSolidPlatform finalP2 = addFlyingPlatform(5565 + 40, 4749);
        finalP2.setReachStrategy(EarthDrone.ReachStrategy.STRICT);
        finalP2.addPath();
        finalP2.addPath(6187 - 40, 4749);
        finalP2.addReversed();

        final EarthSolidPlatform finalP3 = addFlyingPlatform(4746 + 80, 4736);
        finalP3.addPath();
        finalP3.addPath(5146 - 80, finalP3.y());

        addFlyingPlatform(6266, 4936);
        addFlyingPlatform(6356, 4836);
        addFlyingPlatform(6406, 4756);

        /*
         * Gem
         */
        final Collectable gem = new Collectable(4528, 4703, play);
        gem.setCollectSound(res.getSound("collect1.wav"));
        gem.setCollectEvent(collector -> play.win());
        gem.setImage(res.getImage("gem.png"));
        add(gem);

        /*
         * Hit Event
         */
        play.setActionEvent(caller -> {
            if (caller.isCloneOf(bullet)) {
                play.touch(-1);
            }
        });
    }

    private void addEnergySpike(final int tileX, final int tileY) {
        final float x = tileX * getTileWidth();
        final float y = tileY * getTileHeight() + 48;

        for (int i = 0; i < 4; i++) {
            final boolean isFirst = i == 0;
            final EarthDrone spikeBall = new EarthDrone(x + (i * 32), y);
            spikeBall.setHitbox(Hitbox.PIXEL);
            spikeBall.addPath();
            spikeBall.setReachStrategy(EarthDrone.ReachStrategy.STRICT);
            spikeBall.setImage(res.getImage("energy_spike.png"));
            spikeBall.addEvent(Factory.hitMain(spikeBall, play, -1));
            spikeBall.getGravityAware().setSolid(true);
            spikeBall.sounds.useFalloff = true;
            spikeBall.addEvent(()-> {
                if (energy2Active.value) {
                    spikeBall.unfreeze();
                } else {
                    spikeBall.freeze();
                    final boolean falling = spikeBall.getGravityAware().velocity.y != 0;
                    spikeBall.getGravityAware().drag();
                    if (isFirst && falling && spikeBall.getGravityAware().velocity.y == 0) {
                        spikeBall.sounds.play(res.getSound("energypland.wav"));
                    }
                }
            });
            add(spikeBall);
        }
    }

    @Override
    public void dispose() {
        res.disposeAll();
    }

    @Override
    public String getLevelName() {
        return "Flying Battery";
    }

    @Override
    public Music getStageMusic() {
        return res.getMusic("music.ogg");
    }

    private void addBlocks(final float x, final float y, final int amount) {
        for (int i = 0; i < amount; i++) {
            add(block.getClone().move(x, y - (i * 32)));
        }
    }

    private void addWallSpikes(final Vector2 start, final int amount, final boolean flipX) {
        for (float y = start.y; y > start.y - (32 * amount); y -= 32) {
            final Entity spikes = new Entity();
            spikes.move(start.x, y);
            spikes.zIndex(10);
            spikes.setImage(10, res.getAnimation("spikes"));
            spikes.flipX = flipX;
            spikes.addEvent(Factory.hitMain(spikes, play, -1));
            add(spikes);
        }
    }

    private void addTeslaSystem(final Vector2 start, final Vector2 count, final Vector2 distance) {
        for (int x = 0; x < count.x; x++) {
            for (int y = 0; y < count.y; y++) {
                final Animation<Image2D> active = new Animation<>(3, res.getAnimation("bolter"));
                final Animation<Image2D> idle = new Animation<>(3, res.getImage("bolteridle.png"));
                final Int32 soundCounter = new Int32();

                final Entity teslaSphere = new Entity();
                teslaSphere.move(
                    start.x + (x*distance.x),
                    start.y + (y*distance.y));
                teslaSphere.setImage(idle);
                teslaSphere.zIndex(2);
                teslaSphere.setHitbox(Hitbox.PIXEL);
                teslaSphere.addEvent(Factory.hitMain(teslaSphere, play, -1));
                teslaSphere.sounds.useFalloff = true;
                teslaSphere.sounds.maxDistance = 500;
                teslaSphere.addEvent(()-> {
                    if (teslaActive) {
                        teslaSphere.setImage(active);
                    } else {
                        teslaSphere.setImage(idle);
                    }
                });
                add(teslaSphere);
                
                if (x < count.x - 1) {
                    for (int x2 = 0; x2 < (distance.x / 16) - 3; x2++) {
                        final Animation<Image2D> wireActive = new Animation<>(5, res.getAnimation("wire-hori"));
                        final Animation<Image2D> wireIdle = new Animation<>(3, res.getImage("wire-hori-idle.png"));

                        final Entity wire = new Entity();
                        wire.move(
                            teslaSphere.x() + (x2 * 16) + 58,
                            teslaSphere.y() + 27);
                        wire.setImage(wireIdle);
                        wire.zIndex(-1);
                        wire.addEvent(()-> {
                            if (teslaActive) {
                                wire.setImage(wireActive);
                                if (wire.collidesWith(play)) {
                                    play.touch(-1);
                                }
                            } else {
                                wire.setImage(wireIdle);
                            }
                        });
                        add(wire);
                    }
                }
                if (y < count.y - 1) {
                    for (int y2 = 0; y2 < (distance.y / 16) - 3; y2++) {
                        final Animation<Image2D> wireActive = new Animation<>(5, res.getAnimation("wire-vert"));
                        final Animation<Image2D> wireIdle = new Animation<>(3, res.getImage("wire-vert-idle.png"));

                        final Entity wire = new Entity();
                        wire.move(
                            teslaSphere.x() + 27,
                            teslaSphere.y() + (y2 * 16) + 58);
                        wire.setImage(wireIdle);
                        wire.zIndex(-1);
                        wire.addEvent(()-> {
                            if (teslaActive) {
                                wire.setImage(wireActive);
                                if (wire.collidesWith(play)) {
                                    play.touch(-1);
                                }
                            } else {
                                wire.setImage(wireIdle);
                            }
                        });
                        add(wire);
                    }
                }
            }
        }
    }

    private void addBlobs() {
        final List<PathDrone> blobs = IntStream.range(0, 4)
            .mapToObj(index -> {
                final int startX = 3584 + (24 * index) + index + 5;
                final int startY = 2148;

                final PathDrone blob = new PathDrone(startX, startY);
                blob.setImage(res.getImage("blob.png"));
                blob.setMoveSpeed(1);
                blob.appendPath(blob.x(), blob.y(), 0, true, null);
                blob.appendPath(3189, 2148);
                blob.appendPath(3189, 1795);
                blob.appendPath(2795, 1795);
                blob.appendPath(2795, 2051);
                blob.appendPath(2272, 2051);
                blob.appendPath(2272, 1916);
                blob.appendPath(2477, 1916);
                blob.appendPath(2477, 1683);
                blob.appendPath(2147, 1683, () -> discard(blob));

                add(blob);

                return blob;
            })
            .collect(Collectors.toList());

        final GroupedPlatform groupedPlatform = new GroupedPlatform(play, blobs);
        groupedPlatform.zIndex(1);
        add(groupedPlatform);
    }

    private void addStageObject(final float x, final float y, final int partIndex) {
        final MobileEntity part = new MobileEntity();
        play.addObstacle(part);
        part.setImage(res.getImage("obj" + partIndex + ".png"));
        part.addEvent(Factory.solidify(part));
        part.move(x, y);
        part.setHitbox(Hitbox.PIXEL);
        part.zIndex(2);
        add(part);
    }

    private EarthSolidPlatform addFlyingPlatform(final float x, final float y) {
        final Entity propeller = new Entity();
        propeller.setImage(1, res.getAnimation("flypbottom"));
        propeller.setHitbox(Hitbox.PIXEL);
        propeller.addEvent(Factory.hitMain(propeller, play, -1));

        final EarthSolidPlatform platform = new EarthSolidPlatform(x, y, play);
        platform.setImage(res.getImage("flyptop.png"));
        platform.addEvent(Factory.follow(platform, propeller));
        platform.getGravityAware().maxX = 180;
        platform.getGravityAware().accX = 200;

        add(platform);
        add(propeller);

        return platform;
    }

    private void addLightningEffect() {
        final Particle lightning = Particle.imageParticle(1, res.getAnimation("lightning"));

        final Dimension screenSize = getEngine().getScreenSize();
        float startX = getEngine().tx() - (screenSize.width / 2);
        float startY = getEngine().ty() - (screenSize.height / 2);

        lightning.zIndex(-95);
        lightning.bounds.pos.x = MathUtils.random(startX - 130, startX + screenSize.width - 470);
        lightning.bounds.pos.y = MathUtils.random(startY - 130, startY + screenSize.height - 470);

        add(lightning);
    }

    private void addEnergyEffect(final float tileX, final float tileY, final Bool bool) {
        final float x = tileX * getTileWidth();
        final float y = tileY * getTileHeight();

        final Entity bolt = new Entity();
        bolt.setImage(4, res.getAnimation("bgelec"));
        bolt.move(x + 16, y + 8);
        bolt.zIndex(1000);
        bolt.addEvent(()-> bolt.setVisible(bool.value));

        final Entity lamp = new Entity();
        lamp.setImage(3, res.getAnimation("lamp"));
        lamp.zIndex(10000);
        lamp.move(x + 28, y + 40);
        lamp.addEvent(()-> lamp.setVisible(bool.value));
        lamp.setCloneEvent(clonie -> clonie.addEvent(()-> clonie.setVisible(bool.value)));

        add(bolt);
        add(lamp);
        add(lamp.getClone().move(x + 60, y + 40));
        add(lamp.getClone().move(x + 92, y + 40));
    }

    private void addEnergyPlatform(final float x, final float y) {
        final int chillFrames = 300;

        final EarthSolidPlatform platform = new EarthSolidPlatform(x, y, play);
        platform.setImage(res.getImage("solidpart.png"));
        platform.getGravityAware().gravity = -500;
        platform.getGravityAware().maxY = -600;
        platform.setReachTolerance(4);
        platform.sounds.useFalloff = true;
        platform.addPath(x, y, chillFrames, false, ()-> {
            platform.deaccelerate();
            platform.sounds.play(res.getSound("energypland.wav"));
            runOnceAfter(()-> energyActive.value = true, chillFrames);
        });
        platform.addPath(x, y - 842, chillFrames, false, ()-> {
            platform.deaccelerate();
            platform.sounds.play(res.getSound("preach.wav"));
            runOnceAfter(()-> energyActive.value = false, chillFrames);
        });
        platform.setReachStrategy(EarthDrone.ReachStrategy.STRICT);

        final Entity middleSpikes = new Entity();
        middleSpikes.setHitbox(Hitbox.PIXEL);
        middleSpikes.setImage(res.getImage("lethalpart.png"));
        middleSpikes.addEvent(Factory.hitMain(middleSpikes, play, -1));
        platform.addEvent(Factory.follow(platform, middleSpikes, 1, 10));

        final Entity chain = new Entity();
        chain.setImage(res.getImage("chain.png"));
        platform.addEvent(Factory.follow(platform, chain, 21, 33));

        final Entity bottom = new Entity();
        bottom.setImage(res.getImage("bottompart.png"));
        bottom.move(x + 9, y + 33);
        bottom.zIndex(10);

        add(platform);
        add(middleSpikes);
        add(chain);
        add(bottom);
    }

    private void addEnergySoundEmitter(final float x, final float y, final float width) {
        final Entity emitter = new Entity();
        emitter.move(x, y);
        emitter.addEvent(()-> emitter.bounds.pos.x = MathUtils.clamp(play.x(), x, x + width));
        emitter.sounds.useFalloff = true;
        emitter.sounds.maxDistance = 800;
        emitter.sounds.power = 40;
        final Int32 counter = new Int32();
        emitter.addEvent(()-> {
            if (energyActive.value && ++counter.value % 20 == 0) {
                emitter.sounds.play(res.getSound("magnet.wav"));
            }
        });
        add(emitter);
    }
}