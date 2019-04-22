package pojahn.game.desktop.redguyruns.levels.training1;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import pojahn.game.core.Entity;
import pojahn.game.core.PlayableEntity;
import pojahn.game.desktop.redguyruns.util.ResourceUtil;
import pojahn.game.entities.platform.SolidPlatform;
import pojahn.game.essentials.EntityBuilder;
import pojahn.game.essentials.ResourceManager;
import pojahn.game.essentials.Utils;
import pojahn.game.essentials.stages.PixelBasedLevel;

import java.io.Serializable;
import java.util.List;

public class TrainingStage1 extends PixelBasedLevel {

    private ResourceManager res;
    private PlayableEntity play;
    private int crystals,collectedCrystals;

    @Override
    public void init(final Serializable meta) throws Exception {
        res = new ResourceManager();
        res.loadContentFromDirectory(Gdx.files.internal("res/data"));
        res.loadContentFromDirectory(Gdx.files.internal("res/general"));
        res.loadContentFromDirectory(Gdx.files.internal("res/training1"));
        res.loadAnimation(Gdx.files.internal("res/lasereverywhere/diamond"));

        getEngine().timeFont = res.getFont("sansserif32.fnt");
        createMap(res.getPixmap("pixmap.png"));

        Utils.playMusic(res.getMusic("music.wav"), 7.80f, .6f);
    }

    @Override
    public void build() {
        /*
         * Main Character
         */
        play = ResourceUtil.getGravityMan(res);
        play.move(61, 1329);
        add(play);

        /*
         * Background & Foreground
         */
        add(new EntityBuilder().image(res.getImage("foreground.png")).zIndex(500).build());
        add(new EntityBuilder().image(res.getImage("background.png")).zIndex(-5).build());

        /*
         * Crystals
         */
        crystals = collectedCrystals = 0;
        runOnceWhen(play::win, ()-> crystals == collectedCrystals);

        addCrystal(1962, 375);
        addCrystal(2112, 425);
        addCrystal(2262, 475);
        addCrystal(2312, 575);
        addCrystal(2312, 925);
        addCrystal(2312, 1175);
        addCrystal(2512, 1125);
        addCrystal(2662, 1075);
        addCrystal(2862, 1125);
        addCrystal(2812, 725);
        addCrystal(2912, 525);
        addCrystal(2787, 475);
        addCrystal(2661, 325);
        addCrystal(2312, 725);

        /*
		 * Moving Platform
		 */
        final SolidPlatform solp = new SolidPlatform(350, 650, play);
        solp.appendPath(350, 650, 10, false, null);
        solp.appendPath(861, 650, 10, false, null);
        solp.setImage(res.getImage("platformImg.png"));
        solp.setMoveSpeed(1);
        add(solp);

        /*
         * Friends
         */
        addFriend(244, 1327, -150, -50, "Use your jump button to jump over the hindrance.\n"
                + "Hold it to reach higher.");
        addFriend(1055, 1127, -310, -50, "You can wall jump by pressing the jump button while sliding on it.\n"
                + "Hold the jump button to reach higher.");
        addFriend(280, 376, -150, -60, "Remember, if you hold the jump button, you reach further/higher.\n"
                + "Also, dont forget that you can move while floating in the air.");
        addFriend(1790, 376, -35, -30, "Collect all the crystals to complete the stage.");
    }

    private void addCrystal(final float x, final float y) {
        crystals++;
        final Entity crystal = new Entity();
        crystal.move(x, y);
        crystal.setImage(5, res.getAnimation("diamond"));
        crystal.addEvent(() -> {
            if (crystal.collidesWith(play)) {
                collectedCrystals++;
                crystal.sounds.play(res.getSound("collect2.wav"));
                discard(crystal);
            }
        });

        add(crystal);
    }

    private void addFriend(final float x, final float y, final float offsetX, final float offsetY, final String text) {
        final Friend friend = new Friend();
        friend.setImage(res.getImage("friendImg.png"));
        friend.zIndex(-1);
        friend.move(x, y);
        friend.setOffsetX(offsetX);
        friend.setOffsetY(offsetY);
        friend.setFont(res.getFont("talking.fnt"));
        friend.setText(text);
        friend.setSubjects(List.of(play));
        friend.setTalkingSounds(List.of(res.getSound("talking.wav")));

        add(friend);
    }

    @Override
    public void dispose() {
        res.disposeAll();
    }

    @Override
    public Music getStageMusic() {
        return res.getMusic("music.wav");
    }

    @Override
    public String getLevelName() {
        return "Training Stage 1";
    }
}
