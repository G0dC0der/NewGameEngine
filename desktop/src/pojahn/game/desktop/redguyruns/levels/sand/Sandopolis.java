package pojahn.game.desktop.redguyruns.levels.sand;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.BaseLogic;
import pojahn.game.core.Entity;
import pojahn.game.core.PlayableEntity;
import pojahn.game.desktop.redguyruns.util.ResourceUtil;
import pojahn.game.entities.TmxEntity;
import pojahn.game.entities.image.RepeatingParallaxImage;
import pojahn.game.entities.movement.PathDrone;
import pojahn.game.entities.particle.Particle;
import pojahn.game.entities.platform.SolidPlatform;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.EntityBuilder;
import pojahn.game.essentials.Factory;
import pojahn.game.essentials.Hitbox;
import pojahn.game.essentials.Image2D;
import pojahn.game.essentials.ResourceManager;
import pojahn.game.essentials.Utils;
import pojahn.game.essentials.stages.TileBasedLevel;

import java.io.Serializable;
import java.util.stream.Stream;

import static pojahn.game.essentials.Factory.hitMain;

public class Sandopolis extends TileBasedLevel {

    private ResourceManager res;
    private PlayableEntity play;
    private UglySun uglySun;

    @Override
    public void load(final Serializable meta) throws Exception {
        res = new ResourceManager();
        res.loadContentFromDirectory(Gdx.files.internal("res/data"));
        res.loadContentFromDirectory(Gdx.files.internal("res/general"));
        res.loadContentFromDirectory(Gdx.files.internal("res/sandopolis"));
        getEngine().timeFont = res.getFont("sansserif32.fnt");
        getEngine().timeColor = Color.BLACK;

        Stream.of(res.getAnimation("main")).forEach(Image2D::createPixelData);
        Stream.of(res.getAnimation("flamer")).forEach(Image2D::createPixelData);
        Stream.of(res.getAnimation("flamer2")).forEach(Image2D::createPixelData);

        Utils.playMusic(res.getMusic("music.ogg"), 6, .35f);
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
        play.move(48 * getTileWidth(), 18 * getTileHeight() - play.height() - 1);
        add(play);

        /*
         * Backgrounds & Foreground
         */
        final Entity foreground = getWorldImage();
        foreground.zIndex(100);
        add(foreground);
        add(new EntityBuilder().image(res.getImage("background.png")).zIndex(-100).move(0, 0).build(RepeatingParallaxImage.class));

        final TmxEntity bgStuff1 = new TmxEntity(res.getTiledMap("map_bg1.tmx"));
        bgStuff1.zIndex(-99);
        add(bgStuff1);

        final TmxEntity bgStuff2 = new TmxEntity(res.getTiledMap("map_bg2.tmx"));
        bgStuff2.zIndex(-98);
        add(bgStuff2);

        /*
         * Flamers
         */
        add(getFlamer(5856, 1311, 6405, 1311));
        add(getFlamer(6405, 1311, 5856, 1311));

        add(getFlamer2(6529, 1214, 6679, 1214));

        add(getFlamer(6911, 1023, 6990, 1023));
        add(getFlamer(6940, 1023, 7019, 1023));
        add(getFlamer(6969, 1023, 7048, 1023));
        add(getFlamer(6998, 1023, 7077, 1023));

        add(getFlamer2(7289, 928, 7352, 928));
        add(getFlamer(7483, 926, 7558, 926));
        add(getFlamer2(7680, 928, 7740, 928));

        /*
         * Rolling stones
         */
        add(new EntityBuilder().move(2361, 816).image(res.getImage("pipe.png")).zIndex(50).build());
        add(getStone());
        addAfter(getStone(), 320);
        addAfter(getBigStone(), 200);

        /*
         * Big Crusher
         */
        final float moveSpeed = 1.2f;
        final float offset = 20;
        final int freezeFrames = 45;

        final SolidPlatform crusher = new SolidPlatform(5328, 1332, play);
        crusher.setImage(res.getImage("slammer.png"));
        crusher.setMoveSpeed(moveSpeed);
        crusher.appendPath(5328, 1332, freezeFrames, false, null);
        crusher.appendPath(5328, 1282 - offset, freezeFrames, false, null);

        final SolidPlatform crusher2 = new SolidPlatform(5232, 1282 - offset, play);
        crusher2.setImage(res.getImage("slammer.png"));
        crusher2.setMoveSpeed(moveSpeed);
        crusher2.appendPath(5232, 1282 - offset, freezeFrames, false, null);
        crusher2.appendPath(5232, 1332, freezeFrames, false, null);

        final SolidPlatform crusher3 = new SolidPlatform(5424, 1282 - offset, play);
        crusher3.setImage(res.getImage("slammer.png"));
        crusher3.setMoveSpeed(moveSpeed);
        crusher3.appendPath(5424, 1282 - offset, freezeFrames, false, null);
        crusher3.appendPath(5424, 1332, freezeFrames, false, null);

        add(crusher);
        add(crusher2);
        add(crusher3);

        /*
         * Ugly sun
         */
        uglySun = new UglySun(play);
        uglySun.setHappyImage(new Animation<Image2D>(1, res.getImage("sunhappy.png")));
        uglySun.setAngryImage(new Animation<Image2D>(1, res.getImage("sunangry.png")));
        uglySun.setPissedImage(new Animation<Image2D>(1, res.getImage("sunpissed.png")));
        uglySun.move(play.x(), play.y() - (getEngine().getScreenSize().height / 2));
        uglySun.setMoveSpeed(2.2f);
        uglySun.setRes(res);

        final Entity eye1 = new EntityBuilder().image(res.getImage("eye.png")).build();
        eye1.addEvent(Factory.follow(uglySun, eye1, 24, 30));
        eye1.addEvent(() -> BaseLogic.rotateTowards(eye1, play, 0.11f));

        final Entity eye2 = new EntityBuilder().image(res.getImage("eye.png")).build();
        eye2.addEvent(Factory.follow(uglySun, eye2, 48, 30));
        eye2.addEvent(() -> BaseLogic.rotateTowards(eye2, play, 0.11f));

        add(eye1);
        add(eye2);
        add(uglySun);

        /*
         * Sign
         */
        add(new EntityBuilder().move(4592, 1696).image(res.getImage("sign.png")).zIndex(-10).build());

        /*
         * Hearth
         */
        final Entity health = new EntityBuilder().image(res.getAnimation("health")).move(7933, 1705).build();
        health.addEvent(() -> {
            if (health.collidesWith(play)) {
                play.touch(1);
                res.getSound("health.wav").play();
                discard(health);
            }
        });
        add(health);

        /*
         * Lightnings
         */
        addBoltItem(5366, 1516);
        addBoltItem(8102, 844);
        addBoltItem(2033, 940);
    }

    private void addBoltItem(final float x, final float y) {
        final Entity item = new Entity();
        item.move(x, y);
        item.zIndex(-10);
        item.setImage(4, res.getAnimation("item"));
        item.addEvent(() -> {
            if (item.collidesWith(play)) {
                discard(item);
                uglySun.hit();

                final Particle bolt = Particle.imageParticle(4, res.getAnimation("bolt"));
                bolt.setIntroSound(res.getSound("boltstrike.wav"));
                bolt.move(uglySun.centerX(), uglySun.centerY() - uglySun.height() - 50);
                bolt.zIndex(1000);
                add(bolt);
            }
        });
        add(item);
    }

    private RollingStone getStone() {
        final RollingStone rs = new RollingStone();
        rs.wp1 = new Vector2(2384, 814);
        rs.wp2 = new Vector2(2384, 917);
        rs.setImage(res.getImage("smallspike.png"));
        rs.setMoveSpeed(6);
        rs.maxX = 4192 - 5;
        rs.setHitbox(Hitbox.CIRCLE);
        rs.addEvent(hitMain(rs, play, -1));

        return rs;
    }

    private RollingStone getBigStone() {
        final RollingStone rs = new RollingStone();
        rs.wp1 = new Vector2(2368, 795);
        rs.wp2 = new Vector2(2368, 886);
        rs.setImage(res.getImage("spikeball.png"));
        rs.setMoveSpeed(4);
        rs.rotationSpeed = 6;
        rs.maxX = 4192 - 5;
        rs.setHitbox(Hitbox.CIRCLE);
        rs.addEvent(hitMain(rs, play, -1));

        return rs;
    }

    private PathDrone getFlamer(final float x1, final float y1, final float x2, final float y2) {
        final PathDrone flamer = new PathDrone(x1, y1);
        flamer.setImage(7, res.getAnimation("flamer"));
        flamer.appendPath();
        flamer.appendPath(x2, y2);
        flamer.setFacings(2);
        flamer.addEvent(flamer::face);
        flamer.setHitbox(Hitbox.PIXEL);
        flamer.setMoveSpeed(.7f);
        flamer.addEvent(hitMain(flamer, play, -1));

        return flamer;
    }

    private PathDrone getFlamer2(final float x1, final float y1, final float x2, final float y2) {
        final PathDrone flamer = new PathDrone(x1, y1);
        flamer.setImage(4, res.getAnimation("flamer2"));
        flamer.appendPath();
        flamer.appendPath(x2, y2);
        flamer.setFacings(2);
        flamer.setHitbox(Hitbox.PIXEL);
        flamer.addEvent(flamer::face);
        flamer.setMoveSpeed(2);
        flamer.addEvent(hitMain(flamer, play, -1));

        return flamer;
    }

    @Override
    public void dispose() {
        res.disposeAll();
    }

    @Override
    public String getLevelName() {
        return "Sandopolis";
    }

    @Override
    public Music getStageMusic() {
        return res.getMusic("music.ogg");
    }
}
