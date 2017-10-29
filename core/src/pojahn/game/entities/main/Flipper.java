package pojahn.game.entities.main;

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

    @Override
    protected boolean landed() {
        return flip ? vel.y > 0 : super.landed();
    }

    @Override
    protected boolean launching() {
        return flip ? vel.y < 0 : super.launching();
    }

    public boolean isFlipped() {
        return flip;
    }
}
