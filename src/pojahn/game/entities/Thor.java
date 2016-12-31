package pojahn.game.entities;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.Collisions;
import pojahn.game.core.Entity;
import pojahn.game.essentials.Utils;

public class Thor extends PathDrone {

    private float displace, detail, precision, thickness, offsetX, offsetY;
    private int bolts;
    private boolean blend, fire;
    private Entity[] targets;
    private Color[] boltColors;
    private Music thunderSound;

    public Thor(float x, float y, Entity... targets) {
        super(x, y);
        this.targets = targets;
        displace = 100;
        detail = 2;
        thickness = 2.5f;
        precision = 0;
        bolts = 1;
        blend = true;
        fire = true;
        boltColors = new Color[]{ Color.BLUE };
    }

    /**
     * The firing offset of the bolts.
     */
    public void setFiringOffset(float x, float y) {
        offsetX = x;
        offsetY = y;
    }

    /**
     * Whether or not to fire. Setting this flag to false stops the bolts.
     */
    public void setFire(boolean fire) {
        this.fire = fire;
    }

    /**
     * Returns true if this Thor is in a firing state.
     */
    public boolean isFiring() {
        return fire;
    }

    /**
     * The music to loop while firing the bolts.
     */
    public void thunderSound(Music thunderSound) {
        this.thunderSound = thunderSound;
    }

    /**
     * The amount of pixels to travel outside the given line.
     */
    public void setDisplaceAmount(float displace) {
        this.displace = displace;
    }

    /**
     * The jagged level of the line. Lower means more. Something really high will result in a straight line.
     */
    public void setDetailLevel(float detail) {
        this.detail = detail;
    }

    /**
     * The thickness of a bolt.
     */
    public void setThickness(float thickness) {
        this.thickness = thickness;
    }

    /**
     * The precision of the bolt. 0 means 100% accurate.
     */
    public void setPrecisionLevel(float precision) {
        this.precision = precision;
    }

    /**
     * The number of bolts
     */
    public void setBolts(int bolts) {
        this.bolts = bolts;
    }

    /**
     * Whether or not to blend the bolts.
     */
    public void setBlend(boolean blend) {
        this.blend = blend;
    }

    /**
     * The colors of the bolts.
     */
    public void setBoltColors(Color... boltColors) {
        this.boltColors = boltColors;
    }

    @Override
    public void dispose() {
        super.dispose();

        if (thunderSound != null)
            thunderSound.stop();
    }

    @Override
    public void logistics() {
        super.logistics();

        if (thunderSound != null) {
            if (fire) {
                if (sounds.useFalloff) {
                    thunderSound.setVolume(sounds.calc());
                }

                if (!thunderSound.isPlaying()) {
                    thunderSound.setLooping(true);
                    thunderSound.play();
                }
            } else if (thunderSound.isPlaying()){
                thunderSound.stop();
            }
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        super.render(batch);

        if (!fire)
            return;

        Entity target = Collisions.findClosest(this, targets);

        drawLightning(
                batch,
                x() + offsetX,
                y() + offsetY,
                target.centerX(),
                target.centerY(),
                displace,
                detail,
                thickness,
                precision,
                bolts,
                blend,
                getImage().getObject(),
                boltColors);
    }

    private static void drawLightning(SpriteBatch batch, float x1, float y1, float x2, float y2, float displace, float detail, float thickness, float noise, int numberOfBolts, boolean blend, Texture dot, Color... colors) {
        Color orgColor = batch.getColor();
        if (blend)
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

        for (int i = 0; i < numberOfBolts; i++) {
            batch.setColor(Utils.getRandomElement(colors));
            drawSingleP2PLightning(batch, x1, y1, x2 + MathUtils.random(-noise, noise), y2 + MathUtils.random(-noise, noise), displace, detail, thickness, dot);
        }

        batch.setColor(orgColor);
        if (blend)
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private static void drawLine(SpriteBatch batch, float _x1, float _y1, float _x2, float _y2, float thickness, Texture dot) {
        float length = (float) Collisions.distance(_x1, _y1, _x2, _y2);
        float dx = _x1;
        float dy = _y1;
        dx = dx - _x2;
        dy = dy - _y2;
        float angle = MathUtils.radiansToDegrees * MathUtils.atan2(dy, dx);
        angle = angle - 180;

        batch.draw(dot, _x1, _y1, 0f, thickness * 0.5f, length, thickness, 1f, 1f, angle, 0, 0, dot.getWidth(), dot.getHeight(), false, false);
    }

    private static void drawSingleP2PLightning(SpriteBatch batch, float x1, float y1, float x2, float y2, float displace, float detail, float thickness, Texture dot) {
        if (displace < detail)
            drawLine(batch, x1, y1, x2, y2, thickness, dot);
        else {
            float mid_x = (x2 + x1) * 0.5f;
            float mid_y = (y2 + y1) * 0.5f;
            mid_x += (Math.random() - 0.5f) * displace;
            mid_y += (Math.random() - 0.5f) * displace;
            drawSingleP2PLightning(batch, x1, y1, mid_x, mid_y, displace * 0.5f, detail, thickness, dot);
            drawSingleP2PLightning(batch, x2, y2, mid_x, mid_y, displace * 0.5f, detail, thickness, dot);
        }
    }
}
