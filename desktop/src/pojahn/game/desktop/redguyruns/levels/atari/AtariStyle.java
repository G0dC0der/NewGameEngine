package pojahn.game.desktop.redguyruns.levels.atari;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import pojahn.game.core.Entity;
import pojahn.game.core.PlayableEntity;
import pojahn.game.desktop.redguyruns.util.ResourceUtil;
import pojahn.game.entities.BigImage;
import pojahn.game.entities.BigImage.RenderStrategy;
import pojahn.game.entities.Bullet;
import pojahn.game.entities.DestroyablePlatform;
import pojahn.game.entities.Projectile;
import pojahn.game.entities.SimpleWeapon;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.Direction;
import pojahn.game.essentials.EntityBuilder;
import pojahn.game.essentials.Factory;
import pojahn.game.essentials.Image2D;
import pojahn.game.essentials.ResourceManager;
import pojahn.game.essentials.Vitality;
import pojahn.game.essentials.stages.PixelBasedLevel;

import java.io.Serializable;
import java.util.stream.Stream;

public class AtariStyle extends PixelBasedLevel {

    private ResourceManager resources;
    private Music music;
    private PlayableEntity play;
    private Projectile p;
    private boolean taken1, taken2, taken3;

    @Override
    public void init(final Serializable meta) throws Exception {
        resources = new ResourceManager();
        resources.loadContentFromDirectory(Gdx.files.internal("res/data"));
        resources.loadContentFromDirectory(Gdx.files.internal("res/general"));
        resources.loadContentFromDirectory(Gdx.files.internal("res/atari"));

        getEngine().timeFont = resources.getFont("sansserif32.fnt");
        Stream.of(resources.getAnimation("main")).forEach(Image2D::createPixelData);
        createMap(resources.getPixmap("pixmap.png"));
        resources.remove("pixmap.png");

        music = resources.getMusic("music.ogg");
        music.setLooping(true);
        music.setVolume(.7f);
        music.play();
    }

    @Override
    public void build() {
        taken1 = taken2 = taken3 = false;
        play = ResourceUtil.getGravityMan(resources);
        play.move(2580, 1358);
        play.nudge(-1, 0);
        add(play);

        /*
         * Background and foreground
         */
        add(new EntityBuilder().image(resources.getImage("foreground.png")).zIndex(0).build());
        add(new EntityBuilder().image(resources.getImage("background.png")).zIndex(-5).build(BigImage.class, RenderStrategy.PARALLAX_REPEAT));

		/*
         * Lava
		 */
        addLava(73, 44, 4, 2);
        addLava(69, 44, 1, 2);
        addLava(72, 37, 1, 4);
        addLava(73, 40, 4, 1);

        addLava(69, 30, 1, 5);
        addLava(58, 30, 1, 6);
        addLava(54, 26, 3, 1);
        addLava(54, 39, 3, 1);
        addLava(54, 27, 1, 12);

        addLava(27, 52, 40, 2);

        addLava(32, 29, 2, 1);
        addLava(37, 29, 3, 1);
        addLava(43, 29, 2, 1);

        addLava(36, 10, 1, 17);

        addLava(49, 15.5f, 4, 2);
        addLava(49, 10, 4, 1.5f);

        addLava(57, 10, 2, 1);
        addLava(57, 16, 2, 1);

        addLava(61, 10, 1, 7);

		/*
         * Weak
		 */
        addWeak(63, 45);
        addWeak(64, 45);
        addWeak(65, 45);
        addWeak(66, 45);
        addWeak(64, 50);
        addWeak(60, 50);
        addWeak(56, 46);
        addWeak(56, 47);
        addWeak(56, 48);
        addWeak(56, 49);
        addWeak(56, 50);
        addWeak(56, 51);
        addWeak(52, 49);
        addWeak(45, 51);
        addWeak(34, 50);
        addWeak(40, 49);
        addWeak(28, 50);
        addWeak(27, 50);

		/*
		 * Turrets
		 */
        p = new Bullet(0, 0, play);
        p.setImage(2, resources.getAnimation("lethal"));
        p.bounds.size.width = p.bounds.size.height = 20;
        p.setMoveSpeed(7);

        add(getTurret(73, 29, Direction.W));
        add(getTurret(27, 32, Direction.S));
        addAfter(getTurret(29, 32, Direction.S), 125);
        add(getTurret(7, 20, Direction.S));
        add(getTurret(20, 21, Direction.W));
        add(getTurret(19, 34, Direction.N));
        add(getTurret(6, 33, Direction.E));
        add(getTurret(37, 9, Direction.S));
        add(getTurret(38, 9, Direction.S));
        add(getTurret(39, 9, Direction.S));
        add(getTurret(40, 9, Direction.S));
        add(getTurret(41, 9, Direction.S));
        add(getTurret(42, 9, Direction.S));
        add(getTurret(43, 9, Direction.S));

		/*
		 * Door & Keys
		 */
        final Entity key1 = new Entity();
        key1.setImage(resources.getImage("key.png"));
        key1.move(7 * 30, 27 * 30);
        key1.addEvent(() -> {
            if (key1.collidesWith(play)) {
                discard(key1);
                taken1 = true;
            }
        });

        final Entity key2 = new Entity();
        key2.setImage(resources.getImage("key.png"));
        key2.move(19 * 30, 27 * 30);
        key2.addEvent(() -> {
            if (key2.collidesWith(play)) {
                discard(key2);
                taken2 = true;
            }
        });

        final Entity key3 = new Entity();
        key3.setImage(resources.getImage("key.png"));
        key3.move(13 * 30, 21 * 30);
        key3.addEvent(() -> {
            if (key3.collidesWith(play)) {
                discard(key3);
                taken3 = true;
            }
        });

        final Entity theDoor = new Entity();
        theDoor.move(20 * 30, 27 * 30);
        theDoor.setImage(resources.getImage("door.png"));
        play.addObstacle(theDoor);
        theDoor.addEvent(() -> {
            if (taken1 && taken2 && taken3) {
                play.removeObstacle(theDoor);
                discard(theDoor);
            }
        });

        add(key1);
        add(key2);
        add(key3);
        add(theDoor);

		/*
		 * Goal
		 */
        final Entity g = new Entity();
        g.move(59 * 30, 13 * 30);
        g.setImage(resources.getImage("goal.png"));
        g.addEvent(() -> {
            if (g.collidesWith(play)) {
                discard(g);
                play.setState(Vitality.COMPLETED);
            }
        });
        add(g);

		/*
		 * Finalizing
		 */
        play.setActionEvent((hitter) -> {
            if (hitter.isCloneOf(p))
                play.touch(-1);
        });
    }

    @Override
    public void dispose() {
        resources.disposeAll();
    }

    @Override
    public Music getStageMusic() {
        return music;
    }

    @Override
    public String getLevelName() {
        return "Atari Style";
    }

    SimpleWeapon getTurret(final int x, final int y, final Direction dir) {
        final SimpleWeapon sw = new SimpleWeapon(x * 30, y * 30, p, dir, 250);
        sw.setImage(5, resources.getAnimation("turret"));
        sw.setFiringSound(resources.getSound("gunfire.wav"));
        sw.sounds.useFalloff = true;
        sw.zIndex(101);

        final float spawnX = (dir == Direction.S || dir == Direction.N) ? 5 : 0;
        final float spawnY = (dir == Direction.E || dir == Direction.W) ? 5 : 0;

        sw.spawnOffset(spawnX, spawnY);
        play.addObstacle(sw);

        return sw;
    }

    void addLava(final float x, final float y, final float width, final float height) {
        final Entity lava = new Entity();
        lava.move(x * 30.0f, y * 30.0f);
        lava.setImage(2, resources.getAnimation("lethal"));
        lava.bounds.size.width = width * 30.0f;
        lava.bounds.size.height = height * 30.0f;
        lava.addEvent(Factory.hitMain(lava, play, -1));

        add(lava);
    }

    void addWeak(final int x, final int y) {
        final DestroyablePlatform weak = new DestroyablePlatform(x * 30, y * 30, play);
        weak.setImage(resources.getAnimation("weak")[0]);
        weak.setDestroyImage(new Animation<>(2, resources.getAnimation("weak")));
        add(weak);
    }
}