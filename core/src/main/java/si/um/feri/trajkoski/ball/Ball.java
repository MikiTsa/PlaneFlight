package si.um.feri.trajkoski.ball;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.Gdx;

public class Ball {
    private Vector2 position;
    private Vector2 velocity;
    private float radius;
    private float gravity = -500f;
    private float restitution = 0.55f;
    private float groundHeight = 0;
    private float colorR, colorG, colorB;

    public Ball(float x, float y, float radius) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(0, 0);
        this.radius = radius;
        this.colorR = MathUtils.random();
        this.colorG = MathUtils.random();
        this.colorB = MathUtils.random();
    }

    public void update() {
        velocity.y += gravity * Gdx.graphics.getDeltaTime();
        position.add(velocity.x * Gdx.graphics.getDeltaTime(), velocity.y * Gdx.graphics.getDeltaTime());

        if (position.y - radius <= groundHeight) {
            position.y = radius;
            velocity.y *= -restitution;
        }

        if (position.x - radius < 0) {
            position.x = radius;
            velocity.x = -velocity.x * restitution;
        } else if (position.x + radius > Gdx.graphics.getWidth()) {
            position.x = Gdx.graphics.getWidth() - radius;
            velocity.x = -velocity.x * restitution;
        }
    }

    public void render(ShapeRenderer renderer) {
        renderer.setColor(colorR, colorG, colorB, 1);
        renderer.circle(position.x, position.y, radius);
    }
}
