package net.buddat.ludumdare.ld35;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

import net.buddat.ludumdare.ld35.gfx.WorldRenderer;

public class GraphicsHandler extends ApplicationAdapter {

	private static LogicHandler logicHandler = new LogicHandler();
	private static GraphicsHandler graphicsHandler;

	private WorldRenderer worldRenderer;

	@Override
	public void create() {
		GraphicsHandler.getLogicHandler().init();

		worldRenderer = new WorldRenderer();
		worldRenderer.create();
	}

	@Override
	public void render() {
		GraphicsHandler.getLogicHandler().update();
		worldRenderer.update();

		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		worldRenderer.render();
	}

	@Override
	public void dispose() {
		worldRenderer.dispose();
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
