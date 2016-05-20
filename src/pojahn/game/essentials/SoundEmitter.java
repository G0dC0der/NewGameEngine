package pojahn.game.essentials;

import pojahn.game.core.Collisions;
import pojahn.game.core.Entity;
import pojahn.game.events.Event;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Music.OnCompletionListener;

public class SoundEmitter {

    public float power, maxDistance, maxVolume;
    public boolean useFalloff;
    private final Entity emitter;

    public SoundEmitter(Entity emitter) {
        this.emitter = emitter;
        power = 20;
        maxDistance = 700;
        maxVolume = 1.0f;
    }

    public float calc() {
        if (!useFalloff)
            return maxVolume;

        return calc(Collisions.findClosest(emitter, emitter.getLevel().getSoundListeners()));
    }

    public float calc(Entity listener) {
        return calc(listener.x(), listener.y());
    }

    public float calc(float listenerX, float listenerY) {
        if (!useFalloff)
            return maxVolume;

        double distance = Collisions.distance(emitter.x(), emitter.y(), listenerX, listenerY);
        float candidate = (float) (power * Math.max((1 / Math.sqrt(distance)) - (1 / Math.sqrt(maxDistance)), 0));

        return Math.min(candidate, maxVolume);
    }

    public Event dynamicVolume(Music music) { //TODO: Test.... Don't know wtf this does.
        return () -> {
            Entity listener = Collisions.findClosest(emitter, emitter.getLevel().getSoundListeners());

            double distance = emitter.dist(listener);
            float candidate = (float) (power * Math.max((1 / Math.sqrt(distance)) - (1 / Math.sqrt(maxDistance)), 0));

            music.setVolume(Math.min(candidate, maxVolume));
        };
    }

    public static SoundEmitter quiteEmitter(Entity emitter) {
        SoundEmitter se = new SoundEmitter(emitter);
        se.maxDistance = 400;
        se.maxVolume = .6f;
        se.power = 10;
        se.useFalloff = true;
        return se;
    }

    public static SoundEmitter loudEmitter(Entity emitter) {
        SoundEmitter se = new SoundEmitter(emitter);
        se.maxDistance = 1300;
        se.power = 40;
        se.useFalloff = true;
        return se;
    }

    public static Event fade(Music music, float targetVolume, int duration, boolean stopWhenDone) {
        return new Event() {

            int fadeTime = duration;
            double startVolume = music.getVolume();
            boolean done;

            @Override
            public void eventHandling() {
                if (!done) {
                    fadeTime -= Gdx.graphics.getDeltaTime();
                    if (fadeTime < 0) {
                        fadeTime = 0;
                        done = true;
                        if (stopWhenDone)
                            music.stop();
                    } else {
                        double offset = (targetVolume - startVolume) * (1 - (fadeTime / (double) duration));
                        music.setVolume((float) (startVolume + offset));
                    }
                }
            }
        };
    }

    public static void setLoopPosition(Music music, float seconds) {
        music.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(Music music) {
                music.setPosition(seconds);
                if(!music.isPlaying())
                    music.play();
            }
        });
    }
}