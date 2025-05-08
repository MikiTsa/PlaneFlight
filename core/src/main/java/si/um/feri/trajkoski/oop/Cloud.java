package si.um.feri.trajkoski.oop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Pool;

public class Cloud extends DynamicGameObject implements Pool.Poolable {
    private boolean alive;

    public Cloud() {
        super(null, 0, 0, 0, 0);
        this.alive = false;
    }

    public void init(TextureRegion texture, float x, float y) {
        this.texture = texture;
        this.position.set(x, y);
        this.bounds.set(x, y, texture.getRegionWidth(), texture.getRegionHeight());
        this.velocity.y = -170;
        this.alive = true;
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        bounds.setPosition(position.x, position.y);
    }

    public boolean isOffScreen() {
        return position.y + bounds.height < 0;
    }

    @Override
    public void reset() {
        position.set(0, 0);
        velocity.y = -170;
        alive = false;
    }

    public boolean isAlive() {
        return alive;
    }

    public static Cloud spawn(Pool<Cloud> cloudPool, TextureRegion texture) {
        Cloud cloud = cloudPool.obtain();
        int width = Gdx.graphics.getWidth();
        int textureWidth = texture.getRegionWidth();
        if (width > textureWidth) {
            cloud.init(texture, MathUtils.random(0, width - textureWidth), Gdx.graphics.getHeight());
        } else {
            cloud.init(texture, 0, Gdx.graphics.getHeight());
        }
        return cloud;
    }
}
