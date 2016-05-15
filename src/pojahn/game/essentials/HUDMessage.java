package pojahn.game.essentials;

import java.awt.Dimension;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
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
	
	public static HUDMessage centeredMessage(String text, Dimension screenSize, Color color){
		return new HUDMessage() {
			
			float x = -1, y = -1;

			@Override
			public float getY() {
				return y;
			}
			
			@Override
			public float getX() {
				return x;
			}
			
			@Override
			public String getMessage() {
				return text;
			}
			
			@Override
			public Color getColor() {
				return color == null ? Color.WHITE : color;
			}
			
			@Override
			public void draw(SpriteBatch batch, BitmapFont font) {
				if(x == -1 || y == -1)
					calc(font);
				
				HUDMessage.super.draw(batch, font);
			}
			
			void calc(BitmapFont font){
				GlyphLayout layout = new GlyphLayout();
				layout.setText(font, getMessage());
				float width = layout.width;
				float height = layout.height;
				
				x = (screenSize.width / 2) - (width / 2);
				y = (screenSize.height / 2) - (height / 2);
			}
		};
	}
}
