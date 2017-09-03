package pojahn.game.desktop.redguyruns.levels.deathegg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.Collisions;
import pojahn.game.core.Entity;
import pojahn.game.desktop.redguyruns.util.GFX;
import pojahn.game.desktop.redguyruns.util.ResourceUtil;
import pojahn.game.entities.BigImage;
import pojahn.game.entities.Bullet;
import pojahn.game.entities.Button;
import pojahn.game.entities.Flash;
import pojahn.game.entities.OneWay;
import pojahn.game.entities.Particle;
import pojahn.game.entities.PathDrone;
import pojahn.game.entities.SimpleWeapon;
import pojahn.game.entities.SolidPlatform;
import pojahn.game.entities.mains.Flipper;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.Direction;
import pojahn.game.essentials.EntityBuilder;
import pojahn.game.essentials.Factory;
import pojahn.game.essentials.Hitbox;
import pojahn.game.essentials.Image2D;
import pojahn.game.essentials.ResourceManager;
import pojahn.game.essentials.Utils;
import pojahn.game.essentials.stages.TileBasedLevel;
import pojahn.game.events.Event;
import pojahn.lang.Bool;
import pojahn.lang.Int32;

import java.io.Serializable;
import java.util.stream.Stream;

public class DeathEgg extends TileBasedLevel {

    private ResourceManager res;
    private Flipper play;
    private Music bandMove, sawWork, energy, music;
    private boolean energized, fieldOffline;

    @Override
    public void init(Serializable meta) throws Exception {
        res = new ResourceManager();
        res.loadContentFromDirectory(Gdx.files.internal("res/deathegg"));
        res.loadContentFromDirectory(Gdx.files.internal("res/data"));
        res.loadContentFromDirectory(Gdx.files.internal("res/general"));
        getEngine().timeFont = res.getFont("sansserif32.fnt");

        parse(res.getTiledMap("map.tmx"));
        res.addAsset("flash", Utils.toImage(Color.WHITE));
        Stream.of(res.getAnimation("main")).forEach(Image2D::createPixelData);
        Stream.of(res.getAnimation("gear")).forEach(Image2D::createPixelData);
        Stream.of(res.getAnimation("forcefield")).forEach(Image2D::createPixelData);
        Stream.of(res.getAnimation("mine")).forEach(Image2D::createPixelData);
        res.getImage("singleDown.png").createPixelData();
        res.getImage("doubleDown.png").createPixelData();
        res.getImage("leftSpikeSection.png").createPixelData();
        res.getImage("rightSpikeSection.png").createPixelData();
        res.getImage("leftSpikes2.png").createPixelData();
        res.getImage("rightSpikes2.png").createPixelData();
        res.getImage("roofSpikes.png").createPixelData();

        bandMove = res.getMusic("bandmove_music.wav");
        sawWork = res.getMusic("sawwork_music.wav");
        energy = res.getMusic("energy_music.wav");
        music = res.getMusic("music.ogg");
        Utils.playMusic(music, .45f, .4f);

        getCheckpointHandler().setReachEvent(()-> GFX.renderCheckpoint(res, this));
        getCheckpointHandler().appendCheckpoint(new Vector2(2959,4332 - 1), new Rectangle(2967, 4174, 221, 246));
        getCheckpointHandler().appendCheckpoint(new Vector2(3453,2412 - 1), new Rectangle(3382, 1521, 415, 985));
        getCheckpointHandler().appendCheckpoint(new Vector2(479,748 - 1), new Rectangle(64, 773, 415, 131));
    }

    @Override
    public void build() {
        bandMove.stop();
        sawWork.setVolume(0);
        sawWork.stop();
        energized = fieldOffline = false;

        /*
         * Main Character
         */
        play = ResourceUtil.getFlipper(res);
        play.move(20, 32 * 128 + (128 - play.height() - 1));
        add(play);
//        play.move(19 * 128, 5 * 128 + (128 - play.height() - 1));

        /*
         * Backgrounds & Foreground
         */
        Entity foreground = getWorldImage();
        foreground.zIndex(100);
        add(foreground);
        add(new EntityBuilder().image(res.getImage("background.png")).zIndex(-100).build(BigImage.class, BigImage.RenderStrategy.PARALLAX_REPEAT));

        /*
         * Flip Buttons
         */
        addSwitch(623, 3888, false, null);
        addSwitch(1903, 4016, false, null);
        addSwitch(3055, 3681, true, null);
        addSwitch(3055, 3456, true, null);
        addSwitch(150, 256, true, null);
        addSwitch(2574, 752, false, null);

        /*
         * Spikes
         */
        spikes(424, 2880,  res.getImage("singleDown.png"));
        spikes(1014, 2816, res.getImage("singleDown.png"));
        spikes(1280, 2880, res.getImage("doubleDown.png"));
        spikes(730, 2816, res.getImage("doubleDown.png"));

        Entity leftSpikeSection = new EntityBuilder().image(res.getImage("leftSpikeSection.png")).hitbox(Hitbox.PIXEL).move(2208, 3200).zIndex(9).build();
        Entity rightSpikeSection = new EntityBuilder().image(res.getImage("rightSpikeSection.png")).hitbox(Hitbox.PIXEL).move(1600, 3200).zIndex(9).build();
        Entity leftSpikes2 = new EntityBuilder().image(res.getImage("leftSpikes2.png")).hitbox(Hitbox.PIXEL).move(4384 - 128, 2944).zIndex(9).build();
        Entity rightSpikes2 = new EntityBuilder().image(res.getImage("rightSpikes2.png")).hitbox(Hitbox.PIXEL).move(3904, 2432).zIndex(9).build();
        Entity roofSpikes = new EntityBuilder().image(res.getImage("roofSpikes.png")).hitbox(Hitbox.PIXEL).move(3647, 3456).zIndex(9).build();
        leftSpikeSection.addEvent(Factory.hitMain(leftSpikeSection, play, -1));
        rightSpikeSection.addEvent(Factory.hitMain(rightSpikeSection, play, -1));
        leftSpikes2.addEvent(Factory.hitMain(leftSpikes2, play, -1));
        rightSpikes2.addEvent(Factory.hitMain(rightSpikes2, play, -1));
        roofSpikes.addEvent(Factory.hitMain(roofSpikes, play, -1));
        add(leftSpikeSection);
        add(rightSpikeSection);
        add(leftSpikes2);
        add(rightSpikes2);
        add(roofSpikes);

        /*
         * Moving Platform
         */
        Animation<Image2D> solpImage = new Animation<>(4, res.getAnimation("band"));
        solpImage.stop(true);

        SolidPlatform solp = new SolidPlatform(2056, 3098, play);
        solp.setImage(solpImage);
        solp.freeze();
        solp.setMoveSpeed(1.8f);
        solp.sounds.maxVolume = .15f;
        solp.appendPath(1676, 3478);
        solp.appendPath(2056, 3850);
        solp.appendPath(1676, 3980, 0, false, ()->{
            solp.freeze();
            bandMove.stop();
            solpImage.stop(true);
        });
        add(solp);
        runOnceWhen(()-> {
            solp.unfreeze();
            bandMove.setLooping(true);
            bandMove.setVolume(1f);
            bandMove.play();
            solpImage.stop(false);
            add(solp.sounds.dynamicVolume(bandMove));
        }, ()-> Collisions.rectanglesCollide(play.x(), play.y(), play.width(), play.height(), solp.x(), solp.y() + solp.height(), solp.width(), 2));


        /*
         * Blocker
         */
        SolidPlatform blocker = new SolidPlatform(3008, 3648, play);
        blocker.setImage(res.getImage("blocker.png"));
        add(blocker);

        /*
         * Simple Weapons
         */
        Bullet bullet = new Bullet(0,0, play);
        bullet.setImage(res.getImage("proj.png"));
        bullet.setMoveSpeed(2.3f);
        play.setActionEvent(caller -> {
            if (caller.isCloneOf(bullet)) {
                play.touch(-1);
            }
        });

        SimpleWeapon wp1 = new SimpleWeapon(1600, 3533, bullet, Direction.E, 150);
        wp1.setImage(res.getImage("simple.png"));
        wp1.spawnOffset(0, 12);
        wp1.setFiringSound(res.getSound("gunfire.wav"));
        wp1.sounds.useFalloff = true;
        wp1.sounds.power = 50;
        wp1.zIndex(10);

        SimpleWeapon wp2 = new SimpleWeapon(2231, 3385, bullet, Direction.W, 150);
        wp2.setImage(res.getImage("simple.png"));
        wp2.flipX = true;
        wp2.spawnOffset(0, 12);
        wp2.setFiringSound(res.getSound("gunfire.wav"));
        wp2.sounds.useFalloff = true;
        wp2.sounds.power = 50;
        wp2.zIndex(10);

        add(wp1);
        add(wp2);
        add(wp1.getClone().move(1600, 3936));
        add(wp2.getClone().move(2231, 3788));

        /*
         * Gears
         */
        PathDrone gear = new PathDrone(0,0);
        gear.setImage(4, res.getAnimation("gear"));
        gear.setHitbox(Hitbox.PIXEL);
        gear.setMoveSpeed(.6f);
        gear.setCloneEvent(clonie -> clonie.addEvent(Factory.hitMain(clonie, play, -1)));

        PathDrone gear1 = gear.getClone();
        gear1.move(3008, 4096);
        gear1.appendPath();
        gear1.appendPath(3008, 4208);
        gear1.appendPath(3120, 4208);
        gear1.appendPath(3120, 4096);

        PathDrone gear2 = gear.getClone();
        gear2.move(3120, 4208);
        gear2.appendPath();
        gear2.appendPath(3120, 4096);
        gear2.appendPath(3008, 4096);
        gear2.appendPath(3008, 4208);

//        add(gear1);
//        add(gear2);

        PathDrone gear3 = gear.getClone();
        gear3.move(3120, 4032 + 128);
        gear3.appendPath();
        gear3.appendPath(3056, 4032 + 128);
        gear3.appendPath(3056, 4016 + 128);
        gear3.appendPath(3008, 4016 + 128);
        gear3.appendReversed();

        PathDrone gear4 = gear.getClone();
        gear4.move(3008, 4016 + 128);
        gear4.appendPath();
        gear4.appendPath(3072, 4016 + 128);
        gear4.appendPath(3072, 4032 + 128);
        gear4.appendPath(3120, 4032 + 128);
        gear4.appendReversed();

        add(gear3);
        add(gear4);

        PathDrone gear5 = gear.getClone();
        gear5.move(3008, 3840);
        gear5.appendPath();
        gear5.appendPath(3008, 3952);

        PathDrone gear6 = gear.getClone();
        gear6.move(3120, 3952);
        gear6.appendPath();
        gear6.appendPath(3120, 3840);

        add(gear5);
        add(gear6);

        float x1 = 3392;
        float x2 = 3504;
        float startY = 3648;
        float height = res.getAnimation("gear")[0].getHeight();

        for (int i = 0; i < 37; i++) {
            PathDrone aGear = gear.getClone();

            if (i % 2 == 0) {
                aGear.move(x1, startY + (height * i));
                aGear.appendPath();
                aGear.appendPath(x2, aGear.y());
            } else {
                aGear.move(x2, startY + (height * i));
                aGear.appendPath();
                aGear.appendPath(x1, aGear.y());
                aGear.setImage(aGear.getImage().getReversed(Image2D.class));
            }

            add(aGear);
        }

        /*
         * Platform Section
         */
        SolidPlatform pl1 = new SolidPlatform(3919, 3182, play);
        pl1.setImage(res.getImage("plat.png"));
        pl1.zIndex(-1);
        pl1.setMoveSpeed(1);
        pl1.setFollowMode(SolidPlatform.FollowMode.STRICT);
        pl1.appendPath(pl1.x(), pl1.y(), 0, true, null);
        pl1.appendPath(4164, 3434, 0, false, null);

        add(pl1);
        addAfter(pl1.getClone(), 120);
        addAfter(pl1.getClone(), 240);

        SolidPlatform pl2 = pl1.getClone();
        pl2.move(4072, 3077);
        pl2.clearData();
        pl2.appendPath(4072, 3077, 60, false, null);
        pl2.appendPath(4072 + 90, 2917 + 20, 60, false, null);
        add(pl2);

        x1 = 3895;
        x2 = 3950;
        startY = 2456;
        int waitFrames = 60;
        int padding = 80;
        for (int i = 0; i < 5; i++) {
            SolidPlatform pl = pl1.getClone();
            pl.setMoveSpeed(.7f);
            pl.clearData();

            if (i % 2 == 0) {
                pl.move(x1, startY + (i * padding));
                pl.appendPath(pl.x(), pl.y(), waitFrames, false, null);
                pl.appendPath(x2, pl.y(), waitFrames, false, null);
            } else {
                pl.move(x2, startY + (i * padding));
                pl.appendPath(pl.x(), pl.y(), waitFrames, false, null);
                pl.appendPath(x1, pl.y(), waitFrames, false, null);
            }

            add(pl);
        }

        /*
         * Big Pusher
         */
        SolidPlatform bigPusher = new SolidPlatform(208, 2090, play);
        bigPusher.zIndex(10);
        bigPusher.setImage(res.getImage("pushengine.png"));
        play.addObstacle(bigPusher);
        add(bigPusher);

        SolidPlatform cylinder = new SolidPlatform(220, 2080, play);
        cylinder.setImage(res.getImage("cylinder.png"));
        cylinder.freeze();
        cylinder.appendPath(cylinder.x(),2010);
        cylinder.setMoveSpeed(25);
        play.addObstacle(cylinder);
        add(cylinder);

        SolidPlatform pushPlatform = new SolidPlatform(192, 2064, play);
        pushPlatform.setImage(res.getImage("pushplatform.png"));
        pushPlatform.setMoveSpeed(25);
        pushPlatform.freeze();
        pushPlatform.appendPath(pushPlatform.x(), 1994, 0, false, ()-> {
            if (Collisions.rectanglesCollide(play.bounds.toRectangle(), 192, 1989, 128, 16)) {
                play.vel.y = 1300;
            }
        });
        add(pushPlatform);

        runOnceWhen(()-> {
            res.getSound("warning.ogg").play();
            runOnceAfter(()->{
                cylinder.unfreeze();
                pushPlatform.unfreeze();
                res.getSound("launch.wav").play();
            }, 120);
        }, ()-> Collisions.rectanglesCollide(play.bounds.toRectangle(), 202, 2056, 108, 16));

        /*
         * Giant Saws
         */
        int length = 2030;
        float speed = 10;

        PathDrone sawHolder = new PathDrone(564, 1430);
        sawHolder.zIndex(5);
        sawHolder.setImage(res.getImage("sawholder.png"));
        sawHolder.appendPath();
        sawHolder.appendPath(sawHolder.x() + length, sawHolder.y());
        sawHolder.setMoveSpeed(speed);
        add(sawHolder);

        int[] yPositions = {1531, 1696, 1862, 2026};
        for(int i = 0; i < 4; i++) {
            PathDrone saw = new PathDrone(503, yPositions[i]);
            saw.appendPath();
            saw.appendPath(saw.x() + length, saw.y());
            saw.setHitbox(Hitbox.CIRCLE);
            saw.setMoveSpeed(speed);
            saw.setImage(res.getImage("saw.png"));
            saw.addEvent(()->{
                if (!energized && saw.collidesWith(play)) {
                    play.touch(-100);
                }
            });
            saw.addEvent(()-> saw.rotate(7));
            add(saw);
        }

        Entity soundDummy = new Entity();
        soundDummy.sounds.maxDistance = 1400;
        soundDummy.addEvent(()-> soundDummy.move(sawHolder.x(), play.y()));

        Event calcEvent = soundDummy.sounds.dynamicVolume(sawWork);
        add(soundDummy);
        add(()->{
            if (Collisions.rectanglesCollide(play.bounds.toRectangle(), 37, 1064, 3281, 1410)) {
                calcEvent.eventHandling();
                if (!sawWork.isPlaying()) {
                    sawWork.setLooping(true);
                    sawWork.play();
                }
            } else {
                sawWork.stop();
            }
        });

        /*
         * One Way Platforms
         */
        OneWay ow = new OneWay(1800, 2128, Direction.N, play);
        ow.setImage(res.getImage("ow.png"));

        add(ow);
        add(ow.getClone().move(ow.x(), 2058));
        add(ow.getClone().move(1040, ow.y()));

        /*
         * Force Fields
         */
        Entity forceField = new Entity();
        forceField.move(1786, 2023);
        forceField.setImage(3, res.getAnimation("forcefield"));
        forceField.tint.a = .7f;
        forceField.setHitbox(Hitbox.PIXEL);

        Entity forceField2 = forceField.getClone().move(1026, 2023);

        add(forceField);
        add(forceField2);
        add(()-> {
            if (forceField.collidesWith(play) || forceField2.collidesWith(play)) {
                energized = true;
                play.tint.a = .4f;
            } else {
                energized = false;
                play.tint.a = 1;
            }

            if (energized && !energy.isPlaying()) {
                energy.setLooping(true);
                energy.play();
            } else if (!energized && energy.isPlaying()){
                energy.stop();
            }
        });

        /*
         * Electric Fields
         */
        Entity field1 = addField(640, 192, true);
//        Entity field2 = addField(640, 800, false);

        /*
         * Platforms
         */
        final int freezeFrames = 50;
        final int freezeFramesLong = freezeFrames + 50;

        SolidPlatform p1 = new SolidPlatform(730, 252, play);
        p1.setImage(res.getImage("platform3.png"));
        p1.setMoveSpeed(1.8f);
        p1.appendPath(p1.x(), p1.y(), freezeFrames, false, null);
        p1.appendPath(960, p1.y(), freezeFrames, false, null);

        SolidPlatform p2 = p1.getClone();
        p2.clearData();
        p2.move(1100, 252);
        p2.appendPath(p2.x(), p2.y(), freezeFrames, false, null);
        p2.appendPath(p2.x(), 377, freezeFrames, false, null);

        SolidPlatform p3 = new SolidPlatform(1355, 252, play);
        p3.setImage(res.getImage("platform2.png"));
        p3.setMoveSpeed(.8f);
        p3.appendPath(p3.x(), p3.y(), freezeFramesLong, false, null);
        p3.appendPath(1400, p3.y(), freezeFramesLong, false, null);

        SolidPlatform p4 = p3.getClone();
        p4.clearData();
        p4.move(1477, p4.y());
        p4.appendPath(p4.x(), p4.y(), freezeFramesLong, false, null);
        p4.appendPath(1432, p4.y(), freezeFramesLong, false, null);

        SolidPlatform p5 = p1.getClone();
        p5.bounds.pos.x = 1700;
        p5.setMoveSpeed(1);
        p5.clearData();
        p5.appendPath(p5.x(), p5.y(), freezeFramesLong, false, null);
        p5.appendPath(p5.x(), 179, freezeFramesLong, false, null);

        SolidPlatform p6 = p1.getClone();
        p6.clearData();
        p6.bounds.pos.x = 1900;

        SolidPlatform p7 = p3.getClone();
        p7.clearData();
        p7.setMoveSpeed(.7f);
        p7.move(2257 - 100, 252);
        p7.appendPath();
        p7.appendPath(2257 - 100, 317);
        p7.appendPath(2225 - 100, 317);
        p7.appendPath(2225 - 100, 252);

        SolidPlatform p8 = p7.getClone();
        p8.clearData();
        p8.move(2225 - 100, 317);
        p8.appendPath();
        p8.appendPath(2225 - 100, 252);
        p8.appendPath(2257 - 100, 252);
        p8.appendPath(2257 - 100, 317);

        add(p1);
        add(p2);
        add(p3);
        add(p4);
        add(p5);
        add(p6);
        add(p7);
        add(p8);

        /*
         * Detect Drone
         */
        PathDrone dDrone = new PathDrone(0,0);
        dDrone.setImage(res.getImage("drone.png"));
        dDrone.setMoveSpeed(4.5f);
        dDrone.setCloneEvent(clonie -> {
            PathDrone pd = (PathDrone) clonie;
            Bool detected = new Bool();
            Bool collided = new Bool();

            pd.addEvent(()-> {
                pd.rotate(7);

                if (!detected.value && pd.dist(play) < 300) {
                    Vector2 edgePoint = Collisions.findEdgePoint(pd, play, this);
                    pd.appendPath(edgePoint.x, edgePoint.y);
                    detected.value = true;
                    res.getSound("detect.wav").play();
                }

                if (pd.collidesWith(play)) {
                    collided.value = true;
                    play.touch(-1);
                } else if (pd.collidesWith(field1) || pd.getOccupyingCells().contains(Tile.SOLID)) {
                    collided.value = true;
                }

                if (collided.value) {
                    Particle particle = new Particle();
                    particle.setIntroSound(res.getSound("teslaboom.wav"));
                    particle.setImage(1, res.getAnimation("teslaexp"));
                    particle.center(pd);
                    particle.sounds.useFalloff = true;
                    add(particle);
                    discard(pd);
                }
            });
        });

        add(dDrone.getClone().move(1200, 411));
        add(dDrone.getClone().move(1907, 411));

        /*
         * Bottom Platforms
         */
        SolidPlatform p9 = p1.getClone();
        p9.clearData();
        p9.move(2167, 737);

        SolidPlatform p10 = p9.getClone();
        p10.clearData();
        p10.setMoveSpeed(1);
        p10.move(2027, 737);
        p10.appendPath(p10.x(), p10.y(), freezeFramesLong, false, null);
        p10.appendPath(1647, p10.y(), freezeFramesLong, false, null);

        SolidPlatform p11 = p9.getClone();
        p11.bounds.pos.x = 1537;

        SolidPlatform p12 = p9.getClone();
        p12.move(1427, 677 - 30);

        SolidPlatform p13 = p9.getClone();
        p13.bounds.pos.x = 1267;

        add(p9);
        add(p10);
        add(p11);
        add(p12);
        add(p13);

        /*
         * Crystal
         */
        Entity crystal = new Entity();
        crystal.setImage(4, res.getAnimation("gem"));
        crystal.addEvent(Factory.follow(p10, crystal, 18, 34));
        crystal.addEvent(()-> {
            if (play.isAlive() && play.collidesWith(crystal)) {
                res.getSound("collect1.wav").play();
                play.win();
                discard(crystal);
            }
        });
        add(crystal);

        /*
         * Power Off Button
         */
        Button btn = new Button(1284, 723, play);
        btn.setImage(res.getImage("btn.png"));
        btn.setPushingDirection(Direction.S);
        btn.setPushEvent(()->{
            res.getSound("poweroff.wav").play();
            fieldOffline = true;
        });
        add(btn);

        /*
         * Mines
         */
        int padding1 = 7;
        int padding2 = 12;

        addMine(1974, 706);
        addMine(1894 - padding1, 706);
        addMine(1894 - padding1, 672);
        addMine(1804 - padding2, 706);
        addMine(1764 - padding2, 706);
        PathDrone mine = addMine(1413, 616);
        mine.appendPath();
        mine.appendPath(1477, 616);
    }

    private PathDrone addMine(float x, float y) {
        PathDrone mine = new PathDrone(x,y);
        mine.setMoveSpeed(1);
        mine.setImage(3, res.getAnimation("mine"));
        mine.setHitbox(Hitbox.PIXEL);
        mine.addEvent(()-> {
            if (play.isAlive() && mine.collidesWith(play)) {
                Particle exp = new Particle();
                exp.setImage(1, res.getAnimation("exp"));
                exp.setIntroSound(res.getSound("mineexp.wav"));
                exp.center(mine);

                add(exp);
                discard(mine);
                play.touch(-1);
            }
        });

        add(mine);
        return mine;
    }

    Entity addField(float x, float y, boolean top) {
        final int spawnDelay = 20;
        final int width = res.getAnimation("field")[0].getWidth();
        final int height = res.getAnimation("field")[0].getHeight();
        final int teslaWidth = res.getAnimation("tesla")[0].getWidth();
        final int length = res.getAnimation("tesla").length;
        final Int32 c = new Int32();

        Entity field = new Entity();
        field.move(x, y);
        field.setImage(3, res.getAnimation("field"));
        field.flipY = top;
        field.zIndex(40);
        field.addEvent(()-> {
            if (!fieldOffline && field.collidesWith(play)) {
                play.touch(-10);
            }

            if (!fieldOffline && ++c.value % spawnDelay == 0) {
                Particle particle = new Particle();
                particle.setImage(1, res.getAnimation("tesla"));
                particle.setIntroSound(res.getSound("spark.wav"));
                particle.move(MathUtils.random(x, x + width - teslaWidth), top ? (y + height - particle.halfHeight()) : (y - particle.halfHeight()));
                particle.sounds.useFalloff = true;
                particle.sounds.power = 10;
                particle.zIndex(-1);
                particle.addEvent(()->{
                    if(particle.getImage().getIndex() > length / 2)
                        particle.tint.a -= .05f;
                });
                add(particle);
            }
        });

        runOnceWhen(()-> {
            field.setImage(res.getImage("fieldoffline.png"));
            play.addObstacle(field);
        }, ()-> fieldOffline);
        add(field);

        return field;
    }

    private void spikes(float x, float y, Image2D image) {
        Entity spikes = new Entity();
        spikes.move(x, y);
        spikes.setImage(image);
        spikes.addEvent(Factory.hitMain(spikes, play, -1));
        spikes.setHitbox(Hitbox.PIXEL);
        add(spikes);
    }

    private void addSwitch(float x, float y, boolean flipY, Event event) {
        Button button = new Button(x, y, play);
        button.setImage(res.getImage("switchButton.png"));
        button.flipY = flipY;
        button.setPushingDirection(flipY ? Direction.N : Direction.S);
        button.setPushEvent(()-> {
            play.flip();
            res.getSound("flip.wav").play();
            add(new Flash(res.getImage("flash"), 100f));
            if (event != null)
                event.eventHandling();
         });
        add(button);
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
        return "Death Egg";
    }
}
