package net.buddat.ludumdare.ld35.gfx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class UIRenderer {

	private SpriteBatch batch;
	private BitmapFont font;

	public UIRenderer() {

	}

	public void init() {
		batch = new SpriteBatch();
		font = new BitmapFont();
		font.setColor(Color.WHITE);
		font.getData().setScale(5f);
	}

	public void render() {
		batch.begin();
		font.draw(batch, "DOLLA DOLLA BILLS YA'LL", 20, 80);
		batch.end();
	}

	public void dispose() {
		batch.dispose();
		font.dispose();
	}
}
