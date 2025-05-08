package si.um.feri.trajkoski.oop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

public class Bird extends DynamicGameObject implements Pool.Poolable {
    private Plane plane;
    private boolean alive;

    public Bird() {
        super(null, 0, 0, 0, 0);
        this.velocity = new Vector2(0, -420);
        this.alive = false;
    }

    public void init(TextureRegion texture, float x, float y, Plane plane) {
        this.texture = texture;
        this.position.set(x, y);
        this.bounds.set(x, y, texture.getRegionWidth(), texture.getRegionHeight());
        this.plane = plane;
        this.alive = true;
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        if (position.x < plane.position.x) {
            velocity.x = 150;
        } else if (position.x > plane.position.x) {
            velocity.x = -150;
        }

        bounds.setPosition(position.x, position.y);
    }

    public boolean isOffScreen() {
        return position.y + bounds.height < 0;
    }

    @Override
    public void reset() {
        position.set(0, 0);
        velocity.set(0, -420);
        alive = false;
    }

    public boolean isAlive() {
        return alive;
    }

    public static Bird spawn(Pool<Bird> birdPool, TextureRegion texture, Plane plane) {
        Bird bird = birdPool.obtain();
        int width = Gdx.graphics.getWidth();
        int textureWidth = texture.getRegionWidth();
        if (width > textureWidth) {
            bird.init(texture, MathUtils.random(0, width - textureWidth), Gdx.graphics.getHeight(), plane);
        } else {
            bird.init(texture, 0, Gdx.graphics.getHeight(), plane);
        }
        return bird;
    }
}
