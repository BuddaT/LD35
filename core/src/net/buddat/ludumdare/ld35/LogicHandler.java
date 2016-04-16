package net.buddat.ludumdare.ld35;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import net.buddat.ludumdare.ld35.entity.PositionComponent;
import net.buddat.ludumdare.ld35.entity.MovementComponent;

public class LogicHandler {
	private Engine engine;
	private static final int NUM_DOODS = 10;
	private Entity player;
	public static final ComponentMapper<PositionComponent> POSN_MAPPER =
			ComponentMapper.getFor(PositionComponent.class);
	public static final ComponentMapper<MovementComponent> VEL_MAPPER =
			ComponentMapper.getFor(MovementComponent.class);

	public void init() {
		engine = new Engine();
		player = new Entity();
		player.add(new PositionComponent(0, 0, 0, 0, 0, 0));
		player.add(new MovementComponent(0, 0, 0, 0, 0, 0));
		engine.addEntity(player);
		for (int i = 0; i < NUM_DOODS; i++) {
			Entity dood = new Entity();
			dood.add(new PositionComponent(i, 0, 0, 0, 0, 0));
			dood.add(new MovementComponent(0, 0, 0, 0, 0, 0));
			engine.addEntity(new Entity());
		}
	}

	public void update() {
		// Move the player towards the 3d position the mouse is pointing to?
		float mousePosnX = 0;
		float mousePosnY = 0;
		float mousePosnZ = 0;
		PositionComponent posn = POSN_MAPPER.get(player);
	}

}
