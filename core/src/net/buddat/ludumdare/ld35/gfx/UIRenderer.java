package net.buddat.ludumdare.ld35.gfx;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;

import net.buddat.ludumdare.ld35.GraphicsHandler;
import net.buddat.ludumdare.ld35.LogicHandler;

public class UIRenderer {
	
	private static final boolean DEBUG_DRAW_COLLISIONS = false;

	private SpriteBatch batch;
	private BitmapFont font;
	
	private ShapeRenderer shapes;
	
	public UIRenderer() {

	}

	public void init() {
		batch = new SpriteBatch();
		shapes = new ShapeRenderer();
		
		font = new BitmapFont();
		font.setColor(Color.WHITE);
		font.getData().setScale(2f);
	}

	private static final int scale = 5, offset = 100;
	public void render() {
		Engine e = GraphicsHandler.getGraphicsHandler().getWorldRenderer().getCurrentLevel().getEngine();
		int sheepCount = 0, wolfCount = 0, hiddenSheepCount = 0;
		
		if (DEBUG_DRAW_COLLISIONS) {
			shapes.begin(ShapeType.Filled);
			
			shapes.setColor(0f, 0f, 1f, 0.5f);
			for (IntersectableModel m : GraphicsHandler.getGraphicsHandler().getWorldRenderer().getCurrentLevel().getCollisionModels()) {
				Rectangle r = m.collision.getBoundingRectangle();
				shapes.rect(r.x * scale + offset * scale, 280 - (r.y * scale - offset), r.width * scale, r.height * scale);
			}
			
			shapes.setColor(1f, 1f, 1f, 0.5f);
			for (Entity ent : e.getEntities()) {
				if (ent.getComponent(LogicHandler.ModelComponent.class) != null) {
					Rectangle r = ent.getComponent(LogicHandler.ModelComponent.class).model.collision.getBoundingRectangle();
					shapes.rect(r.x * scale + offset * scale, 280 - (r.y * scale - offset), r.width * scale, r.height * scale);
				}
			}
			
			Rectangle r = GraphicsHandler.getGraphicsHandler().getWorldRenderer().getPlayerModel().collision.getBoundingRectangle();
			shapes.setColor(1f, 0f, 0f, 0.5f);
			shapes.rect(r.x * scale + offset * scale, 280 - (r.y * scale - offset), r.width * scale, r.height * scale);
			
			shapes.end();
		}
		
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
