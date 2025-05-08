package si.um.feri.trajkoski.oop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Pool;

public class Plane extends DynamicGameObject {

    public Plane(TextureRegion texture, float x, float y, float width, float height) {
        super(texture, x, y, width, height);
    }

    public void moveLeft(float speed, float deltaTime) {
        velocity.x = -speed;
        update(deltaTime);
        if (position.x < 0) {
            position.x = 0;
        }
    }

    public void moveRight(float speed, float deltaTime) {
        velocity.x = speed;
        update(deltaTime);
        if (position.x > Gdx.graphics.getWidth() - bounds.width) {
            position.x = Gdx.graphics.getWidth() - bounds.width;
        }
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        bounds.setPosition(position.x, position.y);
    }

    public Bullet shoot(Pool<Bullet> bulletPool, TextureRegion bulletTexture) {
        float bulletX = position.x + bounds.width / 2 - bulletTexture.getRegionWidth() / 2;
        float bulletY = position.y + bounds.height;

        Bullet bullet = bulletPool.obtain();
        bullet.init(bulletTexture, bulletX, bulletY);
        return bullet;
    }

}
