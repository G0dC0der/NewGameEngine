package pojahn.game.essentials;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import pojahn.game.core.Collisions;
import pojahn.game.core.Entity;
import pojahn.game.events.Event;

import java.util.List;

public class SoundEmitter {

    public float power, maxDistance, maxVolume;
    public boolean useFalloff, mute;
    private final Entity emitter;

    public SoundEmitter(Entity emitter) {
        this.emitter = emitter;
        power = 20;
        maxDistance = 700;
        maxVolume = 1.0f;
    }

    public void play(final Sound sound) {
        if (!mute && sound != null) {
            final Entity soundListener = getSoundListener();
            float volume = useFalloff ? calc(soundListener.x(), soundListener.y()) : maxVolume;
            if (volume > 0.0f) {
                sound.play(volume);
            }
        }
    }

    @Deprecated
    public float calc() {
        if (!useFalloff)
            return maxVolume;

        return calc(getSoundListener());
    }

    @Deprecated
    public float calc(Entity listener) {
        return calc(listener.x(), listener.y());
    }

    private float calc(float listenerX, float listenerY) {
        double distance = Collisions.distance(emitter.x(), emitter.y(), listenerX, listenerY);
        float candidate = (float) (power * Math.max((1 / Math.sqrt(distance)) - (1 / Math.sqrt(maxDistance)), 0));

        return Math.min(candidate, maxVolume);
    }

    public Event dynamicVolume(Music music) {
        return () -> {
            Entity listener = getSoundListener();

            double distance = emitter.dist(listener);
            float candidate = (float) (power * Math.max((1 / Math.sqrt(distance)) - (1 / Math.sqrt(maxDistance)), 0));

            music.setVolume(Math.min(candidate, maxVolume));
        };
    }

    private Entity getSoundListener() {
        List<? extends Entity> soundListeners = emitter.getLevel().getSoundListeners();
        if(soundListeners == null || soundListeners.isEmpty())
            soundListeners = emitter.getLevel().getMainCharacters();

        return Collisions.findClosest(emitter, soundListeners);
    }
}