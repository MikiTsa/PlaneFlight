package si.um.feri.trajkoski.oop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

public class Bullet extends DynamicGameObject implements Pool.Poolable {
    private boolean alive;

    public Bullet() {
        super(null, 0, 0, 0, 0);
        this.velocity = new Vector2(0, 500);
        this.alive = false;
    }

    public void init(TextureRegion texture, float x, float y) {
        this.texture = texture;
        this.position.set(x, y);
        this.bounds.set(x, y, texture.getRegionWidth(), texture.getRegionHeight());
        this.alive = true;
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        bounds.setPosition(position.x, position.y);
    }

    public boolean isOffScreen() {
        return position.y > Gdx.graphics.getHeight();
    }

    @Override
    public void reset() {
        position.set(0, 0);
        velocity.set(0, 500);
        alive = false;
    }

    public boolean isAlive() {
        return alive;
    }

    public static Bullet spawn(Pool<Bullet> bulletPool, TextureRegion texture, float x, float y) {
        Bullet bullet = bulletPool.obtain();
        bullet.init(texture, x, y);
        return bullet;
    }
}
