package pojahn.game.desktop.redguyruns.levels.blocks;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import pojahn.game.core.Entity;
import pojahn.game.core.PlayableEntity;
import pojahn.game.desktop.redguyruns.util.ResourceUtil;
import pojahn.game.entities.image.StaticImage;
import pojahn.game.entities.object.OneWay;
import pojahn.game.entities.movement.PathDrone;
import pojahn.game.entities.platform.SolidPlatform;
import pojahn.game.entities.platform.SolidPlatform.FollowMode;
import pojahn.game.entities.platform.TilePlatform;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.Direction;
import pojahn.game.essentials.EntityBuilder;
import pojahn.game.essentials.Factory;
import pojahn.game.essentials.Hitbox;
import pojahn.game.essentials.Image2D;
import pojahn.game.essentials.ResourceManager;
import pojahn.game.essentials.Utils;
import pojahn.game.essentials.Vitality;
import pojahn.game.essentials.stages.PixelBasedLevel;

import java.io.Serializable;
import java.util.stream.Stream;

public class SquareTown extends PixelBasedLevel {

    private ResourceManager resources;
    private Music music;
    private PlayableEntity play;
    private int collectedGems, gems, collectedGems2, gems2;

    @Override
    public void init(final Serializable meta) throws Exception {
        resources = new ResourceManager();
        resources.loadContentFromDirectory(Gdx.files.internal("res/data"));
        resources.loadContentFromDirectory(Gdx.files.internal("res/general"));
        resources.loadContentFromDirectory(Gdx.files.internal("res/blocks"));

        getEngine().timeFont = resources.getFont("sansserif32.fnt");
        Stream.of(resources.getAnimation("main")).forEach(Image2D::createPixelData);
        Stream.of(resources.getAnimation("crabImg")).forEach(Image2D::createPixelData);
        Stream.of(resources.getAnimation("spikeyImg")).forEach(Image2D::createPixelData);
        Stream.of(resources.getAnimation("spikeyminiImg")).forEach(Image2D::createPixelData);
        Stream.of(resources.getImage("spikeseImg.png")).forEach(Image2D::createPixelData);
        Stream.of(resources.getImage("spikesnImg.png")).forEach(Image2D::createPixelData);
        Stream.of(resources.getImage("spikeswImg.png")).forEach(Image2D::createPixelData);
        createMap(resources.getPixmap("pixmap.png"));
        resources.remove("pixmap.png");

        music = resources.getMusic("music.ogg");
        Utils.playMusic(music, 29.57f, .65f);
    }

    @Override
    public void build() {
        collectedGems = gems = collectedGems2 = gems2 = 0;

        /*
         * Main Character
         */
        play = ResourceUtil.getGravityMan(resources);
        play.move(196, 1839);
        play.zIndex(50);
        play.addTileEvent((tileType) -> {
            if (tileType == Tile.LETHAL)
                play.setState(Vitality.DEAD);
        });
        add(play);

        /*
         * Background and foreground
         */
        add(new EntityBuilder().image(resources.getImage("foreground.png")).zIndex(Integer.MAX_VALUE).build());
        add(new EntityBuilder().image(resources.getImage("background.png")).zIndex(-5).build(StaticImage.class));

		/*
         * Spikey
		 */
        final PathDrone spikey = new PathDrone(240, 1202);
        spikey.appendPath();
        spikey.appendPath(1020, spikey.y());
        spikey.appendPath(1020, 1501);
        spikey.appendPath(240, 1501);
        spikey.addEvent(Factory.hitMain(spikey, play, -1));
        spikey.setImage(4, resources.getAnimation("spikeyImg"));
        spikey.setHitbox(Hitbox.PIXEL);
        spikey.setMoveSpeed(getSpikeySpeed());
        add(spikey);

		/*
         * Platforms and gems
		 */
        final OneWay ow1 = new OneWay(360, 1178, Direction.N, play);
        ow1.setImage(resources.getImage("platformImg.png"));

        final OneWay ow2 = new OneWay(854, 1178, Direction.N, play);
        ow2.setImage(resources.getImage("platformImg.png"));

        add(ow1);
        add(ow2);

        add(getGem(405, 1116));
        add(getGem(899, 1116));
        add(getGem(652, 1176));
        add(getGem(552, 1176));
        add(getGem(752, 1176));

		/*
		 * Crabs
		 */
        final PathDrone crab1 = new PathDrone(1181, 1302);
        crab1.appendPath();
        crab1.appendPath(crab1.x(), 792);
        crab1.setImage(getCrabAnimationSpeed(), resources.getAnimation("crabImg"));
        crab1.setMoveSpeed(getCrabSpeed());
        crab1.setHitbox(Hitbox.PIXEL);
        crab1.addEvent(Factory.hitMain(crab1, play, -1));
        crab1.sounds.useFalloff = true;
        crab1.addEvent(Factory.repeatSound(crab1, resources.getSound("crabMoveLoop.wav"), 25));

        final PathDrone crab2 = new PathDrone(1181, 182);
        crab2.appendPath();
        crab2.appendPath(crab2.x(), 432);
        crab2.setImage(getCrabAnimationSpeed(), resources.getAnimation("crabImg"));
        crab2.setMoveSpeed(getCrabSpeed());
        crab2.setHitbox(Hitbox.PIXEL);
        crab2.addEvent(Factory.hitMain(crab2, play, -1));
        crab2.addEvent(Factory.repeatSound(crab1, resources.getSound("crabMoveLoop.wav"), 25));

        add(crab1);
        add(crab2);

		/*
		 * Top Block
		 */
        final SolidPlatform topBlock = getBlock(1620, 180, Color.YELLOW);
        topBlock.appendPath(1620, 300);
        add(topBlock);

		/*
		 * Crushing Block Wall
		 */
        final float xs = 1200;
        float ys = 480;
        final float xe = 1620;

        final SolidPlatform w1 = getBlock(xs, ys, Color.YELLOW);
        w1.appendPath(xe, ys);
        w1.setMoveSpeed(getWallSpeed());
        ys += w1.height();

        final SolidPlatform w2 = getBlock(xs, ys, Color.RED);
        w2.appendPath(xe, ys);
        w2.setMoveSpeed(getWallSpeed());
        ys += w1.height();

        final SolidPlatform w3 = getBlock(xs, ys, Color.YELLOW);
        w3.appendPath(xe, ys);
        w3.setMoveSpeed(getWallSpeed());
        ys += w1.height();

        final SolidPlatform w4 = getBlock(xs, ys, Color.RED);
        w4.appendPath(xe, ys);
        w4.setMoveSpeed(getWallSpeed());
        ys += w1.height();

        final SolidPlatform w5 = getBlock(xs, ys, Color.YELLOW);
        w5.appendPath(xe, ys);
        w5.setMoveSpeed(getWallSpeed());
        ys += w1.height();

        add(w1);
        add(w2);
        add(w3);
        add(w4);
        add(w5);

		/*
		 * Second batch of gems and blockade block.
		 */
        add(getGem2(1300, 700));
        add(getGem2(1400, 588));
        add(getGem2(1541, 665));
        add(getGem2(1656, 490));

        final SolidPlatform embargo = new SolidPlatform(xe, ys, play);
        embargo.setImage(resources.getImage("blockadeImg.png"));
        embargo.appendPath(xe + embargo.width(), ys, 0, false, () -> discard(embargo));
        embargo.freeze();
        embargo.addEvent(() -> {
            if (gems2 == collectedGems2)
                embargo.unfreeze();
        });
        add(embargo);

		/*
		 * Rectangular moving blocks
		 */
        //Upper right
        final SolidPlatform rec1 = getBlock(1620, 840, Color.RED);
        rec1.appendPath(1620, 840);
        rec1.appendPath(1500, 840);
        rec1.appendPath(1500, 960);
        rec1.appendPath(1620, 960);
        rec1.setMoveSpeed(2);
        rec1.unfreeze();

        //Upper left
        final SolidPlatform rec2 = getBlock(1260, 840, Color.RED);
        rec2.appendPath(1260, 840);
        rec2.appendPath(1380, 840);
        rec2.appendPath(1380, 960);
        rec2.appendPath(1260, 960);
        rec2.setMoveSpeed(2);
        rec2.unfreeze();

        //Lower left
        final SolidPlatform rec3 = getBlock(1380, 960, Color.RED);
        rec3.appendPath(1380, 960);
        rec3.appendPath(1260, 960);
        rec3.appendPath(1260, 1080);
        rec3.appendPath(1380, 1080);
        rec3.setMoveSpeed(2);
        rec3.unfreeze();

        final SolidPlatform rec4 = getBlock(1500, 960, Color.RED);
        rec4.appendPath(1500, 960);
        rec4.appendPath(1620, 960);
        rec4.appendPath(1620, 1080);
        rec4.appendPath(1500, 1080);
        rec4.setMoveSpeed(2);
        rec4.unfreeze();

        add(rec1);
        add(rec2);
        add(rec3);
        add(rec4);

		/*
		 * Spikes
		 */
        final Entity spike1 = new Entity();
        spike1.move(1440, 1366);
        spike1.setHitbox(Hitbox.PIXEL);
        spike1.setImage(4, resources.getImage("spikesnImg.png"));
        spike1.addEvent(Factory.hitMain(spike1, play, -1));

        final Entity spike2 = spike1.getClone().move(1620, 1261);
        spike2.setImage(4, resources.getImage("spikeseImg.png"));
        spike2.addEvent(Factory.hitMain(spike2, play, -1));

        final Entity spike3 = spike1.getClone().move(1666, 1381);
        spike3.setImage(4, resources.getImage("spikeswImg.png"));
        spike3.addEvent(Factory.hitMain(spike3, play, -1));

        add(spike1);
        add(spike2);
        add(spike3);

		/*
		 * Block going up and down
		 */
        final SolidPlatform bl = getBlock(1380, 1560, Color.YELLOW);
        bl.appendPath(1380, 1440);
        bl.appendPath(1380, 1560, 0, true, null);
        bl.setMoveSpeed(1.8f);
        bl.unfreeze();

        add(bl);

		/*
		 * Two pushing blocks
		 */
        final SolidPlatform push1 = getBlock(2520, 1860, Color.RED);
        push1.appendPath(2520, 1860);
        push1.appendPath(1921, 1860);
        push1.appendPath(1921, 1980);
        push1.appendPath(2520, 1980, 0, true, null);
        push1.setMoveSpeed(getPushSpeed());
        push1.unfreeze();

        final SolidPlatform push2 = getBlock(2520, 1800, Color.YELLOW);
        push2.appendPath(2520, 1800);
        push2.appendPath(1921, 1800);
        push2.appendPath(1921, 1920);
        push2.appendPath(2520, 1920, 0, true, null);
        push2.setMoveSpeed(getPushSpeed());
        push2.unfreeze();

        add(push1);
        add(push2);

        /*
         * Metal Blocks
         */
        final Image2D steelBlockImg = resources.getImage("steelBlockImg.png");
        final TilePlatform met1 = new TilePlatform(2760, 1859, play);
        met1.setImage(steelBlockImg);
        met1.setMoveSpeed(2);
        met1.appendPath(2760, 1859);
        met1.appendPath(2760, 1680);
        met1.appendPath(2820, 1680);
        met1.appendPath(2820, 1859, 100, true, null);

        final TilePlatform met2 = met1.getClone();
        met2.move(2760, 1441);
        met2.clearData();
        met2.appendPath(2760, 1441);
        met2.appendPath(2760, 1620);
        met2.appendPath(2820, 1620);
        met2.appendPath(2820, 1441, 100, true, null);

        add(met1);
        add(met2);

        /*
         * Up and down metal blocks
         */
        float xStart = 2820;
        float yBottom = 1380;
        final float yTop = 1380 - steelBlockImg.getHeight();

        for (int i = 0; i < 7; i++) {
            final SolidPlatform solp = new SolidPlatform(0, 0, play);
            solp.setImage(steelBlockImg);
            solp.setMoveSpeed(.5f);

            if (i % 2 == 0) {
                solp.move(xStart, yBottom);
                solp.appendPath(xStart, yBottom);
                solp.appendPath(xStart, yTop);
            } else {
                solp.move(xStart, yTop);
                solp.appendPath(xStart, yTop);
                solp.appendPath(xStart, yBottom);
            }
            add(solp);

            xStart += steelBlockImg.getWidth();
        }

		/*
		 * Final Steel blocks
		 */
        final SolidPlatform solp = new SolidPlatform(3000, 1080, play);
        solp.setImage(steelBlockImg);
        solp.setMoveSpeed(1.5f);
        solp.setFollowMode(FollowMode.STRICT);
        solp.appendPath(3000, 1080, 80, false, null);
        solp.appendPath(3000, 1080 - (steelBlockImg.getHeight() * 2), 80, false, null);

        final SolidPlatform solp2 = solp.getClone();
        solp2.move(2880, 900);
        solp2.clearData();
        solp2.appendPath(2880, 900, 80, false, null);
        solp2.appendPath(2880 - (steelBlockImg.getWidth() * 7), 900, 80, false, null);

        final SolidPlatform solp3 = solp.getClone();
        solp3.move(2280, 780);
        solp3.setMoveSpeed(1);
        solp3.clearData();
        solp3.appendPath(2280, 780);
        solp3.appendPath(2340, 780);
        solp3.appendPath(2340, 840);
        solp3.appendPath(2280, 840);

        final SolidPlatform solp4 = solp3.getClone();
        solp4.move(2340, 840);
        solp4.clearData();
        solp4.appendPath(2340, 840);
        solp4.appendPath(2280, 840);
        solp4.appendPath(2280, 780);
        solp4.appendPath(2340, 780);

        final SolidPlatform solp5 = solp3.getClone();
        solp5.move(3120, 480);
        solp5.clearData();
        solp5.appendPath(3120, 480);
        solp5.appendPath(3120 + solp5.width(), 480);

        final SolidPlatform solp6 = solp3.getClone();
        solp6.move(3120 + solp5.width(), 480 - (solp5.height() * 3));
        solp6.clearData();
        solp6.appendPath(3120, 480 - (solp5.height() * 3));
        solp6.appendPath(3120 + solp5.width(), 480 - (solp5.height() * 3));


        add(solp);
        add(solp2);
        add(solp3);
        add(solp4);
        add(solp5);
        add(solp6);

		/*
		 * Mini Spikeies
		 */
        final PathDrone mspikey = new PathDrone(2489, 688);
        mspikey.setImage(5, resources.getAnimation("spikeyminiImg"));
        mspikey.setMoveSpeed(2);
        mspikey.setHitbox(Hitbox.PIXEL);
        mspikey.addEvent(Factory.hitMain(mspikey, play, -1));
        mspikey.appendPath(2489, 688);
        mspikey.appendPath(2580, 688);
        mspikey.appendPath(2580, 780);
        mspikey.appendPath(2489, 780);

        final PathDrone smpikey2 = mspikey.getClone();
        smpikey2.move(2940, 780);
        smpikey2.clearData();
        smpikey2.addEvent(Factory.hitMain(smpikey2, play, -1));
        smpikey2.appendPath(2940, 780);
        smpikey2.appendPath(2940, 688);
        smpikey2.appendPath(2850, 688);
        smpikey2.appendPath(2850, 780);

        add(mspikey);
        add(smpikey2);

		/*
		 * Flag
		 */
        final Entity flagPole = new Entity();
        flagPole.setImage(resources.getImage("flagPoleImg.png"));
        flagPole.move(2836, 139);

        final Entity flag = new Entity();
        flag.setImage(4, resources.getAnimation("flag"));
        flag.move(2840, 136);

        add(flagPole);
        add(flag);

		/*
		 * Finalizing
		 */
        play.addTileEvent((tileType) -> {
            if (collectedGems == gems && tileType == Tile.CUSTOM_3) {
                gems = -1;
                topBlock.unfreeze();
            } else if (tileType == Tile.CUSTOM_4) {
                w1.unfreeze();
                w2.unfreeze();
                w3.unfreeze();
                w4.unfreeze();
                w5.unfreeze();
            }
        });
    }

    @Override
    public Music getStageMusic() {
        return music;
    }

    @Override
    public String getLevelName() {
        return "Square Town";
    }

    private SolidPlatform getBlock(final float x, final float y, final Color color) {
        final Animation<Image2D> eyes = new Animation<>(6, resources.getAnimation("eyeImg"));
        eyes.stop(true);
        eyes.setLoop(false);

        final Entity eyeObj = new Entity();

        final TilePlatform block = new TilePlatform(x, y, play) {
            @Override
            public void unfreeze() {
                super.unfreeze();
                eyes.stop(false);
            }

            @Override
            public void dispose() {
                discard(eyeObj);
            }
        };
        block.setImage(color.equals(Color.RED) ? resources.getImage("redBlockImg.png") : resources.getImage("yellowBlockImg.png"));
        block.freeze();
        block.setMoveSpeed(1.5f);
        block.zIndex(60);

        eyeObj.setImage(eyes);
        eyeObj.addEvent(Factory.follow(block, eyeObj, 11, 10));
        eyeObj.zIndex(61);
        add(eyeObj);

        return block;
    }

    private Entity getGem(final float x, final float y) {
        gems++;

        final Entity gem = new Entity();
        gem.move(x, y);
        gem.setImage(6, resources.getAnimation("gemImg"));
        gem.addEvent(() -> {
            if (gem.collidesWith(play)) {
                collectedGems++;
                discard(gem);
                resources.getSound("collect1.wav").play();
            }
        });

        return gem;
    }

    private Entity getGem2(final float x, final float y) {
        gems2++;

        final Animation<Image2D> gemImage = new Animation<>(4, resources.getAnimation("gem2Img"));
        gemImage.pingPong(true);

        final Entity gem = new Entity();
        gem.move(x, y);
        gem.setImage(gemImage);
        gem.addEvent(() -> {
            if (gem.collidesWith(play)) {
                collectedGems2++;
                discard(gem);
                resources.getSound("collect2.wav").play();
            }
        });

        return gem;
    }

    private float getSpikeySpeed() {
        return 6.0f;
    }

    private float getCrabSpeed() {
        return 2.0f;
    }

    private int getCrabAnimationSpeed() {
        return 4;
    }

    private float getWallSpeed() {
        return .8f;
    }

    private float getPushSpeed() {
        return 4;
    }

    @Override
    public void dispose() {
        resources.disposeAll();
    }
}
