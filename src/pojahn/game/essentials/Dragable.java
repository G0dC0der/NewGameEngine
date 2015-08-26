package pojahn.game.essentials;

public interface Dragable {

	void setVx(float vx);
	
	void setVy(float vy);
	
	void setX(float x);
	
	void setY(float y);
	
	float getVx();
	
	float getVy();
	
	float getX();
	
	float getY();
	
	float getGravity();
	
	float getMass();
	
	float getDamping();
	
	public default void pull(){ //TODO:
		
	}
	
	public default void run(boolean west){
		
	}
	
	public default void dragTowards(){
		
	}
	
}
