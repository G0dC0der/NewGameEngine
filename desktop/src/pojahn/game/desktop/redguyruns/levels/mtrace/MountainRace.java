package pojahn.game.desktop.redguyruns.levels.mtrace;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import pojahn.game.core.BaseLogic;
import pojahn.game.core.PlayableEntity;
import pojahn.game.desktop.redguyruns.util.ResourceUtil;
import pojahn.game.entities.BigImage;
import pojahn.game.entities.BigImage.RenderStrategy;
import pojahn.game.entities.mains.GravityMan;
import pojahn.game.essentials.Controller;
import pojahn.game.essentials.EntityBuilder;
import pojahn.game.essentials.Factory;
import pojahn.game.essentials.HUDMessage;
import pojahn.game.essentials.Image2D;
import pojahn.game.essentials.ResourceManager;
import pojahn.game.essentials.Utils;
import pojahn.game.essentials.Vitality;
import pojahn.game.essentials.recording.KeySession;
import pojahn.game.essentials.stages.TileBasedLevel;

import java.io.Serializable;
import java.util.stream.Stream;

public class MountainRace extends TileBasedLevel {

    private ResourceManager resources;
    private PlayableEntity play;
    private Rectangle winArea = new Rectangle(3868, 160, 44, 42);
    private Music music;

    @Override
    public void init(final Serializable meta) throws Exception {
        resources = new ResourceManager();
        resources.loadContentFromDirectory(Gdx.files.internal("res/data"));
        resources.loadContentFromDirectory(Gdx.files.internal("res/general"));
        resources.loadContentFromDirectory(Gdx.files.internal("res/mtrace"));

        getEngine().timeFont = resources.getFont("sansserif32.fnt");
        getEngine().timeColor = Color.BLACK;
        parse(resources.getTiledMap("map.tmx"));

        Stream.of(resources.getAnimation("main")).forEach(Image2D::createPixelData);

        music = resources.getMusic("music.ogg");
        Utils.playMusic(music, 8.45f, .6f);
    }

    @Override
    public void build() {
        /*
         * Main Character
         */
        play = ResourceUtil.getGravityMan(resources);
        play.move(10, 1659);
        add(play);

        /*
         * Background and foreground
         */
        add(getWorldImage());
        add(new EntityBuilder().image(resources.getImage("background.png")).zIndex(-4).move(0, 736).alpha(.8f).build());
        add(new EntityBuilder().image(resources.getImage("sky.png")).zIndex(-5).build(BigImage.class, RenderStrategy.PARALLAX));

        /*
         * Flag
         */
        add(new EntityBuilder().image(resources.getImage("flagpole.png")).zIndex(-1).move(3877, 161).build());
        add(new EntityBuilder().image(resources.getAnimation("flag")).zIndex(-1).move(3881, 161).build());

        add(() -> {
            if (BaseLogic.rectanglesCollide(winArea, play.bounds.toRectangle())) {
                play.setState(Vitality.COMPLETED);
            }
        });

        /*
         * Props
         */
        add(new EntityBuilder().image(resources.getImage("sign.png")).move(86, 1607).zIndex(-1).build());
//        add(new EntityBuilder().image(resources.getImage("tree1.png")).move(385, 1504).zIndex(-1).build());
        add(new EntityBuilder().image(resources.getImage("rock1.png")).move(1683, 1157).zIndex(-1).build());
        add(new EntityBuilder().image(resources.getImage("rock2.png")).move(2549, 1061).zIndex(-1).build());

        /*
         * Contestants
         */
        addContestant((KeySession) resources.getAsset("cont1data.rlp"), resources.getAnimation("cont1"), "Weedy");
        addContestant((KeySession) resources.getAsset("cont2data.rlp"), resources.getAnimation("cont2"), "White Boy");
        addContestant((KeySession) resources.getAsset("cont3data.rlp"), resources.getAnimation("cont3"), "Black Guy");
    }

    @Override
    public String getLevelName() {
        return "Mountain Race";
    }

    @Override
    public Music getStageMusic() {
        return music;
    }

    @Override
    public void dispose() {
        resources.disposeAll();
    }

    private void addContestant(final KeySession data, final Image2D[] img, final String name) {
        final GravityMan cont = new GravityMan();
        cont.setImage(4, img);
        cont.setController(Controller.DEFAULT_CONTROLLER);
        cont.setJumpSound(resources.getSound("jump.wav"));
        cont.addEvent(cont::face);
        cont.setFacings(2);
        cont.move(10, 1659);
        cont.sounds.useFalloff = true;
        cont.setGhostData(data.keystrokes);
        add(cont);
        runOnceWhen(() -> {
            final HUDMessage winText = HUDMessage.centeredMessage(name + " completed!", getEngine().getScreenSize(), Color.BLACK);
            temp(Factory.drawCenteredText(winText, getEngine().timeFont), 120);
        }, () -> BaseLogic.rectanglesCollide(winArea, cont.bounds.toRectangle()));
    }
}
