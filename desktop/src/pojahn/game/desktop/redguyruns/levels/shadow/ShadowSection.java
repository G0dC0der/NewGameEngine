package pojahn.game.desktop.redguyruns.levels.shadow;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.MathUtils;
import pojahn.game.core.BaseLogic;
import pojahn.game.core.Entity;
import pojahn.game.core.PlayableEntity;
import pojahn.game.desktop.redguyruns.util.ResourceUtil;
import pojahn.game.entities.enemy.EvilDog;
import pojahn.game.entities.enemy.LaserDrone;
import pojahn.game.entities.image.RepeatingParallaxImage;
import pojahn.game.entities.image.StaticImage;
import pojahn.game.entities.movement.PathDrone;
import pojahn.game.entities.object.Button;
import pojahn.game.entities.object.Collectable;
import pojahn.game.entities.particle.Particle;
import pojahn.game.entities.platform.PushableObject;
import pojahn.game.entities.platform.SolidPlatform;
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
import pojahn.lang.Int32;
import pojahn.lang.PingPongFloat;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ShadowSection extends TileBasedLevel {

    private ResourceManager res;
    private PlayableEntity play;

    @Override
    public void load(final Serializable meta) throws Exception {
        res = new ResourceManager();
        res.loadContentFromDirectory(Gdx.files.internal("res/data"));
        res.loadContentFromDirectory(Gdx.files.internal("res/general"));
        res.loadContentFromDirectory(Gdx.files.internal("res/shadowside"));
        res.loadSound(Gdx.files.internal("res/spirit/fistslam.wav"));
        res.addAsset("black", Utils.toImage(Color.BLUE));
        getEngine().timeFont = res.getFont("sansserif32.fnt");

        Stream.of(res.getAnimation("main")).forEach(Image2D::createPixelData);
        res.getImage("enemy.png").createPixelData();

        Utils.playMusic(res.getMusic("music.ogg"), 16.13f, .7f);
    }

    @Override
    public TiledMap getTileMap() {
        return res.getTiledMap("map.tmx");
    }

    @Override
    public void build() {
        /*
         * Main Character
         */
        play = ResourceUtil.getGravityMan(res);
        play.zIndex(2);
        play.move(5 * 16, 167 * 16);
        add(play);

        /*
         * Backgrounds & Foreground
         */
        final Entity foreground = getWorldImage();
        foreground.zIndex(100);
        add(foreground);
        add(new EntityBuilder().image(res.getImage("background.png")).zIndex(-100).build(RepeatingParallaxImage.class));

        /*
         * Black overlay
         */
        final PingPongFloat pingPongFloat = new PingPongFloat(.09f, .10f, .001f);
        final StaticImage overlay = new StaticImage();
        overlay.setImage(res.getImage("black"));
        overlay.bounds.size.set(800, 600);
        overlay.zIndex(Integer.MAX_VALUE);
        overlay.addEvent(()-> overlay.tint.a = pingPongFloat.get());
        add(overlay);

        /*
         * Hearth
         */
        final Entity hearth = ResourceUtil.getHearth(res, play);
        hearth.move(615, 2297);
        add(hearth);

        /*
         * Puncher
         */
        addPuncher(688, 2323, 853);

        /*
         * Enemies
         */
//        addEnemy(9, 103, 5, 3);
        addEnemy(9, 103, 5, 3).reverse();
        addEnemy(22, 105, 3, 3);
        addEnemy(48, 106, 3, 3).reverse();
        addEnemy(52, 106, 3, 3);
        addEnemy(56, 106, 3, 3).reverse();
        addEnemy(82, 99, 4, 0);
        addEnemy(82, 104, 4, 0);

        /*
         * Moving head
         */
        final Particle headGone = Particle.fadingParticle(.009f);
        headGone.setImage(res.getAnimation("head")[0]);

        final int freezeFrames = 70;
        final SolidPlatform head = new SolidPlatform(1218 - 3, 1567 - 5, play);
        final Event moveEvent = () -> {
            head.sounds.play(res.getSound("headmove.wav"));
            add(headGone.getClone().move(head.x(), head.y()));
        };
        head.setImage(7, res.getAnimation("head"));
        head.sounds.useFalloff = true;
        head.appendPath(head.x(), head.y(), freezeFrames, true, ()-> head.sounds.play(res.getSound("headmove.wav")));
        head.appendPath(head.x() - (head.width() * 2), head.y(), freezeFrames, true, moveEvent);
        head.appendPath(head.x() - (head.width() * 4), head.y(), freezeFrames, true, moveEvent);
        head.appendPath(head.x() - (head.width() * 6), head.y(), freezeFrames, true, moveEvent);
        head.appendPath(head.x() - (head.width() * 8), head.y(), freezeFrames * 3, true, moveEvent);
        head.appendReversed();
        add(head);

        /*
         * The Hive
         */
        final Map<Integer, Integer> xMap = Map.of(
            0, 672,
            1, 696,
            2, 720);

        final List<LaserDrone> lasers = IntStream.range(0, 3)
            .mapToObj(i -> new LaserDrone(xMap.get(i), 948, 40, 10, 60, play))
            .peek(laserDrone -> {
                laserDrone.freeze();
                laserDrone.setFiringBeam(Factory.threeStageLaser(null, Image2D.animation(4, res.getAnimation("thinlaser")), null));
                laserDrone.setChargeBeam(Factory.threeStageLaser(null, Image2D.animation(4, res.getAnimation("thincharge")), null));
                laserDrone.setFiringSound(res.getSound("laserfire.wav"));
                laserDrone.setStartupSound(res.getSound("lasercharge.wav"));
                laserDrone.zIndex(200);
            })
            .collect(Collectors.toList());

        final Entity topHive = new Entity();
        topHive.setImage(res.getImage("hivetop.png"));
        topHive.zIndex(100);
        topHive.move(662, 880);

        final PathDrone bottomHive = new PathDrone(662, 931);
        bottomHive.setImage(res.getImage("hivebottom.png"));
        bottomHive.zIndex(100);
        bottomHive.freeze();
        bottomHive.setMoveSpeed(.3f);
        bottomHive.appendPath(662, 953, ()-> {
            lasers.get(0).unfreeze();
            runOnceAfter(()-> lasers.get(1).unfreeze(), 30);
            runOnceAfter(()-> lasers.get(2).unfreeze(), 60);
        });

        final Entity middleHive = new Entity();
        middleHive.setImage(res.getImage("hivemiddle.png"));
        middleHive.move(667, 932);

        add(topHive);
        add(bottomHive);
        add(middleHive);
        lasers.forEach(this::add);

        /*
         * Eye Drone
         */
        addEyeDrone(65, 65);
        addEyeDrone(78, 60).thrust = 170;

        /*
         * Pushable Platform
         */
        final PushableObject po = new PushableObject(850, 1103 - 3, play);
        po.setImage(res.getImage("movingp.png"));
        po.pushStrength = 150;
        add(po);

        /*
         * Big Platform
         */
        final SolidPlatform largeHelper = new SolidPlatform(1280, 1112, play);
        largeHelper.setImage(res.getImage("largehelper.png"));
        largeHelper.appendPath(largeHelper.x(), 922);
        largeHelper.freeze();
        add(largeHelper);

        /*
         * Button
         */
        final Button button = new Button(111, 984 - 1, Direction.S, play);
        button.setImage(res.getImage("button.png"));
        button.setPushSound(res.getSound("buttonpress.wav"));
        button.setPushEvent(()-> {
            largeHelper.unfreeze();
            bottomHive.unfreeze();
        });
        add(button);

        /*
         * Gem
         */
        final Collectable gem = new Collectable(151, 288, play);
        gem.setImage(res.getAnimation("gem"));
        gem.setCollectEvent(collector -> play.win());
        add(gem);

        /*
         * Finalizing
         */
        play.setActionEvent(hitter -> {
            if (lasers.stream().anyMatch(laser -> hitter == laser)) {
                play.touch(-1);
            }
        });
    }

    @Override
    public void dispose() {
        res.disposeAll();
    }

    @Override
    public String getLevelName() {
        return "Shadow Section";
    }

    @Override
    public Music getStageMusic() {
        return res.getMusic("music.ogg");
    }

    private EvilDog addEyeDrone(final float tileX, final float tileY) {
        final Int32 soundCounter = new Int32();
        final float freq = MathUtils.random(150, 300);
        final EvilDog eyeDrone = new EvilDog(tileX * getTileWidth(), tileY * getTileHeight(), -1, play);
        eyeDrone.addEvent(()-> {
            if (BaseLogic.rectanglesCollide(play.bounds.toRectangle(), getRectangle(0, 55, 86, 18))) {
                eyeDrone.unfreeze();
                if (++soundCounter.value % freq == 0) {
                    eyeDrone.sounds.play(res.getSound("monster.wav"));
                }
            } else {
                eyeDrone.freeze();
                eyeDrone.vx = eyeDrone.vy = 0;
            }
        });
        eyeDrone.addEvent(Factory.hitMain(eyeDrone, play, -1));
        eyeDrone.setImage(4, res.getImage("enemy.png"));
        eyeDrone.setHitbox(Hitbox.PIXEL);
        eyeDrone.zIndex(Integer.MAX_VALUE);
        eyeDrone.setFacings(2);
        eyeDrone.addEvent(eyeDrone::face);
        eyeDrone.thrust = 300;
        add(eyeDrone);

        return eyeDrone;
    }

    private PathDrone addEnemy(final int tileX, final int tileY, final int tilesX, final int tilesY) {
        final Animation<Image2D> enemyImage = Image2D.animation(4, res.getAnimation("ene"));
        enemyImage.pingPong(true);

        final int left = tileX * getTileWidth();
        final int right = (tileX + tilesX) * getTileWidth();
        final int top = tileY * getTileHeight();
        final int bottom = (tileY + tilesY) * getTileHeight();

        final PathDrone enemy = new PathDrone(left, top);
        enemy.appendPath();
        enemy.appendPath(right, top);
        enemy.appendPath(right, bottom);
        enemy.appendPath(left, bottom);
        enemy.setImage(enemyImage);
        enemy.setMoveSpeed(2);
        enemy.setHitbox(Hitbox.CIRCLE);
        enemy.addEvent(Factory.hitMain(enemy, play, -1));
        add(enemy);

        return enemy;
    }

    private void addPuncher(final float x, final float y, final float x2) {
        final SolidPlatform puncher = new SolidPlatform(x, y, play);
        puncher.setMoveSpeed(5);
        puncher.setImage(res.getImage("puncher.png"));
        puncher.sounds.useFalloff = true;
        puncher.freeze();
        puncher.appendPath(x2, y, 20, false, ()-> {
            puncher.sounds.play(res.getSound("fistslam.wav"));
            puncher.setMoveSpeed(2);
        });
        puncher.appendPath(x, y, 0, false, ()-> {
            puncher.freeze();
            puncher.setMoveSpeed(5);
            runOnceAfter(()-> runOnceWhenMainCollides(()-> {
                puncher.unfreeze();
                puncher.sounds.play(res.getSound("fistattack.wav"));
            }, 43, 145, 15, 2), 60);
        });

        runOnceAfter(()-> runOnceWhenMainCollides(()-> {
            puncher.unfreeze();
            puncher.sounds.play(res.getSound("fistattack.wav"));
        }, 43, 145, 15, 2), 60);

        add(puncher);
    }
}
