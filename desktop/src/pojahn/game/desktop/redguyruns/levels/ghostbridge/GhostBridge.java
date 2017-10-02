package pojahn.game.desktop.redguyruns.levels.ghostbridge;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import pojahn.game.core.Collisions;
import pojahn.game.core.Entity;
import pojahn.game.desktop.redguyruns.util.ResourceUtil;
import pojahn.game.entities.BigImage;
import pojahn.game.entities.Boo;
import pojahn.game.entities.Particle;
import pojahn.game.entities.PathDrone;
import pojahn.game.entities.SolidPlatform;
import pojahn.game.entities.mains.GravityMan;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.Direction;
import pojahn.game.essentials.EntityBuilder;
import pojahn.game.essentials.Factory;
import pojahn.game.essentials.Hitbox;
import pojahn.game.essentials.Image2D;
import pojahn.game.essentials.ResourceManager;
import pojahn.game.essentials.Utils;
import pojahn.game.essentials.Vitality;
import pojahn.game.essentials.stages.TileBasedLevel;
import pojahn.lang.PingPongFloat;

import java.io.Serializable;
import java.util.stream.Stream;

public class GhostBridge extends TileBasedLevel {

    private ResourceManager resources;
    private GravityMan play;
    private Music music;
    private boolean keyTaken;

    @Override
    public void init(final Serializable meta) throws Exception {
        resources = new ResourceManager();
        resources.loadContentFromDirectory(Gdx.files.internal("res/data"));
        resources.loadContentFromDirectory(Gdx.files.internal("res/general"));
        resources.loadContentFromDirectory(Gdx.files.internal("res/ghostbridge"));

        getEngine().timeFont = resources.getFont("sansserif32.fnt");
        parse(resources.getTiledMap("map.tmx"));

        Stream.of(resources.getAnimation("main")).forEach(Image2D::createPixelData);
        resources.getImage("hide.png").createPixelData();
        resources.getImage("attack.png").createPixelData();
        resources.getImage("spikes_n.png").createPixelData();
        resources.getImage("spikes_e.png").createPixelData();
        resources.getImage("spikes_w.png").createPixelData();

        music = resources.getMusic("music.ogg");
        Utils.playMusic(music, 17.55f, .7f);
    }

    @Override
    public void build() {
        keyTaken = false;

        /*
         * Main Character
         */
        play = ResourceUtil.getGravityMan(resources);
        play.move(1 * 64, 6 * 64);
        play.addEvent(Factory.preventHorizontalOverlap(play, this));
        add(play);

        /*
         * Background and foreground
         */
        final Entity worldImage = getWorldImage();
        worldImage.zIndex(2000);
        add(worldImage);

        final BigImage bgImage = new BigImage(BigImage.RenderStrategy.PARALLAX_REPEAT);
        bgImage.setImage(resources.getImage("background.png"));
        bgImage.zIndex(-100);
        add(bgImage);

        /*
         * Ghost
         */
        add(getBoo(1, 200));

        /*
         * Solid Platforms
         */
        add(getBlock(4 * 64, 7 * 64));
        add(getBlock(8 * 64, 7 * 64));
        add(getBlock(12 * 64, 7 * 64));
        add(getBlock(16 * 64, 7 * 64));

        /*
         * Acid
         */
        final PingPongFloat alpha = new PingPongFloat(.85f, 1f, .005f);
        final PingPongFloat yPos = new PingPongFloat(850, 852, .1f);
        final Entity acid = new EntityBuilder().y(850).image(resources.getImage("acid.png")).zIndex(-1).build();
        acid.addEvent(Factory.hitMain(acid, play, -1));
        acid.addEvent(() -> {
            acid.tint.a = alpha.get();
            acid.bounds.pos.y = yPos.get();
        });
        add(acid);

        /*
         * Bubbles
         */
        final Entity bubbles = new Entity();
        bubbles.move(0, 850);
        bubbles.setImage(2, resources.getAnimation("bubbles"));
        bubbles.tint.a = .3f;
        add(bubbles);

        /*
         * Key
         */
        final Entity key = new Entity();
        key.move(1240, 402);
        key.setImage(4, resources.getAnimation("key"));
        key.ifCollides(play).then(() -> {
            key.die();
            resources.getSound("collect3.wav").play();
            keyTaken = true;
        });
        add(key);

        /*
         * Goal Door
         */
        final Entity door = new Entity();
        door.move(67, 385);
        door.setImage(resources.getImage("door.png"));
        door.zIndex(-2);
        door.addEvent(() -> {
            if (keyTaken && play.getState() == Vitality.ALIVE && play.collidesWith(door)) {
                play.win();
            }
        });
        add(door);

        /*
         * Bars
         */
        final Entity bars = new Entity();
        bars.move(67, 385);
        bars.setImage(4, resources.getImage("bars.png"));
        bars.zIndex(-1);
        bars.addEvent(() -> {
            if (keyTaken)
                bars.move(-1000, -1000);
        });
        add(bars);
    }

    private SolidPlatform getBlock(final float x, final float y) {
        final SolidPlatform solp = new SolidPlatform(x, y, play);
        solp.setImage(resources.getImage("evileye.png"));
        solp.move(x, y);
        solp.tint.a = .7f;

        runOnceWhen(() -> {
//            add(getGlow(solp));
            runOnceAfter(() -> {
                add(getSpikes(Direction.N, x, y));
                resources.getSound("spikesintro.wav").play();
            }, 40);
        }, () -> Collisions.rectanglesCollide(play.x(), play.y(), play.width(), play.height(), x, y - 1, 64, 1));

        runOnceWhen(() -> {
//            add(getGlow(solp));
            runOnceAfter(() -> {
                add(getSpikes(Direction.E, x, y));
                resources.getSound("spikesintro.wav").play();
            }, 40);
        }, () -> Collisions.rectanglesCollide(play.x(), play.y(), play.width(), play.height(), x + 64, y, 1, 64));

        runOnceWhen(() -> {
//            add(getGlow(solp));
            runOnceAfter(() -> {
                add(getSpikes(Direction.W, x, y));
                resources.getSound("spikesintro.wav").play();
            }, 40);
        }, () -> Collisions.rectanglesCollide(play.x(), play.y(), play.width(), play.height(), x - 1, y, 1, 64));

        return solp;
    }

    private Particle getGlow(final Entity parent) {
        final Particle glow = new Particle();
        glow.zIndex(10);
        glow.setImage(7, resources.getAnimation("glow"));
        glow.center(parent);

        return glow;
    }

    private Entity getSpikes(final Direction dir, final float x, final float y) {
        final PathDrone spikes = new PathDrone(x, y);
        if (dir == Direction.N) {
            spikes.setImage(resources.getImage("spikes_n.png"));
            spikes.appendPath(x, y - 19);
        } else if (dir == Direction.E) {
            spikes.setImage(resources.getImage("spikes_e.png"));
            spikes.appendPath(x + 60, y);
        } else if (dir == Direction.W) {
            spikes.setImage(resources.getImage("spikes_w.png"));
            spikes.appendPath(x - 20, y);
        }

        spikes.addEvent(Factory.hitMain(spikes, play, -1));
        spikes.setHitbox(Hitbox.PIXEL);
        spikes.zIndex(-1);
        spikes.tint.a = .5f;

        return spikes;
    }

    private Boo getBoo(final float x, final float y) {
        final Animation<Image2D> booImg = new Animation<>(8, resources.getImage("hide.png"));
        final Animation<Image2D> booAttackImg = new Animation<>(5, resources.getImage("attack.png"));

        final Boo boo = new Boo(x, y, play);
        boo.setHideImage(booImg);
        boo.setHuntImage(booAttackImg);
        boo.setFacings(2);
        boo.addEvent(boo::face);
        boo.setHitbox(Hitbox.PIXEL);
        boo.ifCollides(play).then(play::lose);
        boo.zIndex(1000);
        boo.tint.a = .4f;
        boo.setDetectSound(resources.getSound("boo.wav"));

        return boo;
    }

    @Override
    public Music getStageMusic() {
        return music;
    }

    @Override
    public String getLevelName() {
        return "Ghost Bridge";
    }

    @Override
    public void dispose() {
        resources.disposeAll();
    }
}
