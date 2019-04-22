package pojahn.game.desktop.redguyruns.levels.steel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.BaseLogic;
import pojahn.game.core.Entity;
import pojahn.game.desktop.redguyruns.util.GFX;
import pojahn.game.desktop.redguyruns.util.ResourceUtil;
import pojahn.game.entities.enemy.weapon.Bullet;
import pojahn.game.entities.enemy.weapon.Missile;
import pojahn.game.entities.enemy.weapon.Weapon;
import pojahn.game.entities.image.RepeatingParallaxImage;
import pojahn.game.entities.main.GravityMan;
import pojahn.game.entities.movement.Circle;
import pojahn.game.entities.movement.PathDrone;
import pojahn.game.entities.movement.Waypoint;
import pojahn.game.entities.object.Collectable;
import pojahn.game.entities.object.Wind;
import pojahn.game.entities.particle.Particle;
import pojahn.game.entities.particle.Shrapnel;
import pojahn.game.entities.platform.SolidPlatform;
import pojahn.game.entities.platform.TilePlatform;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.Checkpoint;
import pojahn.game.essentials.Direction;
import pojahn.game.essentials.EntityBuilder;
import pojahn.game.essentials.Factory;
import pojahn.game.essentials.Hitbox;
import pojahn.game.essentials.Image2D;
import pojahn.game.essentials.ItemCollection;
import pojahn.game.essentials.ResourceManager;
import pojahn.game.essentials.Utils;
import pojahn.game.essentials.geom.Size;
import pojahn.game.essentials.stages.TileBasedLevel;
import pojahn.game.events.Event;
import pojahn.lang.PingPongFloat;

import java.io.Serializable;
import java.util.Objects;
import java.util.stream.Stream;

public class SteelFactory extends TileBasedLevel {

    private ResourceManager res;
    private GravityMan man;
    private ItemCollection itemCollection, energyCollection;

    @Override
    public void load(final Serializable meta) throws Exception {
        res = new ResourceManager();
        res.loadContentFromDirectory(Gdx.files.internal("res/data"));
        res.loadContentFromDirectory(Gdx.files.internal("res/general"));
        res.loadContentFromDirectory(Gdx.files.internal("res/steel"));
        res.loadAnimation(Gdx.files.internal("res/clubber/trailer"));
        res.loadAnimation(Gdx.files.internal("res/clubber/gunfire"));
        res.loadAnimation(Gdx.files.internal("res/orbit/smallexp"));
        res.loadSound(Gdx.files.internal("res/clubber/fire.wav"));
        res.loadSound(Gdx.files.internal("res/cave/exp1.wav"));
        res.loadSound(Gdx.files.internal("res/cave/exp2.wav"));
        res.loadMusic(Gdx.files.internal("res/deathegg/sawwork_music.wav"));

        Stream.of(res.getAnimation("main")).forEach(Image2D::createPixelData);
        Stream.of(res.getAnimation("zapper")).forEach(Image2D::createPixelData);
        Stream.of(res.getAnimation("wind")).forEach(Image2D::createPixelData);
        Stream.of(res.getAnimation("magic_bullet")).forEach(Image2D::createPixelData);
        Stream.of(res.getAnimation("electric")).forEach(Image2D::createPixelData);
        Stream.of(res.getAnimation("thing")).forEach(Image2D::createPixelData);
        res.getImage("mov_platform.png").createPixelData();
        res.getImage("pusher_right_follower.png").createPixelData();
        res.getImage("pusher_down_follower.png").createPixelData();
        res.getImage("pusher_up_follower.png").createPixelData();
        res.getImage("laserbeam.png").createPixelData();
        getEngine().timeFont = res.getFont("sansserif32.fnt");

        Utils.playMusic(res.getMusic("music.ogg"), 4.48f, .7f);

        getCheckpointHandler().setReachEvent(() -> GFX.renderCheckpoint(res, this));
        getCheckpointHandler().appendCheckpoint(new Checkpoint(standOn(62, 37, Size.from(res.getAnimation("main")[0]))){
            @Override
            public boolean reached(final Entity entity) {
                final Rectangle rectangle = getRectangle(62, 37, 1, 1);
                return getEnergyCollection().allCollected() && entity.collidesWith(rectangle);
            }
        });
        getCheckpointHandler().appendCheckpoint(new Vector2(1811, 4407 - 25), new Rectangle(1811, 4405, 71, 2));
    }

    @Override
    public TiledMap getTileMap() {
        return res.getTiledMap("map.tmx");
    }

    @Override
    public void build() {
        Utils.handleDistanceMusic(res.getMusic("sawwork_music.wav"));
        Utils.handleDistanceMusic(res.getMusic("small_saw_loop_music.wav"));
        Utils.handleDistanceMusic(res.getMusic("propeller_music.wav"));
        Utils.handleDistanceMusic(res.getMusic("laserloop_music.wav"));

        /*
         * Main Character
         */
        man = ResourceUtil.getGravityMan(res);
        man.face(Direction.W);
        man.move(standOn(65, 8, man.bounds.size));
        man.setAllowSlopeWalk(false);
        add(man);

        /*
         * Background and Foreground
         */
        final Entity worldImage = getWorldImage();
        worldImage.zIndex(1000);
        add(worldImage);
        add(new EntityBuilder().image(res.getImage("background.png")).zIndex(-100).build(RepeatingParallaxImage.class));
        add(new EntityBuilder().image(res.getImage("bgstuff.png")).zIndex(-99).move(1837, 2818).build());

        /*
         * Electrics & Energy
         */
        addElectric(7423, 3483, true);
        addElectric(7423, 3173, true);

        addElectric(8031, 3483, false);
        addElectric(8031, 3173, false);

        add(ResourceUtil.getHearth(res, man).move(8040, 3582));
        add(ResourceUtil.getHearth(res, man).move(1992, 3123));
        energyCollection = new ItemCollection();
        if (!getCheckpointHandler().reached(0)) {
            addEnergyItem(7426, 3215 + 60);
            addEnergyItem(7426, 3580 - 60);
            addEnergyItem(8049, 3215 + 60);
        }

        final SolidPlatform small = new SolidPlatform(7574, 3447, man);
        small.setImage(res.getImage("mini_platform.png"));
        add(small);
        add(small.getClone().move(7850, small.y()));

        /*
         * Giant Saws
         */
        for (int i = 0; i <= 60; i+=2) {
            final Entity giantSaw = new Entity();
            giantSaw.setImage(res.getImage("giant_saw.png"));
            giantSaw.move(center(i, 41, giantSaw.bounds.size));
            giantSaw.bounds.pos.y -= 65;
            giantSaw.bounds.pos.x += 55;
            giantSaw.addEvent(()-> giantSaw.rotate(3.5f));
            giantSaw.setHitbox(Hitbox.CIRCLE);
            giantSaw.ifCollides(man).thenRunOnce(man::lose);
            add(giantSaw);

            final Entity giantHolder = new Entity();
            giantHolder.setImage(res.getImage("giant_holder.png"));
            giantHolder.move(center(i, 41, giantHolder.bounds.size));
            giantHolder.bounds.pos.y -= 65;
            giantHolder.bounds.pos.x += 55;
            add(giantHolder);
        }
        final Entity sawSoundPlayer = Utils.stuckFollower(man, new Rectangle(0, 5024, 7900, 224));
        sawSoundPlayer.sounds.useFalloff = true;
        sawSoundPlayer.sounds.maxDistance = 500;
        sawSoundPlayer.sounds.power= 10;
        sawSoundPlayer.addEvent(()-> res.getMusic("sawwork_music.wav").setVolume(sawSoundPlayer.sounds.calc()));
        add(sawSoundPlayer);

        /*
         * Pushers
         */
        final SolidPlatform pushRight = getPusher(5344, 4501, "RIGHT");
        pushRight.setMoveSpeed(5);
        pushRight.appendPath(5858, 4290);
        pushRight.appendPath(6258 + 100, 4290, ()-> {
            pushRight.freeze();
            pushRight.setMoveSpeed(17);
            res.getSound("pusher_reach.wav").play();
        });
        pushRight.appendPath(2997, 4138, () -> pushRight.setMoveSpeed(8));
        pushRight.appendPath(3064 + 44, 4443, ()-> {
            pushRight.freeze();
            res.getSound("pusher_reach.wav").play();
        });

        final SolidPlatform pushUp = getPusher(5068, 4528, "UP");
        pushUp.setMoveSpeed(5);
        pushUp.appendPath(5474, 4305, ()-> pushUp.setMoveSpeed(3.35f));
        pushUp.appendPath(5474, 3840, ()-> {
            pushUp.freeze();
            pushUp.setMoveSpeed(15);
            res.getSound("pusher_reach.wav").play();
        });
        pushUp.appendPath(3856, 4656, ()-> pushUp.setMoveSpeed(7));
        pushUp.appendPath(3171, 4615, ()-> {
            pushUp.freeze();
            res.getSound("pusher_reach.wav").play();
        });

        final SolidPlatform pushDown = getPusher(4083 - 400, 4212 + 44 - 400, "DOWN");
        pushDown.setMoveSpeed(5);
        pushDown.appendPath(4375, 4325 + 44);
        pushDown.appendPath(4375, 4525 + 44, ()-> {
            pushDown.freeze();
            pushDown.setMoveSpeed(10);
            res.getSound("pusher_reach.wav").play();
        });
        pushDown.appendPath(3951, 4309 + 44);
        pushDown.appendPath(3171, 4334 + 44, ()-> {
            pushDown.freeze();
            res.getSound("pusher_reach.wav").play();
        });

        /*
         * Solid Platforms
         */
        final SolidPlatform p1 = getPlatform(7847, 4890, energyCollection);
        p1.appendPath(7150, p1.y());

        final SolidPlatform p2 = getPlatform(7059, 4921, energyCollection);
        p2.appendPath(p2.x(),4622);

        final SolidPlatform p3 = getPlatform(6952, 4632, energyCollection);
        p3.appendPath(6140, p3.y());

        final SolidPlatform p4 = getPlatform(6035, 4666, () -> runOnceAfter(()-> {
            pushRight.unfreeze();
            res.getSound("pusher_move.wav").play();
        }, 30), energyCollection);
        p4.appendPath(p4.x(), 3987);

        final SolidPlatform p5 = getPlatform(5927, 3992, ()-> runOnceAfter(() -> {
            pushUp.unfreeze();
            res.getSound("pusher_move.wav").play();
        }, 90), energyCollection);
        p5.appendPath(5117, p5.y());

        final SolidPlatform p6 = getPlatform(5012, 3984, energyCollection);
        p6.appendPath(p6.x(), 4537);
        p6.setFollowMode(SolidPlatform.FollowMode.STRICT);

        final SolidPlatform p7 = getPlatform(4903, 4631, ()-> runOnceAfter(() -> {
            pushDown.unfreeze();
            res.getSound("pusher_move.wav").play();
        }, 100), energyCollection);
        p7.appendPath(3963, p7.y());
        final TileLayer tileLayer = Utils.fromImage(p7.getImage().getCurrentObject());
        addTileLayer(tileLayer);
        p7.addEvent(()-> tileLayer.setPosition((int)p7.x(), (int)p7.y()));

        final SolidPlatform p8 = getPlatform(3880, 4503, () -> {
            pushDown.unfreeze();
            pushRight.unfreeze();
            pushUp.unfreeze();
            res.getSound("pusher_move.wav").play();
            res.getSound("pusher_move.wav").play();
            res.getSound("pusher_move.wav").play();
        }, energyCollection);
        p8.appendPath(1917 + 30, p8.y());

        final SolidPlatform p10 = getPlatform(1811, 4407, energyCollection);
        p10.appendPath(p10.x(), 2830);
        p10.setMoveSpeed(1);

        /*
         * Saw
         */
        final PathDrone saw = new PathDrone(6847, 4587);
        saw.setImage(res.getImage("saw.png"));
        saw.addEvent(()-> saw.rotate(5));
        saw.addEvent(Factory.hitMain(saw, man, -1));
        saw.setHitbox(Hitbox.CIRCLE);
        saw.setMoveSpeed(4);
        saw.appendPath();
        saw.sounds.useFalloff = true;
        saw.addEvent(()-> {
            res.getMusic("small_saw_loop_music.wav").setVolume(saw.sounds.calc());
        });
        saw.appendPath(6157, saw.y());

        final Entity sawHolder = new Entity();
        sawHolder.zIndex(2);
        sawHolder.setImage(res.getImage("saw_holder.png"));
        saw.addEvent(()-> sawHolder.center(saw));

        add(saw);
        add(sawHolder);

        /*
         * Fan, Engine & Zapper
         */
        if (!getCheckpointHandler().reached(1)) {
            final PathDrone zapperGhost = new PathDrone(1936 + 400, 4424);
            zapperGhost.setMoveSpeed(2);
            zapperGhost.freeze();
            zapperGhost.appendPath(new Waypoint.DynamicWaypoint(p8::centerX, p8::centerY));
            add(zapperGhost);

            final Circle zapper = new Circle(zapperGhost.x(), zapperGhost.y(), 10, 0);
            zapper.setMoveSpeed(.05f);
            zapper.setCenterize(true);
            zapper.setImage(2, res.getAnimation("zapper"));
            zapper.setHitbox(Hitbox.PIXEL);
            zapper.zIndex(100);
            zapper.addEvent(()-> {
                zapper.setCenter(zapperGhost.bounds.center());
                if (!man.isHurt() && zapper.collidesWith(man)) {
                    man.touch(-1);
                    res.getSound("zap.wav").play();
                }
            });
            add(zapper);

            final PathDrone engine = new PathDrone(1936, 4424);
            engine.setImage(1, res.getAnimation("engine"));
            engine.setMoveSpeed(5);
            engine.freeze();
            engine.appendPath(new Waypoint.DynamicWaypoint(()-> p8.x() - 320, engine::y, ()-> engine.setMoveSpeed(p8.getMoveSpeed())));
            engine.appendPath(0, engine.y());
            add(engine);
            runOnceWhen(()-> {
                engine.unfreeze();
                zapperGhost.unfreeze();
                addAfter(()-> {
                    zapper.setRadius(Math.min(zapper.getRadius() + .30f, 50));
                }, 100);
            }, ()-> p8.x() < 2970);

            final Entity fan = new Entity();
            fan.setImage(1, res.getAnimation("fan"));
            fan.addEvent(Factory.hitMain(fan, man, -1));
            fan.addEvent(()-> res.getMusic("propeller_music.wav").setVolume(fan.sounds.calc()));
            fan.sounds.useFalloff = true;
            fan.sounds.maxDistance = 1200;
            fan.sounds.power = 40;
            engine.addEvent(()-> fan.move(engine.x() + engine.width() - 23, engine.y() + 3));
            add(fan);

            final Wind wind = new Wind(0, 0, 50, 1, Direction.E, man);
            wind.setHitbox(Hitbox.PIXEL);
            wind.setImage(1, res.getAnimation("wind"));
            add(wind);
            fan.addEvent(()-> wind.move(fan.x() + 17, fan.y() + 10));
        }

        /*
         * Lasers
         */
        final SolidPlatform laserPlatform1 = new SolidPlatform(7424, 2992, man);
        laserPlatform1.setImage(res.getImage("laseremitter.png"));
        final SolidPlatform laserPlatform2 = laserPlatform1.getClone();
        laserPlatform2.bounds.pos.x = 8048;
        laserPlatform2.flipX = true;
        add(laserPlatform1);
        add(laserPlatform2);

        final PingPongFloat alphaProvider1 = new PingPongFloat(.8f, 1f, .025f);
        final PingPongFloat alphaProvider2 = new PingPongFloat(.8f, 1f, .025f);
        final Entity laserEdge1 = new Entity();
        laserEdge1.setImage(res.getImage("laserfront.png"));
        laserEdge1.move(7440, 2996);
        laserEdge1.addEvent(()-> laserEdge1.tint.a = alphaProvider1.get());
        final Entity laserEdge2 = laserEdge1.getClone();
        laserEdge2.bounds.pos.x = 8042;
        laserEdge2.flipX = true;
        laserEdge2.addEvent(()-> laserEdge2.tint.a = alphaProvider2.get());
        add(laserEdge1);
        add(laserEdge2);

        final PingPongFloat scaleProvider = new PingPongFloat(.5f, 1.2f, .035f);
        final Entity laserBeam = new Entity();
        laserBeam.setImage(res.getImage("laserbeam.png"));
        laserBeam.move(7437, 3001);
        laserBeam.zIndex(-2);
        laserBeam.addEvent(()-> laserBeam.scaleY = scaleProvider.get());
        laserBeam.addEvent(Factory.hitMain(laserBeam, man, -10));
        add(laserBeam);

        final Entity laserSoundEmitter = Utils.stuckFollower(man, laserBeam.bounds.toRectangle());
        laserSoundEmitter.sounds.useFalloff = true;
        laserSoundEmitter.addEvent(()-> res.getMusic("laserloop_music.wav").setVolume(laserSoundEmitter.sounds.calc()));
        add(laserSoundEmitter);

        /*
         * Hunter
         */
        final Particle magicGunfire = Particle.fromSound(res.getSound("magicfire.wav"));
        final Particle magicImpact = Particle.imageParticle(2, res.getAnimation("spark"));
        magicImpact.setIntroSound(res.getSound("magicexplode.wav"));
        magicGunfire.sounds.useFalloff = true;
        magicImpact.sounds.useFalloff = true;

        final Bullet magicBullet = new Bullet(man);
        magicBullet.setImage(2, res.getAnimation("magic_bullet"));
        magicBullet.setMoveSpeed(1.7f);
        magicBullet.setGunfire(magicGunfire);
        magicBullet.setImpact(magicImpact);
        magicBullet.setHitbox(Hitbox.PIXEL);
        magicBullet.addObstacle(laserBeam);

        final Weapon laserEnemy = new Weapon(8005, 2069, 1, 1, 80, man);
        laserEnemy.flipX = true;
        laserEnemy.setProjectile(magicBullet);
        laserEnemy.setMoveSpeed(1);
        laserEnemy.freeze();
        laserEnemy.setImage(3, res.getAnimation("laser_enemy"));
        laserEnemy.addEvent(()-> BaseLogic.rotateTowards(laserEnemy, man, .05f));
        laserEnemy.appendPath(new Waypoint.DynamicWaypoint(man::x, man::y));
        add(laserEnemy);
        runOnceWhenMainCollides(laserEnemy::unfreeze, 58, 16, 1, 1);

        /*
         * Red Platforms
         */
        addRed(7663, 2260, true);
        addRed(7556, 2397, false);
        addRed(7556, 2667, false);
        addRed(7896, 2397, false).flipX = true;
        addRed(7896, 2667, false).flipX = true;

        itemCollection = new ItemCollection();
        addCollectable(7731, 2602);
        addCollectable(7731, 2298);
        addCollectable(7594, 2468);
        addCollectable(7872, 2468);
        addCollectable(7594, 2738);
        addCollectable(7872, 2738);

        runOnceWhen(()-> {
            discard(laserSoundEmitter);
            discard(laserEdge1);
            discard(laserEdge2);
            discard(laserBeam);
            discard(laserEnemy);
            add(Particle.imageParticle(3, res.getAnimation("explosion")).center(laserEnemy));
            res.getMusic("laserloop_music.wav").stop();
            res.getSound("laserremove.wav").play();
        }, itemCollection::allCollected);

        /*
         * Moving Up Area
         */
        add(new EntityBuilder().move(1536, 2830).image(res.getImage("lots_of_spikes.png")).event(entity -> Factory.hitMain(entity, man, -1)).build());
        add(new EntityBuilder().move(2149, 2830).image(res.getImage("lots_of_spikes.png")).event(entity -> Factory.hitMain(entity, man, -1)).flipX(true).build());

        addPlatform2(1669, 3728);
        addPlatform2(1973, 3728);

        addPlatform2(1669, 3449);
        addPlatform2(1973, 3449);

        addPlatform2(1669, 3148);
        addPlatform2(1973, 3148);

        addPlatform2(1669, 2858);
        addPlatform2(1973, 2858);

        addThing(1793, 3666);
        addThing(1832, 3666);
        addThing(1872, 3666);
        addThing(1663, 3403 + 10);
        addThing(1703, 3403 + 10);
        addThing(1792, 3376);
        addThing(1832, 3376);
        addThing(1872, 3376);
        addThing(1832, 3326);
        addThing(1832, 3276);
//        addThing(1832, 3176);
        addThing(1792, 3126);
        addThing(1832, 3126);
        addThing(1872, 3126);
        addThing(1832, 3026);
        addThing(1872, 3026);
        addThing(1912, 3026);

        addThing(1832, 2976);
        addThing(1832, 2926);
        addThing(1832, 2876);

        addThing(1703, 2823);
        addThing(1663, 2823);

        /*
         * Power Sign
         */
        final Entity powerSign = new Entity();
        powerSign.setImage(res.getImage("poweroff_sign.png"));
        powerSign.move(7989 - 30, 4787);
        powerSign.zIndex(-1);
        add(powerSign);
        runOnceWhen(()-> powerSign.setImage(res.getImage("poweron_sign.png")), energyCollection::allCollected);

        /*
         * Turret And Missile
         */
        final Particle splitImpact = Particle.imageParticle(2, res.getAnimation("smallexp"));
        splitImpact.setIntroSound(res.getSound("exp1.wav"));
        splitImpact.sounds.useFalloff = true;

        final Bullet split = new Bullet(man);
        split.setImage(2, res.getAnimation("split"));
        split.setImpact(splitImpact);
        split.setMoveSpeed(8);

        final Shrapnel shrapnel = new Shrapnel(split);
        shrapnel.sounds.useFalloff = true;
        shrapnel.setIntroSound(res.getSound("exp2.wav"));
        shrapnel.setImage(2, res.getAnimation("explosion"));

        final Missile missile = new Missile(man);
        missile.setImage(res.getImage("missileimg.png"));
        missile.setTrailer(Particle.imageParticle(3, res.getAnimation("trailer")));
        missile.fastVeryFloaty();
        missile.setImpact(shrapnel);

        final Particle weaponFire = Particle.imageParticle(3, res.getAnimation("gunfire"));
        weaponFire.setIntroSound(res.getSound("fire.wav"));

        final Weapon weapon = new Weapon(19 * getTileWidth(), 2383, 1, 1, 200, man);
        weapon.setProjectile(missile);
        weapon.setFiringParticle(weaponFire);
        weapon.setFrontFire(true);
        weapon.rotate(45);
        weapon.setImage(res.getImage("turretimg.png"));
        weapon.setRotationSpeed(.020f);
        weapon.setRotateWhileRecover(false);
        weapon.setMoveSpeed(2);
        weapon.setMoveSpeed(.6f);
        weapon.setAlwaysRotate(false);
        weapon.setRotateWhileRecover(true);
        weapon.activate(false);

        add(weapon);
        runOnceWhenMainCollides(()-> weapon.activate(true), 20, 19, 1, 3);

        /*
         * Spike Cars
         */
        addSpikeCar(2438, 2656, 233 - 30, Direction.N);
        addSpikeCar(3040, 2776, 223 - 20, Direction.W);
        addSpikeCar(3328, 2784, 639 - 30, Direction.N);

        /*
         * Goal
         */
        final Entity goal = new Entity();
        goal.move(4248, 2338);
        goal.setImage(res.getImage("door.png"));
        goal.zIndex(-1);
        add(goal);
        runOnceWhen(man::win, ()-> goal.collidesWith(man));

        /*
         * Hit Event
         */
        man.setActionEvent(caller -> {
            if (caller.isCloneOf(magicBullet) || caller.isCloneOf(missile) || caller.isCloneOf(split)) {
                man.touch(-1);
            }
        });
    }

    private void addSpikeCar(final float x, final float y, final float length, final Direction direction) {
        if (!direction.isNorth()  && direction.isWest() && direction.isEast()) {
            throw new IllegalArgumentException("Expected W, E or N");
        }

        final PathDrone spikeCar = new PathDrone(x, y);
        spikeCar.setMoveSpeed(1f);
        spikeCar.appendPath();
        switch (direction) {
            case N:
                spikeCar.setImage(res.getImage("spikecar.png"));
                spikeCar.appendPath(x + length, y);
                break;
            case W:
                spikeCar.setImage(res.getImage("spikecar2.png"));
                spikeCar.appendPath(x, y - length);
                break;
            case E:
                spikeCar.setImage(res.getImage("spikecar3.png"));
                spikeCar.appendPath(x, y - length);
                break;
        }
        spikeCar.addEvent(Factory.hitMain(spikeCar, man, -1));

        add(spikeCar);
    }

    private void addThing(final float x, final float y) {
        add(new EntityBuilder()
            .move(x, y)
            .hitbox(Hitbox.PIXEL)
            .zIndex(5)
            .image(3, res.getAnimation("thing"))
            .event(entity -> Factory.hitMain(entity, man, -1))
            .build());
    }

    private void addPlatform2(final float x, final float y) {
        final SolidPlatform platform2 = new SolidPlatform(x, y, man);
        platform2.setImage(res.getImage("platform2.png"));
        final Entity platform2Spikes = new Entity();
        platform2Spikes.setImage(res.getImage("platform2_spikes.png"));
        platform2Spikes.addEvent(Factory.hitMain(platform2Spikes, man, -1));
        platform2Spikes.move(platform2.bounds.pos);
        platform2Spikes.nudge(0, platform2.height());
        add(platform2);
        add(platform2Spikes);
    }

    private void addEnergyItem(final float x, final float y) {
        final Collectable item = new Collectable(x, y, man);
        item.setImage(2, res.getAnimation("energy"));
        item.setCollectSound(res.getSound("collectsound2.wav"));
        energyCollection.add(item);
        add(item);
    }

    private void addElectric(final float x, final float y, boolean flipX) {
        final Entity electric = new Entity();
        electric.move(x, y);
        electric.flipX = flipX;
        electric.setHitbox(Hitbox.PIXEL);
        electric.addEvent(Factory.hitMain(electric, man, -1));
        electric.sounds.useFalloff = true;

        final Animation<Image2D> electricImg = Image2D.animation(2, res.getAnimation("electric"));
        electricImg.addEvent(()-> electric.sounds.play(res.getSound("buzz.wav")),27);
        electric.setImage(electricImg);

        add(electric);
    }

    private void addCollectable(final float x, final float y) {
        final Collectable item = new Collectable(x, y, man);
        item.setImage(res.getImage("collect.png"));
        item.setCollectSound(res.getSound("collectsound.wav"));
        itemCollection.add(item);
        add(item);
    }

    private SolidPlatform addRed(final int x, final int y, boolean vertical) {
        final SolidPlatform redPlatform = new SolidPlatform(x, y, man);
        redPlatform.setImage(vertical ? res.getImage("red1.png") : res.getImage("red2.png"));
        add(redPlatform);
        return redPlatform;
    }

    private SolidPlatform getPlatform(final float x, final float y, final ItemCollection itemCollection) {
        return getPlatform(x, y, null, itemCollection);
    }

    private SolidPlatform getPlatform(final float x, final float y, final Event event, final ItemCollection itemCollection) {
        final SolidPlatform solidPlatform = new SolidPlatform(x, y, man);
        solidPlatform.setImage(res.getImage("mov_platform.png"));
        solidPlatform.setMoveSpeed(1.5f);
        solidPlatform.freeze();
        runOnceWhen(() -> {
                solidPlatform.unfreeze();
                if (event != null) event.eventHandling();
            },
            ()-> itemCollection.allCollected() && BaseLogic.rectanglesCollide(man.bounds.toRectangle(), solidPlatform.x(), solidPlatform.y() - 2, solidPlatform.width(), 2));
        add(solidPlatform);
        return solidPlatform;
    }

    private SolidPlatform getPusher(final float x, final float y, final String pusherDir) {
        final SolidPlatform pusher = new TilePlatform(x, y, man);
        pusher.freeze();
        pusher.zIndex(10);

        final Entity pusherFollower = new Entity();
        pusherFollower.zIndex(10);

        final Event followEvent;

        if (Objects.equals(pusherDir, "RIGHT")) {
            pusher.setImage(res.getImage("pusher_right.png"));
            pusherFollower.setImage(res.getImage("pusher_right_follower.png"));
            followEvent = () -> pusherFollower.move(pusher.x() - pusherFollower.width(), pusher.y());
        } else if (Objects.equals(pusherDir, "DOWN")) {
            pusher.setImage(res.getImage("pusher_down.png"));
            pusherFollower.setImage(res.getImage("pusher_down_follower.png"));
            followEvent = () -> pusherFollower.move(pusher.x(), pusher.y() - pusherFollower.height());
        } else if (Objects.equals(pusherDir, "UP")) {
            pusher.setImage(res.getImage("pusher_up.png"));
            pusherFollower.setImage(res.getImage("pusher_up_follower.png"));
            followEvent = () -> pusherFollower.move(pusher.x(), pusher.y() + pusher.height());
        } else {
            throw new IllegalArgumentException("Wrong pushing dir");
        }

        final TileLayer tileLayer = Utils.fromImage(pusherFollower.getImage().getCurrentObject());
        addTileLayer(tileLayer);

        pusher.addEvent(()-> {
            followEvent.eventHandling();
            tileLayer.setPosition((int)pusherFollower.x(), (int)pusherFollower.y());
        });

        add(pusherFollower);
        add(pusher);
        return pusher;
    }

    @Override
    public void dispose() {
        res.disposeAll();
    }

    @Override
    public String getLevelName() {
        return "Steel Factory";
    }

    @Override
    public Music getStageMusic() {
        return res.getMusic("music.ogg");
    }

    private ItemCollection getEnergyCollection() {
        return energyCollection;
    }

    private ItemCollection getItemCollection() {
        return itemCollection;
    }
}
