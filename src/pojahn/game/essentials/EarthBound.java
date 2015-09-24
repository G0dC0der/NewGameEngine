package pojahn.game.essentials;

import com.badlogic.gdx.math.Vector2;

public interface EarthBound {

	Vector2 getVelocity();
	
	Vector2 getThermalVelocity();
	
	Vector2 getPosition();
	
	float getAccelerationX();
	
	float getGravity();
	
	float getMass();
	
	float getDamping();
	
	float getDelta();
	
	public default void moveLeft(){
		Vector2 vel = getVelocity();
		if(vel.x < getThermalVelocity().x)
			vel.x += getAccelerationX() * getDelta();
//		else
//			vel.x -= getAccelerationX() * getDelta();
	}
	
	public default void moveRight(){
		Vector2 vel = getVelocity();
		if(-vel.x < getThermalVelocity().x)
			vel.x -= getAccelerationX() * getDelta();
//		else
//			vel.x += getAccelerationX() * getDelta();
	}
	
	public default void drag(){
		Vector2 vel = getVelocity();
		Vector2 tVel = getThermalVelocity();
		
		float force = getMass() * getGravity();
		vel.y *= 1.0 - (getDamping() * getDelta());

		if(tVel.y < vel.y){
			vel.y += (force / getMass()) * getDelta();
		}else
			vel.y -= (force / getMass()) * getDelta(); //Apply air resistance
	}
	
	public default void applyXForces(){
		Vector2 pos = getPosition();
		Vector2 vel = getVelocity();
		
		pos.x -= vel.x * getDelta();
	}
	
	public default void applyYForces(){
		Vector2 pos = getPosition();
		Vector2 vel = getVelocity();
		
		pos.y -= vel.y * getDelta();
	}
	
	public default float getFutureX(){
		return getPosition().x - getVelocity().x * getDelta();
	}
	
	public default float getFutureY(){
		return getPosition().y - getVelocity().y * getDelta();
	}
	
	public default boolean runningLeft(){
		return getVelocity().x > 0;
	}
	
	public default boolean runningRight(){
		return getVelocity().x < 0;
	}
	
	public default void ground(){
		getVelocity().y = 0;
	}
	
	public default void stop(){
		getVelocity().x = 0;
	}
}
