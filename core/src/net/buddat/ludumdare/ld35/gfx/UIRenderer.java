package net.buddat.ludumdare.ld35.gfx;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import net.buddat.ludumdare.ld35.GraphicsHandler;
import net.buddat.ludumdare.ld35.LogicHandler;

public class UIRenderer {

	private SpriteBatch batch;
	private BitmapFont font;

	public UIRenderer() {

	}

	public void init() {
		batch = new SpriteBatch();
		font = new BitmapFont();
		font.setColor(Color.WHITE);
		font.getData().setScale(2f);
	}

	public void render() {
		Engine e = GraphicsHandler.getGraphicsHandler().getWorldRenderer().getCurrentLevel().getEngine();
		int sheepCount = 0, wolfCount = 0, hiddenSheepCount = 0;
		
		for (Entity ent : e.getEntities()) {
			if (ent.getComponent(LogicHandler.Prey.class) != null)
				sheepCount++;
			if (ent.getComponent(LogicHandler.PredatorHidden.class) != null)
				hiddenSheepCount++;
			if (ent.getComponent(LogicHandler.Predator.class) != null)
				wolfCount++;
		}
		
		batch.begin();
		font.draw(batch, "Sheep Count: " + sheepCount, 20, 140);
		font.draw(batch, "Hidden Wolf Count: " + hiddenSheepCount, 20, 100);
		font.draw(batch, "Wolf Count: " + wolfCount, 20, 60);
		batch.end();
	}

	public void dispose() {
		batch.dispose();
		font.dispose();
	}
}
