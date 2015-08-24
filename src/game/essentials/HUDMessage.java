package game.essentials;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface HUDMessage {

	String getMessage();
	
	float getX();
	
	float getY();
	
	default Color getColor(){
		return Color.WHITE;
	}
	
	default void draw(SpriteBatch batch, BitmapFont font){
		Color orgColor = font.getColor();
		font.setColor(getColor());
		font.draw(batch, getMessage(), getX(), getY());
		font.setColor(orgColor);
	}
	
	public static HUDMessage getMessage(String text, float x, float y, Color color){
		return new HUDMessage() {
			@Override
			public float getY() {
				return y;
			}
			
			@Override
			public float getX() {
				return x;
			}
			
			@Override
			public Color getColor() {
				return color == null ? Color.WHITE : color;
			}
			
			@Override
			public String getMessage() {
				return text;
			}
		};
	}
}
