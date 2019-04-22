package pojahn.game.desktop.redguyruns.levels.phanto;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.BaseLogic;
import pojahn.game.core.Entity;
import pojahn.game.desktop.redguyruns.util.GFX;
import pojahn.game.desktop.redguyruns.util.ResourceUtil;
import pojahn.game.entities.enemy.EvilDog;
import pojahn.game.entities.image.RepeatingParallaxImage;
import pojahn.game.entities.main.GravityMan;
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
import pojahn.game.essentials.Vibrator;
import pojahn.game.essentials.stages.TileBasedLevel;
import pojahn.lang.Bool;
import pojahn.lang.Int32;

import java.io.Serializable;
import java.util.stream.Stream;

public class GuardedKey extends TileBasedLevel {

    private final Rectangle deathRectangle = new Rectangle(1024, 3454, 64, 32);
    private ResourceManager res;
    private GravityMan play;
    private Music music;

    @Override
    public void load(final Serializable meta) throws Exception {
        res = new ResourceManager();
        res.loadContentFromDirectory(Gdx.files.internal("res/data"));
        res.loadContentFromDirectory(Gdx.files.internal("res/general"));
        res.loadContentFromDirectory(Gdx.files.internal("res/phanto"));
        getEngine().timeFont = res.getFont("sansserif32.fnt");

        Stream.of(res.getAnimation("main")).forEach(Image2D::createPixelData);
        Stream.of(res.getAnimation("shyguy")).forEach(Image2D::createPixelData);
        res.getImage("phanto.png").createPixelData();

        music = res.getMusic("music.ogg");
        Utils.playMusic(music, 0, .6f);

        getCheckpointHandler().setReachEvent(() -> GFX.renderCheckpoint(res, this));
        getCheckpointHandler().appendCheckpoint(new Vector2(1222, 480 - 21), new Rectangle(1195, 356, 200, 200));
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
        play.move(40 * 32, 133 * 32 + (32 - play.height() - 1));
        add(play);

        /*
         * Backgrounds & Foreground
         */
        final Entity foreground = getWorldImage();
        foreground.zIndex(100);
        add(foreground);
        add(new EntityBuilder().image(res.getImage("background.png")).zIndex(-100).build(RepeatingParallaxImage.class));

        /*
         * Zapper
         */
        rectangleZapper(15, 123, 8, 6).setMoveSpeed(4);
        rectangleZapper(24, 123, 5, 6);
        final PathDrone zapper = rectangleZapper(24, 123, 5, 6);
        zapper.reverse();
        zapper.setMoveSpeed(1);
        rectangleZapper(58, 46, 6, 6);
        rectangleZapper(60, 15, 5, 5);
        rectangleZapper(70, 15, 5, 5).reverse();
        rectangleZapper(24, 13, 5, 3).setMoveSpeed(3);

        /*
         * Sharp Blocks
         */
        sharpBlock(32, 123);
        sharpBlock(32, 122);
        sharpBlock(32, 121);
        sharpBlock(32, 120);
        sharpBlock(32, 119);
        for (int i = 0; i < 8; i++) {
            if (i % 2 == 0)
                sharpBlock(43 + i, 126);
            else
                sharpBlock(43 + i, 125);
        }
        sharpBlock(76, 37);
        sharpBlock(76, 38);
        sharpBlock(76, 39);
        sharpBlock(76, 40);
        sharpBlock(76, 41);
        sharpBlock(76, 42);
        sharpBlock(76, 43);

        sharpBlock(66, 43);
        sharpBlock(67, 43);
        sharpBlock(68, 43);
        sharpBlock(69, 43);
        sharpBlock(70, 43);
        sharpBlock(71, 43);

        for (int y = 15; y < 33; y++) {
            sharpBlock(57, y);
        }

        for (int x = 68; x < 74; x++) {
            sharpBlock(x, 29);
        }

        for (int y = 20; y < 29; y++) {
            if (y % 2 == 0) {
                sharpBlock(70, y);
                sharpBlock(70 + 1, y);
            }
        }

        /*
         * Platform
         */
        final SolidPlatform platform = new SolidPlatform(992, 4123 + 20, play);
        platform.setImage(res.getImage("platform.png"));
        platform.setMoveSpeed(.7f);
        platform.appendPath(platform.x(), platform.y(), 60, false, null);
        platform.appendPath(platform.x() + (32 * 2), platform.y(), 60, false, null);
        add(platform);

        /*
         * Lava
         */
        final int lavaX = 1024;
        for (int i = 0; i < 4; i++) {
            final Entity lava = new Entity();
            lava.move(lavaX + (16 * i), 3424 + 6);
            lava.setImage(7, res.getAnimation("lava"));
            lava.getImage().pingPong(true);
            lava.addEvent(Factory.hitMain(lava, play, -1));
            lava.zIndex(3);
            add(lava);
        }
        add(new EntityBuilder().image(res.getImage("orange.png")).zIndex(2).move(1024, 3439).build());

        /*
         * Vase
         */
        interval(() -> addShyGuy(14 * 32, 106 * 32, true), 450);
        interval(() -> addShyGuy(39 * 32, 99 * 32, false), 400);
        interval(() -> addShyGuy(14 * 32, 92 * 32, true), 350);

        /*
         * Big Roller
         */
        final BigRoller roller = new BigRoller(1048, 2080);
        roller.setImage(res.getImage("roller.png"));
        roller.setHitbox(Hitbox.CIRCLE);
        roller.sounds.useFalloff = true;
        roller.sounds.power = 90;
        roller.addEvent(Factory.hitMain(roller, play, -1));
        roller.setCrashSound(res.getSound("crash.wav"));
        roller.setRollSound(res.getSound("slam.wav"));

        final Vibrator smallVib = new Vibrator(this, roller, Vibrator.VibDirection.CENTER, play);
        smallVib.setDuration(10);
        smallVib.setStrength(1);
        smallVib.setRadius(700);
        smallVib.setStaticStrength(true);
        roller.setRollingVib(smallVib);

        final Vibrator bigVib = new Vibrator(this, roller, Vibrator.VibDirection.CENTER, play);
        bigVib.setDuration(40);
        bigVib.setStrength(2.5f);
        bigVib.setRadius(700);
        bigVib.setStaticStrength(true);
        roller.setCrashingVib(bigVib);

        add(roller);

        /*
         * Spikes
         */
        final int width = res.getAnimation("spike")[0].getWidth();
        final int startX = 1827;
        final int startY = 2057;
        for (int i = 0; i < 90; i += 2) {
            final Entity spike = new Entity();
            spike.move(startX + (width * i), startY);
            spike.setImage(4, res.getAnimation("spike"));
            spike.getImage().pingPong(true);
            spike.getImage().setIndex(i % res.getAnimation("spike").length);
            add(spike);
        }
        final Rectangle rec = new Rectangle(startX, startY, 1000, 10);
        runWhile(() -> play.touch(-1), () -> BaseLogic.rectanglesCollide(rec, play.bounds.toRectangle()));

        /*
         * More Platforms
         */
        final SolidPlatform s1 = new SolidPlatform(58 * 32, 63 * 32, play);
        s1.setMoveSpeed(1f);
        s1.setImage(res.getImage("platform.png"));
        s1.appendPath(s1.x(), s1.y(), 60, false, null);
        s1.appendPath(65 * 32, s1.y(), 60, false, null);
        add(s1);

        final SolidPlatform s2 = new SolidPlatform(75 * 32, 63 * 32, play);
        s2.setMoveSpeed(1f);
        s2.setImage(res.getImage("platform.png"));
        s2.appendPath(s2.x(), s2.y(), 60, false, null);
        s2.appendPath(68 * 32, s2.y(), 60, false, null);
        add(s2);

        /*
         * Phanto
         */
        final EvilDog phanto1 = addPhanto(22, 11);
        final EvilDog phanto2 = addPhanto(26, 10);
        final EvilDog phanto3 = addPhanto(30, 11);

        /*
         * Key & Door
         */
        final Bool keyTaken = new Bool();

        final Entity key = new Entity();
        key.move(840, 430);
        key.setImage(res.getImage("key.png"));
        key.ifCollides(play).then(() -> {
            res.getSound("collect3.wav").play();
            discard(key);
            keyTaken.value = true;
            unfreeze(phanto1);
        });

        final Entity door = new Entity();
        door.move(1414, 4226);
        door.setImage(res.getImage("door.png"));
        door.addEvent(() -> {
            if (keyTaken.value && door.collidesWith(play)) {
                phanto1.freeze();
                phanto2.freeze();
                phanto3.freeze();
                play.win();
            }
        });

        add(key);
        add(door);
    }

    void unfreeze(final EvilDog phanto) {
        temp(Factory.spazz(phanto, 2, 2), 120);
        runOnceAfter(() -> {
            phanto.offsetX = phanto.offsetY = 0;
            phanto.unfreeze();
        }, 121);
    }

    ShyGuy getShyGuy(final float x, final float y, final boolean flip) {
        final ShyGuy shyGuy = new ShyGuy();
        shyGuy.move(x, y);
        shyGuy.setImage(6, res.getAnimation("shyguy"));
        shyGuy.setMoveSpeed(1.5f);
        shyGuy.setFacings(2);
        shyGuy.addEvent(shyGuy::face);
        shyGuy.setHitbox(Hitbox.PIXEL);
        shyGuy.addEvent(Factory.hitMain(shyGuy, play, -1));
        shyGuy.addEvent(() -> {
            if (BaseLogic.rectanglesCollide(shyGuy.bounds.toRectangle(), deathRectangle))
                discard(shyGuy);
        });
        if (!flip) {
            shyGuy.flipMovement();
        }

        return shyGuy;
    }

    void addShyGuy(final float x, final float y, final boolean flip) {
        final PathDrone entr = new PathDrone(x, y);

        final Animation<Image2D> entrImage = new Animation<Image2D>(5, res.getAnimation("shyspawn"));
        entrImage.stop(true);
        entrImage.setLoop(false);
        entrImage.addEvent(() -> {
            discard(entr);
            add(getShyGuy(x, y - 32 - 2, flip));
        }, entrImage.getArray().length - 1);

        entr.setImage(entrImage);
        entr.move(x, y);
        entr.setMoveSpeed(1.5f);
        entr.appendPath(x, y - 32 - 2, 0, false, () -> entrImage.stop(false));
        entr.flipX = !flip;
        add(entr);
    }

    void sharpBlock(final float x, final float y) {
        final Entity sb = new Entity();
        sb.move(x * 32 + 4, y * 32 + 4);
        sb.setImage(7, res.getAnimation("spikeball"));
        sb.addEvent(Factory.hitMain(sb, play, -1));
        sb.tint.a = .85f;
        sb.getImage().setIndex(MathUtils.random(0, sb.getImage().getArray().length - 1));
        add(sb);
    }

    EvilDog addPhanto(final float x, final float y) {
        final EvilDog phanto = new EvilDog(x * 32 - 2.5f, y * 32, -1, play);
        phanto.setImage(res.getImage("phanto.png"));
        phanto.setHitbox(Hitbox.PIXEL);
        phanto.addEvent(() -> {
            if (play.isAlive() && play.collidesWith(phanto)) {
                play.touch(-1);
            }
        });
        phanto.zIndex(10000);
        phanto.thrust = 200;
        phanto.drag = .2f;
        phanto.freeze();

        add(phanto);
        return phanto;
    }

    PathDrone rectangleZapper(final int x, final int y, final int tilesX, final int tilesY) {
        final int padding = 4;
        final int left = x * 32 + padding;
        final int right = (x + tilesX) * 32 - 32;
        final int top = y * 32 + padding;
        final int bottom = (y + tilesY) * 32 - 32;

        final PathDrone z = new PathDrone(left, top);
        z.appendPath();
        z.appendPath(right, top);
        z.appendPath(right, bottom);
        z.appendPath(left, bottom);
        z.setImage(3, res.getImage("zapper.png"));
        z.setHitbox(Hitbox.CIRCLE);
        z.addEvent(Factory.hitMain(z, play, -1));
        final Int32 c = new Int32();
        z.addEvent(() -> {
            if (++c.value % 6 == 0) {
                final Particle part = Particle.shrinkingParticle(.045f);
                part.setImage(res.getImage("spark.png"));
                part.move(MathUtils.random(z.x(), z.x() + z.width()),
                        MathUtils.random(z.y(), z.y() + z.height()));

                add(part);
            }
        });

        add(z);
        return z;
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
        return "Guarded Key";
    }
}
