package pojahn.game.events;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

@FunctionalInterface
public interface RenderEvent {

    void eventHandling(SpriteBatch batch);
}
