package si.um.feri.trajkoski.oop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Pool;

public class PowerUp extends DynamicGameObject implements Pool.Poolable {
    private boolean alive;

    public PowerUp() {
        super(null, 0, 0, 0, 0);
        this.velocity.y = -200;
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
        return position.y + bounds.height < 0;
    }

    @Override
    public void reset() {
        position.set(0, 0);
        velocity.y = -200;
        alive = false;
    }

    public boolean isAlive() {
        return alive;
    }

    public static PowerUp spawn(Pool<PowerUp> powerUpPool, TextureRegion texture) {
        PowerUp powerUp = powerUpPool.obtain();
        powerUp.init(texture, MathUtils.random(0, Gdx.graphics.getWidth() - texture.getRegionWidth()), Gdx.graphics.getHeight());
        return powerUp;
    }
}
