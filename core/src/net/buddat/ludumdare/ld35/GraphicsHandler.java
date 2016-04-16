package net.buddat.ludumdare.ld35;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Model;

import net.buddat.ludumdare.ld35.gfx.UIRenderer;
import net.buddat.ludumdare.ld35.gfx.WorldRenderer;

public class GraphicsHandler extends ApplicationAdapter {

	public static final float FPS_CAP = 90f;

	private static LogicHandler logicHandler = new LogicHandler();
	private static GraphicsHandler graphicsHandler;

	private WorldRenderer worldRenderer;
	private UIRenderer uiRenderer;

	private AssetManager assets;

	private boolean loadingAssets = true;

	public static final String MDL_PLR = "playerTest.g3dj";
	public static final String MDL_SHEEP = "sheepOriginal.g3dj";
	public static final String MDL_WOLF = "wolfOriginal.g3dj";

	@Override
	public void create() {
		assets = new AssetManager();
		assets.load(MDL_PLR, Model.class);
		assets.load(MDL_SHEEP, Model.class);
		assets.load(MDL_WOLF, Model.class);

		GraphicsHandler.getLogicHandler().init();

		worldRenderer = new WorldRenderer();
		uiRenderer = new UIRenderer();
	}

	private void doneLoading() {
		loadingAssets = false;

		worldRenderer.create();
		uiRenderer.init();
	}

	@Override
	public void render() {
		if (loadingAssets) {
			if (assets.update())
				doneLoading();
			return;
		}

		GraphicsHandler.getLogicHandler().update();
		worldRenderer.update();

		worldRenderer.render();

		uiRenderer.render();
	}

	@Override
	public void dispose() {
		worldRenderer.dispose();
		uiRenderer.dispose();
		assets.dispose();
	}

	public AssetManager getAssets() {
		return assets;
	}

	public static LogicHandler getLogicHandler() {
		if (logicHandler == null)
			logicHandler = new LogicHandler();

		return logicHandler;
	}

	public static GraphicsHandler getGraphicsHandler() {
		if (graphicsHandler == null)
			graphicsHandler = new GraphicsHandler();

		return graphicsHandler;
	}

}
