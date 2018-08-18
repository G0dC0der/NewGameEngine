package pojahn.game.desktop.redguyruns.levels.climb;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import pojahn.game.core.BaseLogic;
import pojahn.game.core.Entity;
import pojahn.game.core.PlayableEntity;
import pojahn.game.desktop.redguyruns.util.ResourceUtil;
import pojahn.game.entities.image.StaticImage;
import pojahn.game.entities.object.Bouncer;
import pojahn.game.entities.object.Collectable;
import pojahn.game.entities.platform.DestroyablePlatform;
import pojahn.game.entities.enemy.JumpingThwump;
import pojahn.game.entities.movement.PathDrone;
import pojahn.game.entities.platform.SolidPlatform;
import pojahn.game.entities.platform.SolidPlatform.FollowMode;
import pojahn.game.entities.platform.TilePlatform;
import pojahn.game.entities.object.Wind;
import pojahn.game.entities.main.GravityMan;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.Direction;
import pojahn.game.essentials.EntityBuilder;
import pojahn.game.essentials.Factory;
import pojahn.game.essentials.Hitbox;
import pojahn.game.essentials.Image2D;
import pojahn.game.essentials.ResourceManager;
import pojahn.game.essentials.Utils;
import pojahn.game.essentials.Vibrator;
import pojahn.game.essentials.Vitality;
import pojahn.game.essentials.stages.TileBasedLevel;
import pojahn.lang.OtherMath;

import java.io.Serializable;
import java.util.stream.Stream;

public class Climb extends TileBasedLevel {

    private ResourceManager resources;
    private PlayableEntity play;
    private Music music;

    @Override
    public void load(final Serializable meta) throws Exception {
        resources = new ResourceManager();
        resources.loadContentFromDirectory(Gdx.files.internal("res/data"));
        resources.loadContentFromDirectory(Gdx.files.internal("res/general"));
        resources.loadContentFromDirectory(Gdx.files.internal("res/climb"));

        getEngine().timeFont = resources.getFont("sansserif32.fnt");

        Stream.of(resources.getAnimation("main")).forEach(Image2D::createPixelData);
        Stream.of(resources.getAnimation("rockworm")).forEach(Image2D::createPixelData);
        Stream.of(resources.getAnimation("fan")).forEach(Image2D::createPixelData);
        Stream.of(resources.getImage("enemy.png")).forEach(Image2D::createPixelData);
        Stream.of(resources.getImage("spikes.png")).forEach(Image2D::createPixelData);

        music = resources.getMusic("music.ogg");
        Utils.playMusic(music, 0, .5f);

        final Pixmap pix = new Pixmap(1, 1, Format.RGBA8888);
        pix.setColor(Color.BLACK);
        pix.fill();
        final Image2D black = new Image2D(pix, false);
        pix.dispose();
        resources.addAsset("black", black);

//        getCheckpointHandler().appendCheckpoint(1765, 1370, 1688, 1372, 100, 100);
//        getCheckpointHandler().setReachEvent(() -> GFX.renderCheckpoint(resources, this));
    }

    @Override
    public TiledMap getTileMap() {
        return resources.getTiledMap("map.tmx");
    }

    @Override
    public void build() {
        /*
         * Main Character
         */
        play = ResourceUtil.getGravityMan(resources);
        play.move(250, 4033);
        add(play);

        /*
         * Background and foreground
         */
        add(getWorldImage());
        add(new EntityBuilder().image(resources.getImage("background.png")).zIndex(-10).build(StaticImage.class));

        /*
         * Weak Platforms
         */
        final DestroyablePlatform weak1 = new DestroyablePlatform(47 * 24, 168 * 24, play);
        weak1.setImage(resources.getImage("weak.png"));
        weak1.setDestroyImage(new Animation<Image2D>(1, resources.getImage("weakdest.png")));
        weak1.setBreakSound(resources.getSound("collapsing.wav"));

        add(weak1);
        add(weak1.getClone().move(55 * 24, 171 * 24));
        add(weak1.getClone().move(64 * 24, 171 * 24));
        add(weak1.getClone().move(70 * 24, 168 * 24));

        /*
         * Moving Platforms
         */
        final SolidPlatform solp1 = new SolidPlatform(1709, 3336, play);
        solp1.setImage(resources.getImage("movablep.png"));
        solp1.setMoveSpeed(1.5f);
        solp1.appendPath(1709, 3336, 60, false, null);
        solp1.appendPath(1709 - (1106 / 2) + solp1.width() + 50, 3336, 60, false, null);

        final SolidPlatform solp2 = new SolidPlatform(671, 3336, play);
        solp2.setImage(resources.getImage("movablep.png"));
        solp2.setMoveSpeed(1.5f);
        solp2.appendPath(671, 3336, 60, false, null);
        solp2.appendPath(671 + (1106 / 2) - solp2.width() - 50, 3336, 60, false, null);

        addAfter(solp1, 160);
        addAfter(solp2, 160);

        /*
         * Enemies
         */
        final Entity enemy = new Entity();
        enemy.setImage(resources.getImage("enemy.png"));
        enemy.setHitbox(Hitbox.PIXEL);
        enemy.zIndex(10);
        enemy.setCloneEvent(clonie -> clonie.addEvent(Factory.hitMain(clonie, play, -1)));

        add(enemy.getClone().move(1709 - (1106 / 2) + solp1.width() + 300, 3300));
        add(enemy.getClone().move(1709 - (1106 / 2) + solp1.width() + 350, 3300));
        add(enemy.getClone().move(1709 - (1106 / 2) + solp1.width(), 3300));

        add(enemy.getClone().move(671 + (1106 / 2) - solp2.width() - 300, 3300));
        add(enemy.getClone().move(671 + (1106 / 2) - solp2.width() - 350, 3300));
        add(enemy.getClone().move(671 + (1106 / 2) - solp2.width() + 10, 3300));

        /*
         * Jumpable Thwump
         */
        final JumpingThwump jumpingThwump = new JumpingThwump(495, 2865, play);
        final Vibrator vib = new Vibrator(this, jumpingThwump, Vibrator.VibDirection.CENTER, play);
        vib.setStrength(150);
        vib.setDuration(20);
        jumpingThwump.setImage(resources.getImage("fatidle.png"));
        jumpingThwump.setJumpImage(new Animation<Image2D>(1, resources.getImage("fatjump.png")));
        jumpingThwump.setSlamSound(resources.getSound("slam.wav"));
        jumpingThwump.setJumpSound(resources.getSound("fatjump.wav"));
        jumpingThwump.sounds.useFalloff = true;
        jumpingThwump.sounds.power = 40;
        jumpingThwump.setSlamEvent(vib::vibrate);

        final JumpingThwump jumpingThwump2 = jumpingThwump.getClone();
        final Vibrator vib2 = new Vibrator(this, jumpingThwump2, Vibrator.VibDirection.CENTER, play);
        vib2.setStrength(150);
        vib2.setDuration(20);
        jumpingThwump2.move(243, 2456);
        jumpingThwump2.setSlamEvent(vib2::vibrate);

        add(jumpingThwump);
        addAfter(jumpingThwump2, 40);

        /*
         * Rock Worm
         */
        final Animation<Image2D> rockwormImg = new Animation<>(6, resources.getAnimation("rockworm"));
        rockwormImg.pingPong(true);

        final Entity rockworm = new Entity();
        rockworm.setImage(rockwormImg);
        rockworm.setHitbox(Hitbox.PIXEL);
        rockworm.zIndex(-1);
        rockworm.setCloneEvent(clonie -> clonie.addEvent(Factory.hitMain(clonie, play, -1)));

        add(rockworm.getClone().move(547, 3993));
        add(rockworm.getClone().move(1934, 3849));
        add(rockworm.getClone().move(2039 - 5, 3392));

        /*
         * Cave Background and shadow
         */
        final Rectangle darkArea = new Rectangle(80 * 24, 650, 809, 785);

        final StaticImage darkLayer = new StaticImage();
        darkLayer.tint.a = 0;
        darkLayer.setImage(resources.getImage("black"));
        darkLayer.bounds.size.width = 800;
        darkLayer.bounds.size.height = 600;
        darkLayer.zIndex(Integer.MAX_VALUE);
        darkLayer.addEvent(() -> {
            final boolean dark = BaseLogic.rectanglesCollide(darkArea, play.bounds.toRectangle());
            final float value = dark ? .01f : -.01f;
            darkLayer.tint.a = OtherMath.keepInBounds(0, .7f, darkLayer.tint.a + value);
        });
        add(darkLayer);

        add(new EntityBuilder().move(1591, 565).zIndex(-1).image(resources.getImage("caveback.png")).build());

        /*
         * Crushers
         */
        final TilePlatform crush1 = new TilePlatform(90 * 24, 53 * 24, play);
        crush1.setImage(resources.getImage("crusher.png"));
        crush1.appendPath();
        crush1.appendPath(94 * 24, crush1.y());
        crush1.setMoveSpeed(1.5f);

        final TilePlatform crush2 = new TilePlatform(94 * 24, 46 * 24, play);
        crush2.setImage(resources.getImage("crusher2.png"));
        crush2.appendPath();
        crush2.appendPath(crush2.x(), 51 * 24);
        crush2.setMoveSpeed(1.2f);
        crush2.setFollowMode(FollowMode.STRICT);

        final TilePlatform crush3 = new TilePlatform(90 * 24, 51 * 24, play);
        crush3.setImage(resources.getImage("crusher2.png"));
        crush3.appendPath();
        crush3.appendPath(crush3.x(), 46 * 24);
        crush3.setMoveSpeed(1.2f);
        crush3.setFollowMode(FollowMode.STRICT);

        add(crush1);
        add(crush2);
        add(crush3);

        /*
         * Spikes
         */
        final Entity spikes = new Entity();
        spikes.move(90 * 24, 31 * 24);
        spikes.setHitbox(Hitbox.PIXEL);
        spikes.setImage(resources.getImage("spikes.png"));
        spikes.addEvent(Factory.hitMain(spikes, play, -1));
        add(spikes);

        /*
         * Fans
         */
        final Entity fan1 = new Entity();
        fan1.setImage(1, resources.getAnimation("fan"));
        fan1.flipY = true;
        fan1.move(74, 2116);
        fan1.addEvent(Factory.hitMain(fan1, play, -1));
        fan1.setHitbox(Hitbox.PIXEL);

        final Entity fan2 = fan1.getClone().move(765, 1399);
        fan2.addEvent(Factory.hitMain(fan2, play, -1));
        fan2.flipY = false;

        add(fan1);
        add(fan2);

        /*
         * Winds
         */
        final Wind wind1 = new Wind(75, 1832, 120, 600, Direction.N, (GravityMan) play);
        wind1.setImage(2, resources.getAnimation("wind"));
        wind1.rotate(180);

        final Wind wind2 = new Wind(758, 1419, 7, 250, Direction.S, (GravityMan) play);
        wind2.setImage(2, resources.getAnimation("wind"));

        add(wind1);
        add(wind2);

        /*
         * Wind Holder
         */
        add(new EntityBuilder().image(resources.getImage("stolpe.png")).move(625, 1375).zIndex(-5).build());

        /*
         * Bouncer
         */
        final Rectangle unfreezeArea = new Rectangle(1353, 538, 86, 77);

        final PathDrone bouncerMove = new PathDrone(1365, 553);
        bouncerMove.appendPath();
        bouncerMove.appendPath(484, bouncerMove.y());
        bouncerMove.setMoveSpeed(1.2f);
        bouncerMove.freeze();
        runOnceWhen(bouncerMove::unfreeze, () -> BaseLogic.rectanglesCollide(play.bounds.toRectangle(), unfreezeArea));

        final Bouncer bouncer = new Bouncer(0, 0, (GravityMan) play);
        bouncer.setPower(320);
        bouncer.setBouncingDirection(Direction.N);
        bouncer.setBounceSound(resources.getSound("bumper.wav"));
        bouncer.addEvent(Factory.follow(bouncerMove, bouncer));
        bouncer.setImage(resources.getImage("bouncer.png"));

        add(bouncer);
        add(bouncerMove);

        /*
         * Gem
         */
        final Collectable gem = new Collectable(445, 535, play);
        gem.setImage(4, resources.getAnimation("gem"));
        gem.setCollectSound(resources.getSound("collect1.wav"));
        gem.setCollectEvent(collector -> play.setState(Vitality.COMPLETED));
        add(gem);
    }

    @Override
    public String getLevelName() {
        return "Climb";
    }

    @Override
    public Music getStageMusic() {
        return music;
    }

    @Override
    public void dispose() {
        resources.disposeAll();
    }
}
