package si.um.feri.trajkoski.oop.assets;

import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;

public class AssetManager {
    private static final AssetManager instance = new AssetManager();
    private final com.badlogic.gdx.assets.AssetManager assetManager;

    private AssetManager() {
        assetManager = new com.badlogic.gdx.assets.AssetManager(new InternalFileHandleResolver());
    }

    public static AssetManager getInstance() {
        return instance;
    }

    public void loadAllAssets() {
        assetManager.load(AssetDescriptors.GAMEPLAY);
        assetManager.finishLoading();
    }

    public <T> T get(String assetPath, Class<T> type) {
        return assetManager.get(assetPath, type);
    }

    public void dispose() {
        assetManager.dispose();
    }
}
