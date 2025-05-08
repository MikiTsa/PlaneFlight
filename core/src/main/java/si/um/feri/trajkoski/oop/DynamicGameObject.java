package si.um.feri.trajkoski.oop;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public abstract class DynamicGameObject extends GameObject {
    protected Vector2 velocity;
    protected final Vector2 acceleration;

    public DynamicGameObject(TextureRegion texture, float x, float y, float width, float height) {
        super(texture, x, y, width, height);
        this.velocity = new Vector2();
        this.acceleration = new Vector2();
    }

    @Override
    public void update(float deltaTime) {
        velocity.add(acceleration.x * deltaTime, acceleration.y * deltaTime);
        position.add(velocity.x * deltaTime, velocity.y * deltaTime);
        bounds.setPosition(position.x, position.y);
    }
}
