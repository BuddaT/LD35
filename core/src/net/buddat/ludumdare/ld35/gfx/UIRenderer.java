package net.buddat.ludumdare.ld35.gfx;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;

import net.buddat.ludumdare.ld35.GraphicsHandler;
import net.buddat.ludumdare.ld35.LogicHandler;
import net.buddat.ludumdare.ld35.entity.Prey;

public class UIRenderer {
	
	private static final boolean DEBUG_DRAW_COLLISIONS = false;
	private static final boolean DEBUG_DRAW_ENTITYINFO = false;

	private SpriteBatch batch;
	private BitmapFont font;
	
	private Texture sHead, wHead;
	
	private final GlyphLayout layout = new GlyphLayout();
	
	private ShapeRenderer shapes;
	
	private final String instructions = "Herd your sheep back into their pen " +
						"while avoiding the pesky wolves. Chase them away to " +
						"keep your sheep alive.";
	
	public UIRenderer() {

	}

	public void init() {
		batch = new SpriteBatch();
		shapes = new ShapeRenderer();
		
		font = new BitmapFont(Gdx.files.internal("testFont.fnt"));
		font.setColor(Color.WHITE);
		
		sHead = new Texture(Gdx.files.internal("sheepHead.png"));
		wHead = new Texture(Gdx.files.internal("wolfHead.png"));
	}

	private static final int scale = 5, offset = 100;
	public void render() {
		WorldRenderer w = GraphicsHandler.getGraphicsHandler().getWorldRenderer();
		LogicHandler l = GraphicsHandler.getLogicHandler();
		Engine e = w.getCurrentLevel().getEngine();
		
		final int screenWidth = Gdx.graphics.getWidth();
		final int screenHeight = Gdx.graphics.getHeight();
		
		int sheepCount = w.getCurrentLevel().sheepCount;
		int wolfCount = 0, hiddenSheepCount = 0;
		
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
			if (ent.getComponent(LogicHandler.PredatorHidden.class) != null) {
				if (!ent.getComponent(Prey.class).isDead())
					hiddenSheepCount++;
			}
			if (ent.getComponent(LogicHandler.Predator.class) != null)
				wolfCount++;
		}
		
		if (DEBUG_DRAW_ENTITYINFO) {
			batch.begin();
			font.draw(batch, "Sheep Count: " + sheepCount, 20, 140);
			font.draw(batch, "Hidden Wolf Count: " + hiddenSheepCount, 20, 100);
			font.draw(batch, "Wolf Count: " + wolfCount, 20, 60);
			font.draw(batch, "Pen Count: " + GraphicsHandler.getLogicHandler().getNumPenned(), 20, 180);
			font.draw(batch, "Dead Count: " + GraphicsHandler.getLogicHandler().getNumDead(), 20, 220);
			batch.end();
		}
		
		if (w.justStarted) {
			batch.begin();
				font.setColor(Color.WHITE);
				
				font.getData().setScale(4f, 3f);
				layout.setText(font, "Sheep Shift");
				font.draw(batch, "Sheep Shift", screenWidth / 2 - layout.width / 2, screenHeight / 2 + 200);
				
				font.getData().setScale(1.5f, 1f);
				font.draw(batch, instructions, screenWidth / 4, screenHeight / 2, screenWidth / 2, 1, true);
				
				font.getData().setScale(2.5f, 1.5f);
				layout.setText(font, "Click anywhere to begin");
				font.draw(batch, "Click anywhere to begin", screenWidth / 2 - layout.width / 2, screenHeight / 2 - 200);
			batch.end();
			
			if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
				w.justStarted = false;
				w.pauseLogic = false;
			}
		}
		
		if (w.levelWon) {
			batch.begin();
				font.setColor(Color.GREEN);
				font.getData().setScale(4f, 3f);
				layout.setText(font, "Level Completed!");
				font.draw(batch, "Level Completed!", screenWidth / 2 - layout.width / 2, screenHeight / 2 + 200);
				
				font.getData().setScale(1.5f, 1f);
				layout.setText(font, "Sheep Saved: " + GraphicsHandler.getLogicHandler().getNumPenned() + " / " + w.getCurrentLevel().sheepToWin);
				font.draw(batch, "Sheep Saved: " + GraphicsHandler.getLogicHandler().getNumPenned() + " / " + w.getCurrentLevel().sheepToWin, 
						screenWidth / 2 - layout.width / 2, screenHeight / 2);
				
				font.setColor(Color.RED);
				layout.setText(font, "Sheep Lost: " + GraphicsHandler.getLogicHandler().getNumDead() + " / " + w.getCurrentLevel().deadToLose);
				font.draw(batch, "Sheep Lost: " + GraphicsHandler.getLogicHandler().getNumDead() + " / " + w.getCurrentLevel().deadToLose, 
						screenWidth / 2 - layout.width / 2, screenHeight / 2 - 40);
				
				font.setColor(Color.WHITE);
				font.getData().setScale(2.5f, 1.5f);
				layout.setText(font, "Click anywhere to go to the next level");
				font.draw(batch, "Click anywhere to go to the next level", screenWidth / 2 - layout.width / 2, screenHeight / 2 - 150);
				
				if (w.firstLevel) {
					font.setColor(Color.ORANGE);
					font.getData().setScale(1.5f);
					layout.setText(font, "beware the wolves in sheep's clothing");
					font.draw(batch, "beware the wolves in sheep's clothing", screenWidth / 2 - layout.width / 2, 70);
				}
			batch.end();
			
			if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
				w.newLevel(true);
				w.pauseLogic = false;
				
				if (w.firstLevel)
					w.firstLevel = false;
			}
		}
		
		if (w.levelLost) {
			batch.begin();
				font.setColor(Color.RED);
				font.getData().setScale(4f, 3f);
				layout.setText(font, "Level Lost!");
				font.draw(batch, "Level Lost!", screenWidth / 2 - layout.width / 2, screenHeight / 2 + 200);
				
				font.getData().setScale(1.5f, 1f);
				layout.setText(font, "Sheep Saved: " + GraphicsHandler.getLogicHandler().getNumPenned() + " / " + w.getCurrentLevel().sheepToWin);
				font.draw(batch, "Sheep Saved: " + GraphicsHandler.getLogicHandler().getNumPenned() + " / " + w.getCurrentLevel().sheepToWin, 
						screenWidth / 2 - layout.width / 2, screenHeight / 2);
				
				font.setColor(Color.RED);
				layout.setText(font, "Sheep Lost: " + GraphicsHandler.getLogicHandler().getNumDead() + " / " + w.getCurrentLevel().deadToLose);
				font.draw(batch, "Sheep Lost: " + GraphicsHandler.getLogicHandler().getNumDead() + " / " + w.getCurrentLevel().deadToLose, 
						screenWidth / 2 - layout.width / 2, screenHeight / 2 - 40);
				
				font.setColor(Color.WHITE);
				font.getData().setScale(2.5f, 1.5f);
				layout.setText(font, "Click anywhere to start again");
				font.draw(batch, "Click anywhere to start again", screenWidth / 2 - layout.width / 2, screenHeight / 2 - 200);
			batch.end();
			
			if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
				w.newLevel(false);
				w.justStarted = true;
			}
		}
		
		if (!w.pauseLogic) {
			batch.begin();
				batch.draw(sHead, 20, 70, 64, 64);
				batch.draw(wHead, 20, 10, 64, 64);
				
				font.setColor(Color.WHITE);
				font.getData().setScale(1.5f, 1.5f);
				font.draw(batch, "" + (sheepCount - GraphicsHandler.getLogicHandler().getNumDead() - w.getCurrentLevel().transformCount), 90, 120);
				font.draw(batch, "" + wolfCount, 90, 60);
				
				font.setColor(Color.ORANGE);
				font.getData().setScale(1f);
				layout.setText(font, "Sheep Needed: " + (w.getCurrentLevel().sheepToWin - GraphicsHandler.getLogicHandler().getNumPenned()));
				font.draw(batch, "Sheep Needed: " + (w.getCurrentLevel().sheepToWin - GraphicsHandler.getLogicHandler().getNumPenned()), 
						screenWidth - layout.width - 20, 50);
				
				font.setColor(Color.GRAY);
				font.getData().setScale(1f);
				font.draw(batch, "Esc to Restart", screenWidth - 120, screenHeight - 20);
			batch.end();
			
			if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
				w.newLevel(false);
				w.justStarted = true;
				w.pauseLogic = true;
			}
		}
		
		batch.begin();
			font.setColor(Color.GRAY);
			font.getData().setScale(1f);
			font.draw(batch, "M to Mute", screenWidth - 105, screenHeight - 60);
		batch.end();
		
		if (Gdx.input.isKeyPressed(Input.Keys.M)) {
			w.mute = !w.mute;
			
			if (w.mute) 
				w.sheepAmbientSnd.pause();
			else 
				w.sheepAmbientSnd.play();
		}
	}

	public void dispose() {
		batch.dispose();
		font.dispose();
	}
}
