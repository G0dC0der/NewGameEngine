package pojahn.game.desktop.redguyruns.levels.training2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.google.common.collect.ImmutableList;
import pojahn.game.core.Entity;
import pojahn.game.desktop.redguyruns.levels.training1.Friend;
import pojahn.game.desktop.redguyruns.util.ResourceUtil;
import pojahn.game.entities.Collectable;
import pojahn.game.entities.SolidPlatform;
import pojahn.game.entities.mains.GravityMan;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.EntityBuilder;
import pojahn.game.essentials.Image2D;
import pojahn.game.essentials.ResourceManager;
import pojahn.game.essentials.Utils;
import pojahn.game.essentials.stages.PixelBasedLevel;
import pojahn.lang.Bool;

import java.io.Serializable;

public class TrainingStage2 extends PixelBasedLevel {

    private ResourceManager res;
    private GravityMan man;
    private int collectedGems, gems, pressedButtons, buttons;

    @Override
    public void init(final Serializable meta) throws Exception {
        res = new ResourceManager();
        res.loadContentFromDirectory(Gdx.files.internal("res/data"));
        res.loadContentFromDirectory(Gdx.files.internal("res/general"));
        res.loadContentFromDirectory(Gdx.files.internal("res/training2"));
        res.loadFont(Gdx.files.internal("res/training1/talking.fnt"));
        res.loadAnimation(Gdx.files.internal("res/mtrace/flag"));

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
        man.move(216, 939);
        add(man);

        /*
         * Background & Foreground
         */
        add(new EntityBuilder().image(res.getImage("foreground.png")).zIndex(500).build());
        add(new EntityBuilder().image(res.getImage("background.png")).zIndex(-5).build());

        /*
         * Gems
         */
        collectedGems = gems = 0;
        addGem(435, 261);
        addGem(497, 261);
        addGem(723, 261);
        addGem(785, 261);
        
        /*
		 * Key + door
		 */
        final SolidPlatform door = new SolidPlatform(2016, 672, man);
        door.setImage(res.getImage("door.png"));

        final Collectable key = new Collectable(1261, 647, man);
        key.setImage(res.getImage("key.png"));
        key.setCollectSound(res.getSound("collect1.wav"));
        key.setCollectEvent(collector -> discard(door));

        add(door);
        add(key);

        /*
         * Rope
         */
        final Entity rope = new Entity();
        rope.setImage(res.getImage("rope.png"));
        rope.move(2984, 482);
        rope.addEvent(()->{
            final float limit = man.tVel.x - (man.tVel.x / 4);

            if(man.vel.x > limit || man.vel.x < -limit || man.vel.y > 10 || man.vel.y < -10)
                man.removeObstacle(rope);
            else if(!man.collidesWith(rope))
                man.addObstacle(rope);
        });
        man.addObstacle(rope);
        add(rope);

        /*
         * Buttons & Doors
         */
        pressedButtons = buttons = 0;
        final SolidPlatform door2 = new SolidPlatform(2688, 192, man);
        door2.setImage(res.getImage("door.png"));
        addButton(2629, 669, door2);
        addButton(2629, 477, door2);
        addButton(2629, 285, door2);
        addButton(2437, 573, door2);
        addButton(2437, 381, door2);
        add(door2);

        /*
         * Friends
         */
        addFriend(823, 631, 30, -10, "Get the key to open the door.");
        addFriend(2359, 727, -50, -43, "Press all the buttons and\nquickly pass through the gate.");
        addFriend(2906, 415, -25, -43, "Move cautiously at the rope or you might fall down.");
        addFriend(607, 343, -35, -43, "Make sure to collect the gem.");

        /*
		 * Goal
		 */
        final Entity flag = new Entity();
        flag.setImage(3, res.getAnimation("flag"));
        flag.move(3972, 412);
        flag.addEvent(()->{
            if(flag.collidesWith(man) && !man.isDone() && gems == collectedGems)
                man.win();
        });
        add(flag);
    }

    private void addFriend(final float x, final float y, final float offsetX, final float offsetY, final String text) {
        final ImmutableList<Sound> talkList = ImmutableList.of(res.getSound("talk1.wav"), res.getSound("talk2.wav"), res.getSound("talk3.wav"));
        final int talkDelay = 12;

        final Friend friend = new Friend();
        friend.setImage(res.getImage("friend.png"));
        friend.zIndex(-1);
        friend.move(x, y);
        friend.setOffsetX(offsetX);
        friend.setOffsetY(offsetY);
        friend.setFont(res.getFont("talking.fnt"));
        friend.setText(text);
        friend.setSubjects(ImmutableList.of(man));
        friend.setTalkEvent(()-> {
            runOnceAfter(()-> talkList.get(MathUtils.random(0 ,2)).play(), 0 * talkDelay);
            runOnceAfter(()-> talkList.get(MathUtils.random(0 ,2)).play(), 1 * talkDelay);
            runOnceAfter(()-> talkList.get(MathUtils.random(0 ,2)).play(), 2 * talkDelay);
            runOnceAfter(()-> talkList.get(MathUtils.random(0 ,2)).play(), 3 * talkDelay);
            runOnceAfter(()-> talkList.get(MathUtils.random(0 ,2)).play(), 4 * talkDelay);
            runOnceAfter(()-> talkList.get(MathUtils.random(0 ,2)).play(), 5 * talkDelay);
        });

        add(friend);
    }

    private void addButton(final float x, final float y, final SolidPlatform door) {
        ++buttons;
        final Bool buttonDown = new Bool();

        final Entity button = new Entity();
        button.move(x, y);
        button.setImage(res.getImage("bu.png"));
        button.addEvent(()-> {
            if (!buttonDown.value && button.collidesWith(man)) {
                button.sounds.play(res.getSound("press.wav"));
                button.setVisible(false);
                buttonDown.value = true;

                if (++pressedButtons == buttons) {
                    door.move(0,0);
                    res.getSound("doorshut.wav").play();
                } else {
                    runOnceAfter(()-> {
                        if (pressedButtons != buttons) {
                            pressedButtons--;
                            door.move(2688, 192);
                            button.setVisible(true);
                            buttonDown.value = false;
                        }
                    }, 500);
                }
            }
        });
        add(button);
    }

    private void addGem(final float x, final float y) {
        gems++;

        final Animation<Image2D> gemImage = new Animation<>(3, res.getAnimation("collect"));
        gemImage.pingPong(true);

        final Collectable gem = new Collectable(x, y, man);
        gem.setImage(gemImage);
        gem.setCollectEvent(collector -> collectedGems++);
        gem.setCollectSound(res.getSound("collect2.wav"));

        add(gem);
    }

    @Override
    public void dispose() {
        res.disposeAll();
    }

    @Override
    public String getLevelName() {
        return "Training Stage 2";
    }

    @Override
    public Music getStageMusic() {
        return res.getMusic("music.ogg");
    }
}
