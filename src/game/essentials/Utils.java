package game.essentials;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import game.core.Entity;
import game.events.RenderEvent;

public class Utils {

	public static Entity wrap(RenderEvent renderEvent){
		return wrap(renderEvent, 0);
	}
	
	public static Entity wrap(RenderEvent renderEvent, int zIndex){
		return new Entity(){{
				this.zIndex(zIndex);
			}
			
			@Override
			public void render(SpriteBatch batch) {
				super.render(batch);
				renderEvent.eventHandling(batch);
			}
		};
	}
}
