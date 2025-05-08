package si.um.feri.trajkoski.oop;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public abstract class GameObject {
    protected Vector2 position;
    protected Rectangle bounds;
    protected TextureRegion texture;

    public GameObject(TextureRegion texture, float x, float y, float width, float height) {
        this.texture = texture;
        this.position = new Vector2(x, y);
        this.bounds = new Rectangle(x, y, width, height);
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y);
    }

    public void update(float deltaTime) {
        bounds.setPosition(position.x, position.y);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }

    public float getWidth() {
        return texture.getRegionWidth();
    }

    public float getHeight() {
        return texture.getRegionHeight();
    }

    public void setPosition(float x, float y) {
        position.set(x, y);
        bounds.setPosition(x - bounds.width / 2, y - bounds.height / 2);
    }
}
