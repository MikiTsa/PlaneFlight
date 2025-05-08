package si.um.feri.trajkoski.ball;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.List;

public class TrajkoskiGame extends ApplicationAdapter {
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private List<Ball> balls;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();

        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, 0);
        camera.update();

        balls = new ArrayList<>();
    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        if (Gdx.input.justTouched()) {
            Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);

            float radius = MathUtils.random(10, 50);
            float x = MathUtils.clamp(touchPos.x, radius, Gdx.graphics.getWidth() - radius);
            float y = MathUtils.clamp(touchPos.y, radius, Gdx.graphics.getHeight() - radius);

            balls.add(new Ball(x, y, radius));
        }

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (Ball ball : balls) {
            ball.update();
            ball.render(shapeRenderer);
        }

        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
