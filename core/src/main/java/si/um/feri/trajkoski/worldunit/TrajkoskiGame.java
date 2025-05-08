package si.um.feri.trajkoski.worldunit;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Iterator;

public class TrajkoskiGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Viewport viewport;

    private Texture planeImg;
    private Texture fuelcanImg;
    private Texture cloudImg;
    private Texture backgroundImg;
    private Texture birdImg;
    private Texture bulletImg;

    private BitmapFont font;
    private BitmapFont gameOverfont;

    private Music planeFlying;
    private Sound cloudCollisionalert;
    private Sound fuelcanCollect;

    private Rectangle plane;
    private Rectangle resetButton;

    private Array<Rectangle> fuelcans;
    private Array<Rectangle> clouds;
    private Array<Rectangle> birds;
    private Array<Rectangle> bullets;

    private int fuel;
    private float lastFuelDecreaseTime;
    private float fuelcanSpawnTime;
    private float cloudSpawnTime;
    private float birdSpawnTime;
    private int birdsHit = 0;
    private boolean spacePressed = false;

    private static final float WORLD_WIDTH = 800f;
    private static final float WORLD_HEIGHT = 480f;

    private static final float PLANE_SPEED = 300f;
    private static final float FUELCAN_SPEED = 120f;
    private static final float FUELCAN_REGEN = 10f;
    private static final float CLOUD_SPEED = 170f;
    private static final float CLOUD_DAMAGE = 15f;
    private static final float BULLET_SPEED = 500f;
    private static final float BIRD_SPEED = 420f;
    private static final float BIRD_DAMAGE = 30f;
    private static final float FUEL_DECREASE_INTERVAL = 1.25f;
    private static final float CLOUD_SPAWN_TIME = 2f;
    private static final float FUELCAN_SPAWN_TIME = 4f;
    private static final float BIRD_SPAWN_TIME = 3f;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);

        batch = new SpriteBatch();

        planeImg = new Texture("images/plane.png");
        fuelcanImg = new Texture("images/fuelcan.png");
        cloudImg = new Texture("images/cloud.png");
        backgroundImg = new Texture("images/background.jpg");
        birdImg = new Texture("images/bird.png");
        bulletImg = new Texture("images/bullet.png");

        planeFlying = Gdx.audio.newMusic(Gdx.files.internal("sounds/planeFlying.wav"));
        fuelcanCollect = Gdx.audio.newSound(Gdx.files.internal("sounds/fuelcanCollect.wav"));
        cloudCollisionalert = Gdx.audio.newSound(Gdx.files.internal("sounds/cloudCollisionAlert.wav"));

        font = new BitmapFont(Gdx.files.internal("fonts/arial-32.fnt"));
        gameOverfont = new BitmapFont(Gdx.files.internal("fonts/pixelify.fnt"));
        gameOverfont.getData().setScale(1.75f);

        planeFlying.setLooping(true);
        planeFlying.setVolume(0.3f);
        planeFlying.play();

        plane = new Rectangle(WORLD_WIDTH / 2f - planeImg.getWidth() / 2f, 20f, planeImg.getWidth(), planeImg.getHeight());

        fuelcans = new Array<>();
        clouds = new Array<>();
        birds = new Array<>();
        bullets = new Array<>();
        fuel = 100;

        resetButton = new Rectangle((WORLD_WIDTH - 200) / 2f, WORLD_HEIGHT / 2f - 100, 200, 50);

        birdSpawnTime = TimeUtils.nanosToMillis(TimeUtils.nanoTime()) / 1000f;
        spawnFuelCan();
        spawnCloud();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true); // updates the viewport while keeping the camera centered
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 1);

        if (fuel > 0) {
            handleInput();
            update(Gdx.graphics.getDeltaTime());
        } else {
            if (Gdx.input.justTouched()) {
                Vector2 touch = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
                if (resetButton.contains(touch.x, touch.y)) {
                    resetGame();
                }
            }
        }

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.draw(backgroundImg, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        draw();
        batch.end();
    }

    private void handleInput() {
        float delta = Gdx.graphics.getDeltaTime();
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) moveLeft(delta);
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) moveRight(delta);
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            if (!spacePressed) {
                shoot();
                spacePressed = true;
            }
        } else {
            spacePressed = false;
        }
    }

    private void update(float delta) {
        if (fuel < 0) fuel = 0;

        float elapsedTime = TimeUtils.nanosToMillis(TimeUtils.nanoTime()) / 1000f;
        if (elapsedTime - fuelcanSpawnTime > FUELCAN_SPAWN_TIME) spawnFuelCan();
        if (elapsedTime - cloudSpawnTime > CLOUD_SPAWN_TIME) spawnCloud();
        if (elapsedTime - lastFuelDecreaseTime >= FUEL_DECREASE_INTERVAL) {
            fuel -= 2;
            lastFuelDecreaseTime = elapsedTime;
        }
        if (elapsedTime - birdSpawnTime > BIRD_SPAWN_TIME) {
            spawnBird();
            birdSpawnTime = elapsedTime;
        }

        for (Iterator<Rectangle> it = fuelcans.iterator(); it.hasNext(); ) {
            Rectangle fuelcan = it.next();
            fuelcan.y -= FUELCAN_SPEED * delta;
            if (fuelcan.y + fuelcanImg.getHeight() < 0) it.remove();
            if (fuelcan.overlaps(plane)) {
                fuelcanCollect.play();
                fuel += FUELCAN_REGEN;
                if (fuel > 100) fuel = 100;
                it.remove();
            }
        }

        for (Iterator<Rectangle> it = clouds.iterator(); it.hasNext(); ) {
            Rectangle cloud = it.next();
            cloud.y -= CLOUD_SPEED * delta;
            if (cloud.y + cloudImg.getHeight() < 0) it.remove();
            if (cloud.overlaps(plane)) {
                fuel -= CLOUD_DAMAGE;
                cloudCollisionalert.play();
                it.remove();
            }
        }

        for (Iterator<Rectangle> it = birds.iterator(); it.hasNext(); ) {
            Rectangle bird = it.next();
            bird.y -= BIRD_SPEED * delta;
            if (bird.overlaps(plane)) {
                fuel -= BIRD_DAMAGE;
                it.remove();
            }
        }

        for (Iterator<Rectangle> it = bullets.iterator(); it.hasNext(); ) {
            Rectangle bullet = it.next();
            bullet.y += BULLET_SPEED * delta;
            if (bullet.y > WORLD_HEIGHT) it.remove();
            for (Iterator<Rectangle> birdIt = birds.iterator(); birdIt.hasNext(); ) {
                Rectangle bird = birdIt.next();
                if (bullet.overlaps(bird)) {
                    birdsHit++;
                    birdIt.remove();
                    it.remove();
                    break;
                }
            }
        }
    }

    private void draw() {
        if (fuel <= 0) {
            planeFlying.stop();

            gameOverfont.setColor(Color.RED);
            String gameOverText = "GAME OVER";
            GlyphLayout layout = new GlyphLayout();
            layout.setText(gameOverfont, gameOverText);

            float gameOverX = (WORLD_WIDTH - layout.width) / 2f;
            float gameOverY = (WORLD_HEIGHT + layout.height) / 2f;
            gameOverfont.draw(batch, gameOverText, gameOverX, gameOverY);

            font.getData().setScale(0.6f);
            String restartText = "RESTART";
            GlyphLayout restartLayout = new GlyphLayout();
            restartLayout.setText(font, restartText);

            resetButton.width = restartLayout.width + 20;
            resetButton.height = restartLayout.height + 10;
            resetButton.x = (WORLD_WIDTH - resetButton.width) / 2f;
            resetButton.y = gameOverY - resetButton.height - 55;

            batch.setColor(new Color(1f, 1f, 1f, 0.5f));
            batch.draw(new Texture("images/blank.png"), resetButton.x, resetButton.y, resetButton.width, resetButton.height);
            batch.setColor(Color.NAVY);

            font.setColor(Color.WHITE);
            font.draw(batch, restartText, resetButton.x + (resetButton.width - restartLayout.width) / 2f, resetButton.y + (resetButton.height + restartLayout.height) / 2f);
            return;
        }

        for (Rectangle fuelcan : fuelcans) batch.draw(fuelcanImg, fuelcan.x, fuelcan.y);
        for (Rectangle cloud : clouds) batch.draw(cloudImg, cloud.x, cloud.y);
        for (Rectangle bird : birds) batch.draw(birdImg, bird.x, bird.y);
        for (Rectangle bullet : bullets) batch.draw(bulletImg, bullet.x, bullet.y);
        batch.draw(planeImg, plane.x, plane.y);

        batch.setColor(new Color(1f, 1f, 1f, 0.6f));
        batch.draw(new Texture("images/blank.png"), 10f, WORLD_HEIGHT - 75f, 200f, 60f);
        batch.setColor(Color.WHITE);

        font.getData().setScale(0.75f);
        font.setColor(Color.RED);
        font.draw(batch, "FUEL: " + fuel, 20f, WORLD_HEIGHT - 20f);
        font.setColor(Color.OLIVE);
        font.draw(batch, "BIRDS HIT: " + birdsHit, 20f, WORLD_HEIGHT - 50f);
    }

    private void moveLeft(float delta) {
        plane.x -= PLANE_SPEED * delta;
        if (plane.x < 0) plane.x = 0f;
    }

    private void moveRight(float delta) {
        plane.x += PLANE_SPEED * delta;
        if (plane.x > WORLD_WIDTH - planeImg.getWidth()) plane.x = WORLD_WIDTH - planeImg.getWidth();
    }

    private void spawnFuelCan() {
        Rectangle fuelcan = new Rectangle();
        fuelcan.x = MathUtils.random(0f, WORLD_WIDTH - fuelcanImg.getWidth());
        fuelcan.y = WORLD_HEIGHT;
        fuelcan.width = fuelcanImg.getWidth();
        fuelcan.height = fuelcanImg.getHeight();
        fuelcans.add(fuelcan);
        fuelcanSpawnTime = TimeUtils.nanosToMillis(TimeUtils.nanoTime()) / 1000f;
    }

    private void spawnCloud() {
        Rectangle cloud = new Rectangle();
        cloud.x = MathUtils.random(0f, WORLD_WIDTH - cloudImg.getWidth());
        cloud.y = WORLD_HEIGHT;
        cloud.width = cloudImg.getWidth();
        cloud.height = cloudImg.getHeight();
        clouds.add(cloud);
        cloudSpawnTime = TimeUtils.nanosToMillis(TimeUtils.nanoTime()) / 1000f;
    }

    private void spawnBird() {
        Rectangle bird = new Rectangle();
        bird.x = MathUtils.random(0f, WORLD_WIDTH - birdImg.getWidth());
        bird.y = WORLD_HEIGHT;
        bird.width = birdImg.getWidth();
        bird.height = birdImg.getHeight();
        birds.add(bird);
    }

    private void resetGame() {
        fuel = 100;
        birdsHit = 0;
        birdSpawnTime = TimeUtils.nanosToMillis(TimeUtils.nanoTime()) / 1000f;
        batch.setColor(Color.WHITE);
        planeFlying.play();
        font.getData().setScale(1f);

        fuelcans.clear();
        clouds.clear();
        spawnFuelCan();
        spawnCloud();

        plane.x = WORLD_WIDTH / 2f - planeImg.getWidth() / 2f;
        plane.y = 20f;
    }

    private void shoot() {
        Rectangle bullet = new Rectangle();
        bullet.x = plane.x + plane.width / 2 - bulletImg.getWidth() / 2;
        bullet.y = plane.y + plane.height;
        bullet.width = bulletImg.getWidth();
        bullet.height = bulletImg.getHeight();
        bullets.add(bullet);
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        planeImg.dispose();
        fuelcanImg.dispose();
        cloudImg.dispose();
        planeFlying.dispose();
        cloudCollisionalert.dispose();
        fuelcanCollect.dispose();
    }
}
