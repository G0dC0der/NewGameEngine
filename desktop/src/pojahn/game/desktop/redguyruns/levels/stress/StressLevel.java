package pojahn.game.desktop.redguyruns.levels.stress;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import pojahn.game.core.Entity;
import pojahn.game.core.PlayableEntity;
import pojahn.game.desktop.redguyruns.util.ResourceUtil;
import pojahn.game.entities.movement.PathDrone;
import pojahn.game.essentials.EntityBuilder;
import pojahn.game.essentials.Factory;
import pojahn.game.essentials.ResourceManager;
import pojahn.game.essentials.Utils;
import pojahn.game.essentials.stages.PixelBasedLevel;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

public class StressLevel extends PixelBasedLevel {

    private ResourceManager res;
    private PlayableEntity play;

    @Override
    public void init(final Serializable meta) throws Exception {

        res.loadAnimation(Gdx.files.internal("res/mtrace/flag"));

        getEngine().timeFont = res.getFont("sansserif32.fnt");
        createMap(res.getPixmap("pixmap.png"));

        Utils.playMusic(res.getMusic("music.ogg"), 3.58f, .7f);
    }

    @Override
    public void build() {
        /*
         * Setup
         */
        final Music drillSound = res.getMusic("sawLoopMusic.wav");
        drillSound.setVolume(0);
        if (!drillSound.isPlaying()) {
            drillSound.setLooping(true);
            drillSound.play();
        }

        /*
         * Main Character
         */
        play = ResourceUtil.getGravityMan(res);
        play.move(158, 1460 - 1);
        add(play);

        /*
         * Background & Foreground
         */
        add(new EntityBuilder().image(res.getImage("foreground.png")).zIndex(500).build());
        add(new EntityBuilder().image(res.getImage("background.png")).zIndex(-5).build());

        /*
         * Drills
         */
        final int height = res.getAnimation("drills")[0].getHeight();
        final AtomicReference<PathDrone> pathRef = new AtomicReference<>();
        for (int i = 0; i < 34; i++) {
            final PathDrone drill = new PathDrone(0, i * height);
            drill.setImage(5, res.getAnimation("drills"));
            drill.addEvent(Factory.hitMain(drill, play, -1));
            drill.setMoveSpeed(1.35f);
            drill.zIndex(1000);
            drill.appendPath(getWidth(), i * height);
            runOnceWhen(drill::freeze, ()-> !play.isAlive());
            runOnceWhen(()-> drill.setMoveSpeed(3), ()-> drill.x() > 3126);

            add(drill);
            if (i == 0) {
                pathRef.set(drill);
            }
        }

        /*
         * Drill Sound
         */
        final PathDrone dummyDrone = new PathDrone(0,0);
        dummyDrone.addEvent(()-> dummyDrone.bounds.pos.y = play.y());
        dummyDrone.appendPath(getWidth(), 0);
        dummyDrone.setMoveSpeed(1.35f);
        dummyDrone.sounds.useFalloff = true;
        dummyDrone.addEvent(()-> drillSound.setVolume(dummyDrone.sounds.calc()));
        runOnceWhen(dummyDrone::freeze, ()-> !play.isAlive());
        runOnceWhen(()-> dummyDrone.setMoveSpeed(3), ()-> dummyDrone.x() > 3126);
        add(dummyDrone);

        /*
         * Camera
         */
        final float halfScreenWidth = getEngine().getScreenSize().width / 2;
        add(()-> {
            if (pathRef.get().x() + halfScreenWidth + 2 > getEngine().tx()) {
                getEngine().translate(pathRef.get().x() + halfScreenWidth + 2, getEngine().ty());
            }
        });

        /*
		 * Flag
		 */
        Entity fl = new Entity();
        fl.setImage(5, res.getAnimation("flag"));
        fl.move(5120, 735);
        add(fl);
        runOnceWhen(play::win, ()-> fl.collidesWith(play));
    }

    @Override
    public void dispose() {
        res.disposeAll();
    }

    @Override
    public String getLevelName() {
        return "Stress Level";
    }

    @Override
    public Music getStageMusic() {
        return res.getMusic("music.ogg");
    }
}
