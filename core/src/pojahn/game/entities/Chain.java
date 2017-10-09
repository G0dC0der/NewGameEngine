package pojahn.game.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.BaseLogic;
import pojahn.game.core.Entity;
import pojahn.game.essentials.Image2D;

public class Chain extends Entity {

    private Vector2 pt1, pt2;
    private Entity src1, src2;
    private int links;
    private boolean rotate, linkOnEndpoint;

    public Chain(final int links) {
        this.links = links;
        linkOnEndpoint = true;
    }

    public void setLinks(final int links) {
        this.links = links;
    }

    public void linkOnEndpoint(final boolean linkOnEndpoint) {
        this.linkOnEndpoint = linkOnEndpoint;
    }

    public void rotateLinks(final boolean rotate) {
        this.rotate = rotate;
    }

    public void endPoint1(final Entity src1) {
        this.src1 = src1;
    }

    public void endPoint2(final Entity src2) {
        this.src2 = src2;
    }

    public void endPoint1(final Vector2 pt1) {
        this.pt1 = pt1;
    }

    public void endPoint2(final Vector2 pt2) {
        this.pt2 = pt2;
    }

    @Override
    public final void render(final SpriteBatch batch) {
        final Vector2 endPoint1 = src1 == null ? pt1 : new Vector2(src1.centerX() - halfWidth(), src1.centerY() - halfHeight());
        final Vector2 endPoint2 = src2 == null ? pt2 : new Vector2(src2.centerX() - halfWidth(), src2.centerY() - halfHeight());
        final int start = (linkOnEndpoint) ? 0 : 1;
        final int end = (linkOnEndpoint) ? links : links + 2;
        final int cond = (linkOnEndpoint) ? links : end - 1;
        final Image2D img = nextImage();

        if (rotate)
            bounds.rotation = (float) BaseLogic.getAngle(endPoint1.x, endPoint1.y, endPoint2.x, endPoint2.y);

        for (int i = start; i < cond; i++) {
            final Vector2 linkPos = new Vector2(endPoint1).lerp(endPoint2, (float) i / (float) (end - 1));
            move(linkPos.x, linkPos.y);
            this.basicRender(batch, img);
        }
    }
}