package pojahn.game.desktop.redguyruns.levels.diamond;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.Engine;
import pojahn.game.core.Entity;
import pojahn.game.core.PlayableEntity;
import pojahn.game.desktop.redguyruns.util.GFX;
import pojahn.game.desktop.redguyruns.util.ResourceUtil;
import pojahn.game.entities.enemy.weapon.Bullet;
import pojahn.game.entities.movement.Circle;
import pojahn.game.entities.image.ParallaxImage;
import pojahn.game.entities.movement.Waypoint;
import pojahn.game.entities.platform.DestroyablePlatform;
import pojahn.game.entities.object.OneWay;
import pojahn.game.entities.particle.Particle;
import pojahn.game.entities.movement.PathDrone;
import pojahn.game.entities.enemy.weapon.SimpleWeapon;
import pojahn.game.entities.platform.SolidPlatform;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.Direction;
import pojahn.game.essentials.EntityBuilder;
import pojahn.game.essentials.Factory;
import pojahn.game.essentials.Hitbox;
import pojahn.game.essentials.Image2D;
import pojahn.game.essentials.ResourceManager;
import pojahn.game.essentials.Utils;
import pojahn.game.essentials.stages.PixelBasedLevel;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

public class DiamondCave extends PixelBasedLevel {

    private ResourceManager res;
    private Music music;
    private PlayableEntity play;
    private Particle blingbling;
    private Image2D[] weakDie;
    private int counter, crystals, crystalsTaken;

    @Override
    public void init(final Serializable meta) throws Exception {
        res = new ResourceManager();
        res.loadContentFromDirectory(Gdx.files.internal("res/data"));
        res.loadContentFromDirectory(Gdx.files.internal("res/general"));
        res.loadContentFromDirectory(Gdx.files.internal("res/diamond"));

        getEngine().timeFont = res.getFont("sansserif32.fnt");
        Stream.of(res.getAnimation("main")).forEach(Image2D::createPixelData);
        Stream.of(res.getAnimation("bear")).forEach(Image2D::createPixelData);
        Stream.of(res.getAnimation("bearAttack")).forEach(Image2D::createPixelData);
        Stream.of(res.getAnimation("bug1")).forEach(Image2D::createPixelData);
        Stream.of(res.getAnimation("bug2")).forEach(Image2D::createPixelData);
        Stream.of(res.getAnimation("bug3")).forEach(Image2D::createPixelData);
        Stream.of(res.getAnimation("bug4")).forEach(Image2D::createPixelData);
        Stream.of(res.getAnimation("bacteria")).forEach(Image2D::createPixelData);
        createMap(res.getPixmap("pixmap.png"));

        weakDie = new Image2D[10];
        for (int i = 0; i < weakDie.length; i++) {
            weakDie[i] = i % 2 == 0 ? res.getImage("weak.png") : res.getImage("blank.png");
        }

        music = res.getMusic("music.ogg");
        Utils.playMusic(music, 5.85f, .5f);

        getCheckpointHandler().setReachEvent(() -> GFX.renderCheckpoint(res, this));
        getCheckpointHandler().appendCheckpoint(new Vector2(2187, 276), new Rectangle(2116, 111, 308, 295));
        getCheckpointHandler().appendCheckpoint(new Vector2(4899, 785), new Rectangle(4662, 104, 308, 786));
    }

    @Override
    public void build() {
        crystals = crystalsTaken = 0;

        /*
         * Main Character
         */
        play = ResourceUtil.getGravityMan(res);
        play.setMoveSpeed(10);
        play.move(100, 911);
        play.zIndex(50);
        add(play);
        runOnceWhen(play::lose, () -> outOfBounds(0, play.y()));

        /*
         * Background and foreground
         */
        add(new EntityBuilder().image(res.getImage("foreground.png")).zIndex(Integer.MAX_VALUE).build());
        add(new EntityBuilder().image(res.getImage("background.png")).zIndex(-5).build(ParallaxImage.class));

        /*
         * Top Spikes
         */
        final Entity topSpikes = new Entity();
        topSpikes.move(3266, 305);
        topSpikes.setImage(res.getImage("topspikes.png"));
        topSpikes.ifCollides(play).then(() -> play.touch(-1));
        add(topSpikes);

        /*
         * Bear
		 */
        final Bear b = new Bear(1544 - 3, 227, play);
        b.setImage(7, res.getAnimation("bear"));
        b.setAttackImage(new Animation<>(6, res.getAnimation("bearAttack")));
        b.zIndex(200);
        b.setHitbox(Hitbox.PIXEL);
        b.addEvent(Factory.hitMain(b, play, -1));
        b.setAttackSound(res.getSound("roar.wav"));

        add(b);

		/*
         * Bouncing Random Enemies
		 */
        final PathDrone ram1 = new PathDrone(2303, 297);
        ram1.setImage(res.getImage("sharp.png"));
        ram1.setMoveSpeed(1.1f);
        ram1.setRock(true, true);
        ram1.setHitbox(Hitbox.CIRCLE);
        final PathDrone ram2 = ram1.getClone();
        ram2.move(ram1.x() + ram1.width() + 100, ram1.y());
        final PathDrone ram3 = ram1.getClone();
        ram3.move(ram1.x(), ram1.y() + ram1.halfHeight() + 100);
        final int rotationSpeed = 15;

        ram1.appendPath((Waypoint[]) res.getAsset("data1.obj"));
        ram1.addEvent(Factory.hitMain(ram1, play, -1));
        ram1.addObstacle(ram2);
        ram1.addObstacle(ram3);
        ram1.addEvent(() -> ram1.rotate(rotationSpeed));

        ram2.appendPath((Waypoint[]) res.getAsset("data2.obj"));
        ram2.addEvent(Factory.hitMain(ram2, play, -1));
        ram2.addObstacle(ram1);
        ram2.addObstacle(ram3);
        ram2.addEvent(() -> ram2.rotate(rotationSpeed));

        ram3.appendPath((Waypoint[]) res.getAsset("data3.obj"));
        ram3.addEvent(Factory.hitMain(ram3, play, -1));
        ram3.addObstacle(ram1);
        ram3.addObstacle(ram2);
        ram3.addEvent(() -> ram3.rotate(rotationSpeed));

        add(ram1);
        add(ram2);
        add(ram3);

		/*
		 * Weak Platforms
		 */
        final int weakWidth = res.getImage("weak.png").getWidth();
        for (int i = 0, x = 2303, y = 1026; i < 10; i++, x += weakWidth) {
            final DestroyablePlatform w = new DestroyablePlatform(x, y, play);
            w.move(x, y);
            w.setImage(res.getImage("weak.png"));
            w.setDestroyImage(new Animation<>(10, weakDie));
            w.setDestroyFrames(120);
            add(w);
        }

        /*
         * Hearth
         */
        add(ResourceUtil.getHearth(res, play).move(2305, 995));

		/*
		 * One-Way Platforms
		 */
        final OneWay oneway = new OneWay(2684, 992, Direction.N, play);
        oneway.setImage(res.getImage("ow.png"));

        add(oneway);
        add(oneway.getClone().move(2857, 912));
        add(oneway.getClone().move(2677, 832));
        add(oneway.getClone().move(2777, 757));
        add(oneway.getClone().move(2827, 667));
        add(oneway.getClone().move(2667, 582));
        add(oneway.getClone().move(2897, 552));

		/*
		 * Cannons
		 */
        final Particle explosion = Particle.imageParticle(5, res.getAnimation("iceexp"));
        explosion.zIndex(200);
        explosion.setIntroSound(res.getSound("bum.wav"));
        explosion.sounds.useFalloff = true;
        explosion.sounds.maxVolume = .7f;
        explosion.sounds.power = 15;

        final Particle fireAnim = Particle.imageParticle(2, res.getAnimation("gunfire"));
        fireAnim.zIndex(200);
        fireAnim.offsetX = -fireAnim.halfWidth();
        fireAnim.offsetY = -13;

        final Bullet proj = new Bullet(0, 0, play);
        proj.setImage(res.getImage("iceproj.png"));
        proj.setMoveSpeed(2.5f);
        proj.setQuickCollision(true);
        proj.setHitbox(Hitbox.CIRCLE);
        proj.setImpact(explosion);
        proj.rotate(false);
        proj.setCloneEvent(cloned -> cloned.addEvent(() -> cloned.bounds.rotation += -10));

        final int cannonHeight = res.getImage("cannon.png").getHeight();
        for (int i = 0, reloadTime = 250, x = 3032, y = 480; i < 9; i++, y += cannonHeight + 15) {
            final SimpleWeapon w = new SimpleWeapon(x, y, proj, Direction.W, reloadTime);
            w.spawnOffset(0, 1);
            w.setImage(res.getImage("cannon.png"));
            w.zIndex(10);
            w.setFiringSound(res.getSound("cannonfire.wav"));
            w.setFiringAnimation(fireAnim);
            w.sounds.useFalloff = true;
            play.addObstacle(w);

            if (i % 2 == 0)
                add(w);
            else
                addAfter(w, reloadTime / 2);
        }

		/*
		 * Insects
		 */
        final PathDrone ins1 = new PathDrone(3725, 508);
        ins1.setImage(6, res.getAnimation("bug1"));
        ins1.setFacings(2);
        ins1.addEvent(ins1::face);
        ins1.setMoveSpeed(1);
        ins1.setHitbox(Hitbox.PIXEL);
        ins1.addEvent(Factory.hitMain(ins1, play, -1));
        ins1.appendPath(3725, 508);
        ins1.appendPath(3265, 508);

        final PathDrone ins2 = ins1.getClone();
        ins2.move(3725, 447);
        ins2.addEvent(ins2::face);
        ins2.setImage(6, res.getAnimation("bug2"));
        ins2.clearData();
        ins2.addEvent(Factory.hitMain(ins2, play, -1));
        ins2.appendPath(3725, 447);
        ins2.appendPath(3265, 447);

        final PathDrone ins3 = ins1.getClone();
        ins3.move(3819, 556);
        ins3.setImage(6, res.getAnimation("bug3"));
        ins3.setFacings(8);
        ins3.clearData();
        ins3.appendPath(3819, 556);
        ins3.appendPath(3819, 919);
        ins3.addEvent(Factory.hitMain(ins3, play, -1));
        ins3.addEvent(() -> {
            ins3.flipY = ins3.getFacing() == Direction.N;
        });

        final PathDrone ins4 = ins3.getClone();
        ins4.move(3767, 919);
        ins4.setImage(6, res.getAnimation("bug4"));
        ins4.clearData();
        ins4.appendPath(3767, 919);
        ins4.appendPath(3767, 556);
        ins4.addEvent(Factory.hitMain(ins4, play, -1));
        ins4.addEvent(() -> {
            ins4.flipY = ins4.getFacing() == Direction.N;
        });

        final PathDrone ins5 = ins4.getClone();
        ins5.move(4017, 919);
        ins5.clearData();
        ins5.appendPath(4017, 919);
        ins5.appendPath(4017, 759);
        ins5.addEvent(Factory.hitMain(ins5, play, -1));
        ins5.addEvent(() -> {
            ins5.flipY = ins5.getFacing() == Direction.N;
        });

        final PathDrone ins6 = ins3.getClone();
        ins6.move(4119, 759);
        ins6.clearData();
        ins6.appendPath(4119, 759);
        ins6.appendPath(4119, 919);
        ins6.addEvent(Factory.hitMain(ins6, play, -1));
        ins6.addEvent(() -> {
            ins6.flipY = ins6.getFacing() == Direction.N;
        });

        final PathDrone ins7 = ins4.getClone();
        ins7.move(4017, 716);
        ins7.clearData();
        ins7.appendPath(4017, 716);
        ins7.appendPath(4017, 556);
        ins7.addEvent(Factory.hitMain(ins7, play, -1));
        ins7.addEvent(() -> {
            ins7.flipY = ins7.getFacing() == Direction.N;
        });

        final PathDrone ins8 = ins3.getClone();
        ins8.move(4119, 556);
        ins8.clearData();
        ins8.appendPath(4119, 556);
        ins8.appendPath(4119, 716);
        ins8.addEvent(Factory.hitMain(ins8, play, -1));
        ins8.addEvent(() -> {
            ins8.flipY = ins8.getFacing() == Direction.N;
        });

        add(ins1);
        add(ins2);
        add(ins3);
        add(ins4);
        add(ins5);
        add(ins6);
        add(ins7);
        add(ins8);

		/*
		 * Solid Platform
		 */
        final SolidPlatform solp = new SolidPlatform(4973, 807, play);
        solp.setImage(res.getImage("movingp.png"));
        solp.setMoveSpeed(1);
        solp.freeze();
        solp.appendPath(5839, 807);
        solp.appendPath(5839, 159);
        solp.appendPath(4849, 159, 0, false, solp::freeze);
        add(solp);

		/*
		 * Bacterias
		 */
        final Circle c = new Circle(5279, 152, 50, 0);
        c.setMoveSpeed(.04f);

        final PathDrone backt = getBacteria(0, 0);
        backt.clearData();
        backt.addEvent(Factory.follow(c, backt, 0, 0));

        /* Bottom line */
        add(getBacteria(5249, 782));
        add(getBacteria(5489, 782));
        add(getBacteria(5519, 782));
        add(getBacteria(5619, 782));
        add(getBacteria(5689, 782));

        /* Vertical line */
        add(getBacteria(5859 + 12, 642));
        add(getBacteria(5829 - 12, 642));
        add(getBacteria(5829, 442));
        add(getBacteria(5829, 412));
        add(getBacteria(5829, 382));
        add(getBacteria(5859, 312));
        add(getBacteria(5859, 282));
        add(getBacteria(5859, 252));

        /* Top Line */
        add(getBacteria(new Waypoint(5589, 182), new Waypoint(5589, 122)));
        add(getBacteria(new Waypoint(5559, 122), new Waypoint(5559, 182)));
        add(getBacteria(4929, 152));
        add(getBacteria(4929, 182));
        add(getBacteria(4929, 182 + 30));
        add(getBacteria(4929, 152 - 30));
        add(backt);
        add(c);

		/*
		 * Goal
		 */
        addCrystal(4861, 131);
        runOnceWhen(play::win, () -> crystals == crystalsTaken);

		/*
		 * Finalizing
		 */
        play.setActionEvent((hitter) -> {
            if (hitter.isCloneOf(b) || hitter.isCloneOf(proj))
                play.touch(-1);
        });

        play.addTileEvent((tileType) -> {
            if (tileType == Tile.CUSTOM_5)
                solp.unfreeze();
        });

        /*
         * Bling Bling
         */
        blingbling = Particle.imageParticle(9, res.getAnimation("bling"));
        blingbling.zIndex(500);

        final Dimension screenSize = getEngine().getScreenSize();
        final Engine e = getEngine();
        add(() -> {
            if (++counter % 10 == 0)
                add(blingbling.getClone().move(
                        MathUtils.random(e.tx() - screenSize.width / 2, e.tx() + screenSize.width / 2),
                        MathUtils.random(e.ty() - screenSize.height / 2, e.ty() + screenSize.height / 2)));
        });
    }

    private void addCrystal(final float x, final float y) {
        crystals++;

        final Entity crystal = new Entity();
        crystal.setImage(6, res.getAnimation("crystal"));
        crystal.move(x, y);
        crystal.addEvent(() -> {
            if (crystal.collidesWith(play)) {
                discard(crystal);
                crystalsTaken++;
                res.getSound("collect3.wav").play();
            }
        });
        add(crystal);
    }

    private PathDrone getBacteria(final float x, final float y) {
        return getBacteria(new Waypoint(x, y, 0, false, null));
    }

    private PathDrone getBacteria(final Waypoint... waypoints) {
        class Int32 {
            int i;
        }
        final Int32 value = new Int32();
        value.i = 0;

        final PathDrone b = new PathDrone(waypoints[0].targetX, waypoints[0].targetY);
        b.setImage(ThreadLocalRandom.current().nextInt(3, 8), res.getAnimation("bacteria"));
        b.setMoveSpeed(1.2f);
        b.setHitbox(Hitbox.PIXEL);
        b.addEvent(Factory.hitMain(b, play, -1));
        b.appendPath(waypoints);
        b.addEvent(() -> {
            if (play.isActive() && --value.i < 0 && b.collidesWith(play)) {
                value.i = 60;
                res.getSound("zapp.wav").play();
            }
        });

        return b;
    }

    private Point2D.Float getDirection(final int dir) {
        final Point2D.Float point = new Point2D.Float();
        final int UP = 0, DOWN = 1, LEFT = 2, RIGHT = 3;
        final int minX = 2303;
        final int maxX = 2553 - 32;
        final int minY = 297;
        final int maxY = 667;

        final Random r = new Random();

        switch (dir) {
            case UP:
                point.x = r.nextInt(maxX - minX) + minX;
                point.y = minY;
                break;
            case DOWN:
                point.x = r.nextInt(maxX - minX) + minX;
                point.y = maxY;
                break;
            case LEFT:
                point.x = minX;
                point.y = r.nextInt(maxY - minY) + minY;
                break;
            case RIGHT:
                point.x = maxX;
                point.y = r.nextInt(maxY - minY) + minY;
                break;
        }

        return point;
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
        return "Diamond Cave";
    }
}
