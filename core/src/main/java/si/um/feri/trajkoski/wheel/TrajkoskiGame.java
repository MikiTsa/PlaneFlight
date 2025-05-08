package si.um.feri.trajkoski.wheel;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class TrajkoskiGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture wheelTexture;
    private Sprite wheelSprite;

    private float wheelX;
    private float wheelY;
    private float wheelSpeed;
    private float wheelRotation;
    private int direction = 1;

    @Override
    public void create() {
        batch = new SpriteBatch();
        wheelTexture = new Texture(Gdx.files.internal("images/hypnosys.png"));
        wheelSprite = new Sprite(wheelTexture);

        wheelX = 0;
        wheelY = wheelSprite.getHeight();
        wheelSpeed = 200f;
        wheelRotation = 0f;
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        wheelX += direction * wheelSpeed * deltaTime;
        wheelRotation += direction * -360 * deltaTime; // invert rotation by direction

        if (wheelX + wheelSprite.getWidth() > Gdx.graphics.getWidth() || wheelX < 0) {
            direction *= -1;
        }

        wheelSprite.setPosition(wheelX, wheelY - wheelSprite.getHeight());
        wheelSprite.setRotation(wheelRotation);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        wheelSprite.draw(batch);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        wheelTexture.dispose();
    }
}
