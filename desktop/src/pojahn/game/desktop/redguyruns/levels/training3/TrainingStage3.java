package pojahn.game.desktop.redguyruns.levels.training3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.google.common.collect.ImmutableList;
import pojahn.game.core.Entity;
import pojahn.game.desktop.redguyruns.levels.training1.Friend;
import pojahn.game.desktop.redguyruns.util.ResourceUtil;
import pojahn.game.entities.BigImage;
import pojahn.game.entities.Collectable;
import pojahn.game.entities.DestroyablePlatform;
import pojahn.game.entities.mains.GravityMan;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.EntityBuilder;
import pojahn.game.essentials.Image2D;
import pojahn.game.essentials.ResourceManager;
import pojahn.game.essentials.Utils;
import pojahn.game.essentials.stages.PixelBasedLevel;

import java.io.Serializable;

public class TrainingStage3 extends PixelBasedLevel {

    private ResourceManager res;
    private GravityMan man;

    @Override
    public void init(final Serializable meta) throws Exception {
        res = new ResourceManager();
        res.loadContentFromDirectory(Gdx.files.internal("res/data"));
        res.loadContentFromDirectory(Gdx.files.internal("res/general"));
        res.loadContentFromDirectory(Gdx.files.internal("res/training3"));
        res.loadFont(Gdx.files.internal("res/training1/talking.fnt"));
        res.loadSound(Gdx.files.internal("res/climb/collapsing.wav"));

        getEngine().timeFont = res.getFont("sansserif32.fnt");
        getEngine().timeColor = Color.YELLOW;
        createMap(res.getPixmap("pixmap.png"));

        Utils.playMusic(res.getMusic("music.ogg"), 0, .8f);
    }

    @Override
    public void build() {
        /*
         * Main Character
         */
        man = ResourceUtil.getGravityMan(res);
        man.zIndex(3);
        man.move(3555, 3984);
        add(man);

        /*
         * Background & Foreground
         */
        add(new EntityBuilder().image(res.getImage("foreground.png")).zIndex(500).build());
        add(new EntityBuilder().image(res.getImage("background.png")).zIndex(-5).build(BigImage.class, BigImage.RenderStrategy.FIXED));

        /*
         * Weak Platforms
         */
        final DestroyablePlatform destP = new DestroyablePlatform(0, 0, man);
        destP.setImage(res.getImage("weak.png"));
        destP.setDestroyImage(new Animation<>(1, res.getImage("weakdie.png")));
        destP.setDestroyFrames(90);
        destP.setBreakSound(res.getSound("collapsing.wav"));

        add(destP.getClone().move(2630, 3893));
        add(destP.getClone().move(2490, 3893));
        add(destP.getClone().move(2340, 3853));
        add(destP.getClone().move(2390, 3753));

        /*
         * Friends
         */
        addFriend(2900, 3801, -120, -40, "Welcome to Training Paradise!\nJump on the weak platforms and continue upwards");
            addFriend(1391, 3367, -20, -55, "Perform a wall jump to get up there");
            addFriend(868, 2703, -100, -20, "Continue left my friend");
            addFriend(1651, 1985, -130, -20, "Jump at the very end of this platform to reach the other one");
            addFriend(3033, 2093, -180, -20, "North-east is where you want to go :-)");
            addFriend(2891, 1022, -100, -20, "Jump as high and far as possible");
            addFriend(1138, 241, -60, -20, "Almost done ^_^");

        /*
         * Goal
         */
        final Collectable melon = new Collectable(300, 262, man);
        melon.setImage(res.getImage("melon.png"));
        melon.setCollectSound(res.getSound("collect1.wav"));
        melon.setCollectEvent(collector -> man.win());
        add(melon);

    }

    private void addFriend(final float x, final float y, final float offsetX, final float offsetY, final String text) {
        final Friend friend = new Friend();
        friend.setImage(4, res.getAnimation("cloud"));
        friend.zIndex(-1);
        friend.move(x, y);
        friend.setOffsetX(offsetX);
        friend.setOffsetY(offsetY);
        friend.setFont(res.getFont("talking.fnt"));
        friend.setText(text);
        friend.setSubjects(ImmutableList.of(man));
        friend.setTalkingSounds(ImmutableList.of(res.getSound("talking.wav")));

        add(friend);
    }

    @Override
    public void dispose() {
        res.disposeAll();
    }

    @Override
    public String getLevelName() {
        return "Training Stage 3";
    }

    @Override
    public Music getStageMusic() {
        return res.getMusic("music.ogg");
    }
}
