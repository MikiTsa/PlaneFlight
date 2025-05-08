package si.um.feri.trajkoski.oop;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

import java.util.Iterator;

import si.um.feri.trajkoski.oop.assets.AssetDescriptors;
import si.um.feri.trajkoski.oop.assets.AssetManager;
import si.um.feri.trajkoski.oop.assets.AssetPaths;
import si.um.feri.trajkoski.oop.assets.RegionNames;

public class TrajkoskiGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private Plane plane;

    private Pool<FuelCan> fuelCanPool;
    private Pool<Cloud> cloudPool;
    private Pool<Bird> birdPool;
    private Pool<Bullet> bulletPool;
    private Pool<PowerUp> powerUpPool;
    private Array<FuelCan> fuelCans;
    private Array<Cloud> clouds;
    private Array<Bird> birds;
    private Array<Bullet> bullets;
    private Array<PowerUp> powerUps;

    private TextureAtlas gameplayAtlas;

    private TextureRegion planeTexture;
    private TextureRegion fuelCanTexture;
    private TextureRegion cloudTexture;
    private TextureRegion birdTexture;
    private TextureRegion bulletTexture;
    private TextureRegion backgroundImg;
    private TextureRegion powerUpTexture;
    private Rectangle resetButton;

    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;

    private ParticleEffect planeEffect;
    private ParticleEffect bulletEffect;

    private int fuel = 100;
    private int birdsHit = 0;

    private BitmapFont font;
    private BitmapFont gameOverFont;
    private boolean isGameOver = false;
    private boolean spacePressed = false;
    private boolean powerUpActive = false;
    private boolean isPaused = false;
    private boolean debugMode = false;

    private float cloudSpawnTimer = CLOUD_SPAWN_TIME;
    private float fuelCanSpawnTimer = FUELCAN_SPAWN_TIME;
    private float birdSpawnTimer;
    private float fuelDecreaseTimer;
    private float powerUpTimer = 0;

    private static final float FUEL_DECREASE_INTERVAL = 1.25f;
    private static final float CLOUD_SPAWN_TIME = 2f;
    private static final float FUELCAN_SPAWN_TIME = 4f;
    private static final float BIRD_SPAWN_TIME = 3f;
    private static final float POWER_UP_DURATION = 10f;

    @Override
    public void create() {
        batch = new SpriteBatch();

        AssetManager.getInstance().loadAllAssets();
        gameplayAtlas = AssetManager.getInstance().get(AssetDescriptors.GAMEPLAY.fileName, TextureAtlas.class);

        planeTexture = gameplayAtlas.findRegion(RegionNames.PLANE);
        fuelCanTexture = gameplayAtlas.findRegion(RegionNames.FUELCAN);
        cloudTexture = gameplayAtlas.findRegion(RegionNames.CLOUD);
        birdTexture = gameplayAtlas.findRegion(RegionNames.BIRD);
        bulletTexture = gameplayAtlas.findRegion(RegionNames.BULLET);
        backgroundImg = gameplayAtlas.findRegion(RegionNames.BACKGROUND);
        powerUpTexture = gameplayAtlas.findRegion(RegionNames.POWERUP);

        plane = new Plane(planeTexture, Gdx.graphics.getWidth() / 2f, 20f, planeTexture.getRegionWidth(), planeTexture.getRegionHeight());

        fuelCans = new Array<>();
        clouds = new Array<>();
        birds = new Array<>();
        bullets = new Array<>();
        powerUps = new Array<>();

        fuelCanPool = Pools.get(FuelCan.class);
        cloudPool = Pools.get(Cloud.class);
        birdPool = Pools.get(Bird.class);
        bulletPool = Pools.get(Bullet.class);
        powerUpPool = Pools.get(PowerUp.class);

        resetButton = new Rectangle();
        resetButton.width = 200;
        resetButton.height = 50;
        resetButton.x = (Gdx.graphics.getWidth() - resetButton.width) / 2f;
        resetButton.y = Gdx.graphics.getHeight() / 2f - 100;

        font = new BitmapFont(Gdx.files.internal(AssetPaths.FONT_ARIAL_32));
        gameOverFont = new BitmapFont(Gdx.files.internal(AssetPaths.FONT_PIXELIFY));

        shapeRenderer = new ShapeRenderer();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        planeEffect = new ParticleEffect();
        planeEffect.load(Gdx.files.internal(AssetPaths.PARTICLE1), Gdx.files.internal("particles"));
        planeEffect.start();

        bulletEffect = new ParticleEffect();
        bulletEffect.load(Gdx.files.internal(AssetPaths.PARTICLE2), Gdx.files.internal("particles"));
        bulletEffect.start();
    }

    @Override
    public void render() {
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.draw(backgroundImg, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        if (!isGameOver) {
            handleInput();
            if (!isPaused) {
                update(Gdx.graphics.getDeltaTime());
                draw();
            } else {
                showPauseScreen();
                planeEffect.allowCompletion();
                bulletEffect.allowCompletion();
            }
        } else {
            showGameOverScreen();
            planeEffect.allowCompletion();
            bulletEffect.allowCompletion();
        }

        planeEffect.draw(batch, Gdx.graphics.getDeltaTime());
        for (Bullet bullet : bullets) {
            if (bullet.isAlive()) {
                bulletEffect.setPosition(bullet.getX() + bullet.getWidth() / 2, bullet.getY());
                bulletEffect.draw(batch, Gdx.graphics.getDeltaTime());
            }
        }
        batch.end();

        if (debugMode) {
            renderDebug();
        }
    }

    private void draw() {
        batch.draw(planeTexture, plane.getX(), plane.getY(), plane.getWidth(), plane.getHeight());

        for (FuelCan fuelCan : fuelCans) {
            if (fuelCan.isAlive()) {
                batch.draw(fuelCanTexture, fuelCan.getX(), fuelCan.getY(), fuelCan.getWidth(), fuelCan.getHeight());
            }
        }
        for (Cloud cloud : clouds) {
            if (cloud.isAlive()) {
                batch.draw(cloudTexture, cloud.getX(), cloud.getY(), cloud.getWidth(), cloud.getHeight());
            }
        }
        for (Bird bird : birds) {
            if (bird.isAlive()) {
                batch.draw(birdTexture, bird.getX(), bird.getY(), bird.getWidth(), bird.getHeight());
            }
        }
        for (Bullet bullet : bullets) {
            if (bullet.isAlive()) {
                batch.draw(bulletTexture, bullet.getX(), bullet.getY(), bullet.getWidth(), bullet.getHeight());
            }
        }
        for (PowerUp powerUp : powerUps) {
            if (powerUp.isAlive()) {
                batch.draw(powerUpTexture, powerUp.getX(), powerUp.getY(), powerUp.getWidth(), powerUp.getHeight());
            }
        }

        batch.setColor(new Color(1f, 1f, 1f, 0.6f));
        batch.draw(new TextureRegion(gameplayAtlas.findRegion("blank")), 10f, Gdx.graphics.getHeight() - 75f, 200f, 60f);
        batch.setColor(Color.WHITE);

        font.getData().setScale(0.8f);
        font.setColor(Color.RED);
        font.draw(batch, "FUEL: " + fuel, 20f, Gdx.graphics.getHeight() - 20f);
        font.setColor(Color.OLIVE);
        font.draw(batch, "BIRDS HIT: " + birdsHit, 20f, Gdx.graphics.getHeight() - 50f);
    }

    private void update(float deltaTime) {
        cloudSpawnTimer += deltaTime;
        fuelCanSpawnTimer += deltaTime;
        birdSpawnTimer += deltaTime;
        fuelDecreaseTimer += deltaTime;

        if (fuel < 0) {
            fuel = 0;
            isGameOver = true;
        }

        if (cloudSpawnTimer >= CLOUD_SPAWN_TIME) {
            Cloud cloud = Cloud.spawn(cloudPool, cloudTexture);
            clouds.add(cloud);
            cloudSpawnTimer = 0;
        }
        if (fuelCanSpawnTimer >= FUELCAN_SPAWN_TIME) {
            FuelCan fuelCan = FuelCan.spawn(fuelCanPool, fuelCanTexture);
            fuelCans.add(fuelCan);
            fuelCanSpawnTimer = 0;
        }
        if (birdSpawnTimer >= BIRD_SPAWN_TIME) {
            Bird bird = Bird.spawn(birdPool, birdTexture, plane);
            birds.add(bird);
            birdSpawnTimer = 0;
        }

        if (powerUpActive) {
            powerUpTimer -= deltaTime;
            if (powerUpTimer <= 0) {
                powerUpActive = false;
            }
        } else {
            fuelDecreaseTimer += deltaTime;
            if (fuelDecreaseTimer >= FUEL_DECREASE_INTERVAL) {
                fuel -= 2;
                fuelDecreaseTimer = 0;
            }
        }

        if (birdsHit > 0 && birdsHit % 5 == 0 && !powerUpActive) {
            PowerUp powerUp = PowerUp.spawn(powerUpPool, powerUpTexture);
            powerUps.add(powerUp);
            birdsHit++; // so that we don't spawn powerups every frame
        }

        for (Iterator<FuelCan> it = fuelCans.iterator(); it.hasNext(); ) {
            FuelCan fuelCan = it.next();
            fuelCan.update(deltaTime);
            if (fuelCan.isOffScreen()) {
                fuelCanPool.free(fuelCan);
                it.remove();
            } else if (fuelCan.getBounds().overlaps(plane.getBounds())) {
                fuel += 10;
                if (fuel > 100) fuel = 100;
                fuelCanPool.free(fuelCan);
                it.remove();
            }
        }

        for (Iterator<Cloud> it = clouds.iterator(); it.hasNext(); ) {
            Cloud cloud = it.next();
            cloud.update(deltaTime);
            if (cloud.isOffScreen()) {
                cloudPool.free(cloud);
                it.remove();
            } else if (cloud.getBounds().overlaps(plane.getBounds())) {
                fuel -= 15;
                cloudPool.free(cloud);
                it.remove();
            }
        }

        for (Iterator<Bird> it = birds.iterator(); it.hasNext(); ) {
            Bird bird = it.next();
            bird.update(deltaTime);
            if (bird.isOffScreen()) {
                birdPool.free(bird);
                it.remove();
            } else if (bird.getBounds().overlaps(plane.getBounds())) {
                fuel -= 30;
                birdPool.free(bird);
                it.remove();
            }
        }

        for (Iterator<Bullet> it = bullets.iterator(); it.hasNext(); ) {
            Bullet bullet = it.next();
            bullet.update(deltaTime);

            if (bullet.isOffScreen()) {
                bulletPool.free(bullet);
                it.remove();
                continue;
            }

            boolean bulletHit = false;
            for (Iterator<Bird> birdIt = birds.iterator(); birdIt.hasNext(); ) {
                Bird bird = birdIt.next();

                if (bullet.getBounds().overlaps(bird.getBounds())) {
                    birdsHit++;
                    birdPool.free(bird);
                    birdIt.remove();
                    bulletHit = true;
                    break;
                }
            }
            if (bulletHit) {
                bulletPool.free(bullet);
                it.remove();
            }
        }

        for (Iterator<PowerUp> it = powerUps.iterator(); it.hasNext(); ) {
            PowerUp powerUp = it.next();
            powerUp.update(deltaTime);
            if (powerUp.isOffScreen()) {
                powerUpPool.free(powerUp);
                it.remove();
            } else if (powerUp.getBounds().overlaps(plane.getBounds())) {
                powerUpActive = true;
                powerUpTimer = POWER_UP_DURATION;
                powerUpPool.free(powerUp);
                it.remove();
            }
        }

        planeEffect.setPosition(plane.getX() + plane.getWidth() / 2, plane.getY());
        planeEffect.update(deltaTime);

        for (Bullet bullet : bullets) {
            if (bullet.isAlive()) {
                bulletEffect.setPosition(bullet.getX() + bullet.getWidth() / 2, bullet.getY());
                bulletEffect.update(deltaTime);
            }
        }
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            isPaused = !isPaused;
            if (!isPaused) {
                planeEffect.start();
                bulletEffect.start();
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            debugMode = !debugMode;
        }

        if (!isPaused) {
            if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
                if (!spacePressed) {
                    Bullet bullet = plane.shoot(bulletPool, bulletTexture);
                    bullets.add(bullet);
                    spacePressed = true;
                }
            } else {
                spacePressed = false;
            }

            if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                plane.moveLeft(300, Gdx.graphics.getDeltaTime());
            }
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                plane.moveRight(300, Gdx.graphics.getDeltaTime());
            }
        }

        if (debugMode) {
            if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                camera.translate(0, 300 * Gdx.graphics.getDeltaTime());
            }
            if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                camera.translate(0, -300 * Gdx.graphics.getDeltaTime());
            }
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                camera.translate(-300 * Gdx.graphics.getDeltaTime(), 0);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                camera.translate(300 * Gdx.graphics.getDeltaTime(), 0);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
                camera.zoom += 0.02;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.E)) {
                camera.zoom -= 0.02;
            }
            camera.update();
        }
    }

    private void showGameOverScreen() {
        GlyphLayout layout = new GlyphLayout();

        gameOverFont.setColor(Color.RED);
        String gameOverText = "GAME OVER";
        layout.setText(gameOverFont, gameOverText);

        float gameOverX = (Gdx.graphics.getWidth() - layout.width) / 2f;
        float gameOverY = (Gdx.graphics.getHeight() + layout.height) / 2f;

        gameOverFont.getData().setScale(1.75f);
        gameOverFont.draw(batch, gameOverText, gameOverX, gameOverY);

        font.getData().setScale(0.6f);
        String restartText = "RESTART";
        GlyphLayout restartLayout = new GlyphLayout();
        restartLayout.setText(font, restartText);

        resetButton.width = restartLayout.width + 20;
        resetButton.height = restartLayout.height + 10;
        resetButton.x = (Gdx.graphics.getWidth() - resetButton.width) / 2f;
        resetButton.y = gameOverY - resetButton.height - 55;

        batch.setColor(new Color(1f, 1f, 1f, 0.5f));
        batch.draw(new TextureRegion(gameplayAtlas.findRegion("blank")), resetButton.x, resetButton.y, resetButton.width, resetButton.height);
        batch.setColor(Color.NAVY);

        font.setColor(Color.WHITE);
        font.draw(batch, restartText, resetButton.x + (resetButton.width - restartLayout.width) / 2f, resetButton.y + (resetButton.height + restartLayout.height) / 2f);

        if (Gdx.input.justTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = Gdx.graphics.getHeight() - Gdx.input.getY();
            if (resetButton.contains(touchX, touchY)) {
                resetGame();
            }
        }
    }

    private void showPauseScreen() {
        GlyphLayout layout = new GlyphLayout();

        gameOverFont.setColor(Color.WHITE);
        String pauseText = "PAUSED";
        layout.setText(gameOverFont, pauseText);

        float pauseX = (Gdx.graphics.getWidth() - layout.width) / 2f;
        float pauseY = (Gdx.graphics.getHeight() + layout.height) / 2f;

        gameOverFont.getData().setScale(1.75f);
        gameOverFont.draw(batch, pauseText, pauseX, pauseY);

        batch.setColor(Color.NAVY);
    }

    private void resetGame() {
        fuel = 100;
        birdsHit = 0;
        isGameOver = false;
        powerUpActive = false;
        powerUpTimer = 0;

        fuelCans.clear();
        clouds.clear();
        birds.clear();
        bullets.clear();
        powerUps.clear();

        plane.setPosition(Gdx.graphics.getWidth() / 2f, 20f);
        plane.update(0);

        planeEffect.reset();
        bulletEffect.reset();
    }

    private void renderDebug() {
        shapeRenderer.setProjectionMatrix(camera.combined);

        // Draw grid
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.GRAY);
        for (int x = 0; x < Gdx.graphics.getWidth(); x += 50) {
            shapeRenderer.line(x, 0, x, Gdx.graphics.getHeight());
        }
        for (int y = 0; y < Gdx.graphics.getHeight(); y += 50) {
            shapeRenderer.line(0, y, Gdx.graphics.getWidth(), y);
        }
        shapeRenderer.end();

        // Draw bounds for each texture
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(plane.getX(), plane.getY(), plane.getWidth(), plane.getHeight());
        for (FuelCan fuelCan : fuelCans) {
            shapeRenderer.rect(fuelCan.getX(), fuelCan.getY(), fuelCan.getWidth(), fuelCan.getHeight());
        }
        for (Cloud cloud : clouds) {
            shapeRenderer.rect(cloud.getX(), cloud.getY(), cloud.getWidth(), cloud.getHeight());
        }
        for (Bird bird : birds) {
            shapeRenderer.rect(bird.getX(), bird.getY(), bird.getWidth(), bird.getHeight());
        }
        for (Bullet bullet : bullets) {
            shapeRenderer.rect(bullet.getX(), bullet.getY(), bullet.getWidth(), bullet.getHeight());
        }
        for (PowerUp powerUp : powerUps) {
            shapeRenderer.rect(powerUp.getX(), powerUp.getY(), powerUp.getWidth(), powerUp.getHeight());
        }
        shapeRenderer.end();

        // Display memory usage
        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, "Memory Usage: " + Gdx.app.getJavaHeap() / 1024 + " KB", 10, Gdx.graphics.getHeight() - 80);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        AssetManager.getInstance().dispose();
        font.dispose();
        gameOverFont.dispose();
    }
}
