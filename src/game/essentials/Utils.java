package game.essentials;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

import game.core.Entity;
import game.events.Event;
import game.events.RenderEvent;

public class Utils {

	public static Entity wrap(RenderEvent renderEvent){
		return wrap(renderEvent, 0);
	}
	
	public static Entity wrap(RenderEvent renderEvent, int zIndex){
		return new Entity(){{
				this.zIndex(zIndex);
				this.id = "WRAPPER";
			}
			
			@Override
			public void render(SpriteBatch batch) {
				super.render(batch);
				renderEvent.eventHandling(batch);
			}
		};
	}
	
	public static Entity wrap(Event event){
		Entity entity = new Entity();
		entity.addEvent(event);
		entity.id = "WRAPPER";
		
		return entity;
	}
	
	public static <T> T getRandomElement(T[] array)
	{
		if(array.length == 0)
			return null;
		
		return array[MathUtils.random(array.length)];
	}
}
