package pojahn.game.desktop.redguyruns.levels.training1;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.MathUtils;
import pojahn.game.core.BaseLogic;
import pojahn.game.core.Entity;
import pojahn.game.core.MobileEntity;
import pojahn.game.essentials.Direction;
import pojahn.game.essentials.Factory;
import pojahn.game.essentials.HUDMessage;
import pojahn.game.events.Event;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class Friend extends MobileEntity {

    private BitmapFont font;
    private Color textColor;
    private List<Sound> talkingSounds;
    private List<Entity> subjects;
    private String text;
    private float offsetX, offsetY;
    private int talkRecovery, recoveryCounter, talkDuration;
    private Event talkEvent;

    public Friend() {
        talkRecovery = 300;
        talkDuration = 200;
        textColor = Color.WHITE;
    }

    @Override
    public void init() {
        super.init();

        if (subjects == null || subjects.isEmpty()) {
            subjects = getLevel()
                .getMainCharacters()
                .stream()
                .map(Entity.class::cast)
                .collect(toList());
        }
    }

    @Override
    public void logistics() {
        recoveryCounter--;

        subjects.stream()
            .filter(this::collidesWith)
            .findFirst()
            .ifPresent(entity -> talk());

        final Entity closest = BaseLogic.findClosest(this, subjects);
        face(BaseLogic.leftMost(this, closest) == this ? Direction.E : Direction.W);
    }

    private void talk() {
        if (recoveryCounter > 0)
            return;

        recoveryCounter = talkRecovery;
        getLevel().temp(Factory.drawText(HUDMessage.getMessage(text, x() + offsetX, y() + offsetY, textColor), font), talkDuration);

        if (talkingSounds != null && !talkingSounds.isEmpty())
            sounds.play(talkingSounds.get(MathUtils.random(0, talkingSounds.size() - 1)));

        if (talkEvent != null)
            talkEvent.eventHandling();
    }

    public void setFont(final BitmapFont font) {
        this.font = font;
    }

    public void setTextColor(final Color textColor) {
        this.textColor = textColor;
    }

    public void setTalkingSounds(final List<Sound> talkingSounds) {
        this.talkingSounds = talkingSounds;
    }

    public void setSubjects(final List<Entity> subjects) {
        this.subjects = subjects;
    }

    public void setText(final String text) {
        this.text = text;
    }

    public void setTalkDuration(final int talkDuration) {
        this.talkDuration = talkDuration;
    }

    public void setOffsetX(final float offsetX) {
        this.offsetX = offsetX;
    }

    public void setOffsetY(final float offsetY) {
        this.offsetY = offsetY;
    }

    public void setTalkEvent(final Event talkEvent) {
        this.talkEvent = talkEvent;
    }
}
