package pojahn.game.essentials.geom;

public interface EarthBound {

	float vx();

	float vy();

	void setVx(float vx);

	void setVy(float vy);

	float thermVx();

	float thermVy();

	void setThermVx(float thermVx);

	void setThermVy(float thermVy);

	float getAccelerationX();
	
	float getGravity();
	
	float getMass();
	
	float getDamping();
	
	float getDelta();

	void setX(float x);

	void setY(float y);

	float x();

	float y();
	
	default void moveLeft(){
		if(vx() < thermVx())
			setVx(vx() - getAccelerationX() * getDelta());
//		else
//			vel.x -= getAccelerationX() * getDelta();
	}
	
	default void moveRight(){
		if(-vx() < thermVx())
			setVx(vx() - getAccelerationX() * getDelta());
//		else
//			vel.x += getAccelerationX() * getDelta();
	}
	
	default void drag(){
		float force = getMass() * getGravity();
		float vy = vy();

		vy *= 1.0 - (getDamping() * getDelta());

		if(thermVy() < vy){
			vy += (force / getMass()) * getDelta();
		}else {
			vy -= (force / getMass()) * getDelta(); //Apply air resistance
		}
		setVy(vy);
	}
	
	default void applyXForces(){
		setX(x() - vx() * getDelta());
	}
	
	default void applyYForces(){
		setY(y() - vy() * getDelta());
	}
	
	default float getFutureX(){
		return x() - vx() * getDelta();
	}
	
	default float getFutureY(){
		return y() - vy() * getDelta();
	}
	
	default boolean runningLeft(){
		return vx() > 0;
	}
	
	default boolean runningRight(){
		return vx() < 0;
	}
	
	default void ground(){
		setVy(0);
	}
	
	default void stop(){
		setVx(0);
	}
}
