package pojahn.game.entities.mains;

public class Flipper extends GravityMan {

    private boolean flip;

    public void flip() {
        gravity = -gravity;
        wallGravity = -wallGravity;
        flip = !flip;
        flipY = !flipY;
        jumpStrength = -jumpStrength;
    }

    @Override
    public boolean canDown() {
        return flip ? super.canUp() : super.canDown();
    }

    @Override
    public boolean canUp() {
        return flip ? super.canDown() : super.canUp();
    }

    @Override
    protected void land() {
        if (flip) {
            super.tryUp(10);
        } else {
            super.tryDown(10);
        }
    }

    public boolean isFlipped() {
        return flip;
    }
}
