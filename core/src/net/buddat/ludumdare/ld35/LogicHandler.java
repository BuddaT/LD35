package net.buddat.ludumdare.ld35;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector3;
import net.buddat.ludumdare.ld35.entity.Position;
import net.buddat.ludumdare.ld35.entity.Movement;

import java.util.ArrayList;

public class LogicHandler {
	private Engine engine;
	private static final int NUM_CREATURES = 10;
	private Family creatures;
	private Entity player;
	public static final ComponentMapper<Position> POSN_MAPPER =
			ComponentMapper.getFor(Position.class);
	public static final ComponentMapper<Movement> MVMNT_MAPPER =
			ComponentMapper.getFor(Movement.class);
	private static final float PLAYER_EFFECT_RANGE = 1;
	// Speed at which evasion is attempted
	private static final float EVASION_SPEED = 1;
	// Distance to calculate crowding
	private static final float CROWDING_RANGE = 1;
	private static final float CROWDING_SPEED = 1;
	// Distance to calculate cohesion
	private static final float COHESION_RANGE = 2;
	private static final float COHESION_SPEED = 2;

	private static final float MAX_SPEED = 2;

	public void init() {
		engine = new Engine();
		player = new Entity();
		player.add(new Position(0, 0, 0, 0, 0, 0));
		player.add(new Movement(0, 0, 0, 0, 0, 0));
		engine.addEntity(player);
		for (int i = 0; i < NUM_CREATURES; i++) {
			Entity creature = new Entity();
			creature.add(new Position(i, 0, 0, 0, 0, 0));
			creature.add(new Movement(0, 0, 0, 0, 0, 0));
			engine.addEntity(new Entity());
		}
		creatures = Family.all(Position.class, Movement.class).get();
	}

	public void update() {
		// Move the player towards the 3d position the mouse is pointing to?
		float mousePosnX = 0;
		float mousePosnY = 0;
		float mousePosnZ = 0;
		Position posn = POSN_MAPPER.get(player);
		engine.getEntitiesFor(creatures);
	}

	/**
	 * Calculate the changes in the flock's movement. Note current algorithm is
	 * O(n^2) due to the need to look at all other members of the flock.
	 * Partitioning by location would improve this.
	 */
	private void calculateMovementChanges() {
		ImmutableArray<Entity> entities = engine.getEntitiesFor(creatures);
		Position playerPosn = POSN_MAPPER.get(player);
		for (Entity entity : entities) {
			ArrayList<Entity> neighbours = new ArrayList<Entity>();
			Position posn = POSN_MAPPER.get(entity);
			Movement mvmnt = MVMNT_MAPPER.get(entity);
			Vector3 change;

			// Attempt to move away from the player
			if (posn.position.dst2(playerPosn.position) <= PLAYER_EFFECT_RANGE) {
				change = new Vector3(posn.position).sub(playerPosn.position).nor().scl(EVASION_SPEED);
			} else {
				change = new Vector3();
			}

			// Try to avoid crowding neighbors, yet keep in distance
			Vector3 crowdingChange = new Vector3();
			Vector3 cohesionChange = new Vector3();
			for (Entity potentialNeighbor : entities) {
				Position otherPosn = POSN_MAPPER.get(potentialNeighbor);
				if (posn.position.dst2(otherPosn.position) <= CROWDING_RANGE) {
					crowdingChange.add(new Vector3(otherPosn.position).sub(posn.position).nor());
				}
				if (posn.position.dst2(otherPosn.position) <= COHESION_RANGE) {
					cohesionChange.add(new Vector3(posn.position).sub(otherPosn.position)).nor();
				}
			}
			crowdingChange.nor().scl(CROWDING_SPEED);
			cohesionChange.nor().scl(COHESION_SPEED);
			change.add(crowdingChange).add(cohesionChange).clamp(0, MAX_SPEED);
			posn.position.add(change);
		}
	}

	public ImmutableArray<Entity> getEntities() {
		return engine.getEntitiesFor(creatures);
	}
}
