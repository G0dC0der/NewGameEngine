package game.essentials;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Bounds{

	public final Vector2 pos;
	public final Size size;
	public float rotation;
	
	public Bounds(){
		pos = new Vector2();
		size = new Size();
	}
	
	public Vector2 center(){
		return new Vector2(pos.x + size.width / 2, pos.y + size.height / 2);
	}
	
	public Vector2 centerRelative(){
		return new Vector2(size.width / 2, size.height / 2);
	}
	
	public void set(Bounds bounds){
		pos.set(bounds.pos);
		rotation = bounds.rotation;
		size.width = bounds.size.width;
		size.height = bounds.size.height;
	}
	
	public Rectangle toRectangle(){
		return new Rectangle(pos.x, pos.y, size.width, size.height);
	}
	
	public Circle toCircle(){
		if(size.width != size.height)
			throw new RuntimeException("Aborting toCircle because the outcome would be an oval.");
		
		return new Circle(center(), size.width / 2);
	}
}