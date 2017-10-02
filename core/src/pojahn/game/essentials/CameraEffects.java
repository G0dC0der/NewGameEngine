package pojahn.game.essentials;

import pojahn.game.core.Entity;

public class CameraEffects {
    /**
     * Returns an {@code CameraEffect} event that moves vertically in a pingpong motion.<br>
     * The function {@code isDone()} returns true either when the duration have elapsed or the event is manually stopped with the {@code stop()} function.
     *
     * @param length The length of the vertical movement.
     * @param speed  The speed of the vertical movement.
     * @return The event.
     */
    public static Entity verticalMovement(final float length, final float speed) {
        return pingPongMovement(length, speed, 0);
    }

    /**
     * Returns an {@code CameraEffect} event that moves horizontally in a pingpong motion.
     * <br>
     * The function {@code isDone()} returns true either when the duration have elapsed or the event is manually stopped with the {@code stop()} function.
     *
     * @param length The length of the horizontal movement.
     * @param speed  The speed of the horizontal movement.
     * @return The event.
     */
    public static Entity horizontalMovement(final float length, final float speed) {
        return pingPongMovement(length, speed, 1);
    }

    /**
     * Returns an {@code CameraEffect} event that zooms in and out in a pingpong motion with the given speed.<br>
     * The zoom factor defaults to 1.0, which is 100%. Increasing the values zooms out rather than zooming in.<br>
     * The function {@code isDone()} returns true either when the duration have elapsed or the event is manually stopped with the {@code stop()} function.
     *
     * @param min   The minimum zoom.
     * @param max   The maximum zoom.
     * @param speed The speed of the pingpong motion.
     * @return The event.
     */
    public static Entity zoomEffect(final float min, final float max, final float speed) {
        if (0 > min || 0 > max || 0 > speed)
            throw new IllegalArgumentException("All values must be positive.");

        return new Entity() {

            float scaleValue;
            boolean increasingScale;

            @Override
            public void init() {
                scaleValue = getEngine().getZoom();
                zIndex(Integer.MAX_VALUE);
            }

            @Override
            public void logistics() {
                if (increasingScale) {
                    scaleValue += speed;
                    if (scaleValue > max)
                        increasingScale = false;
                } else {
                    scaleValue -= speed;
                    if (scaleValue < min)
                        increasingScale = true;
                }

                getEngine().setZoom(scaleValue);
            }
        };
    }

    /**
     * Vibrates the screen for the specified amount of frames.<br>
     * The function {@code isDone()} returns true either when the duration have elapsed or the event is manually stopped with the {@code stop()} function.
     *
     * @param strength The strength of the vibration.
     * @return The event.
     */
    public static Entity vibration(final float strength) {
        return new Entity() {
            int counter, counter2;

            {
                zIndex(Integer.MAX_VALUE);
            }

            @Override
            public void logistics() {
                if (++counter2 % 2 == 0) {
                    final int value = counter++ % 4;
                    float tx = getEngine().tx();
                    float ty = getEngine().ty();

                    switch (value) {
                        case 0:
                            tx += -strength;
                            ty += -strength;
                            break;
                        case 1:
                            tx += strength;
                            ty += -strength;
                            break;
                        case 2:
                            tx += strength;
                            ty += strength;
                            break;
                        case 3:
                            tx -= strength;
                            ty += strength;
                            break;
                    }
                    getEngine().translate(tx, ty);
                }
            }
        };
    }

    static Entity pingPongMovement(final float length, final float speed, final int axis) {
        if (0 > length || 0 > speed)
            throw new IllegalArgumentException("Both values must be positive.");

        return new Entity() {
            boolean increasingVert;
            float vertValue, vertLength, vertSpeed;

            {
                if (speed == 0)
                    vertValue = 0;

                vertLength = length;
                vertSpeed = speed;
                zIndex(Integer.MAX_VALUE);
            }

            @Override
            public void logistics() {
                if (vertSpeed > 0) {
                    if (increasingVert) {
                        vertValue += vertSpeed;
                        if (vertValue > vertLength)
                            increasingVert = false;
                    } else {
                        vertValue -= vertSpeed;
                        if (vertValue < -vertLength)
                            increasingVert = true;
                    }

                    float tx = getEngine().tx();
                    float ty = getEngine().ty();
                    if (axis == 0)
                        ty += vertValue;
                    else if (axis == 1)
                        tx += vertValue;

                    getEngine().translate(tx, ty);
                }
            }
        };
    }
}
