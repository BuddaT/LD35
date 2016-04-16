package net.buddat.ludumdare.ld35;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import net.buddat.ludumdare.ld35.entity.Position;
import net.buddat.ludumdare.ld35.entity.Movement;
import net.buddat.ludumdare.ld35.gfx.ModelFactory;

import java.util.ArrayList;
import java.util.List;

public class LogicHandler {
	private Engine engine;
	private static final int NUM_CREATURES = 5;
	private Family creatures;
	private Entity player;
	private List<ModelInstance> models;
	public static final ComponentMapper<Position> POSN_MAPPER =
			ComponentMapper.getFor(Position.class);
	public static final ComponentMapper<Movement> MVMNT_MAPPER =
			ComponentMapper.getFor(Movement.class);
	public static final ComponentMapper<ModelComponent> MODEL_MAPPER =
			ComponentMapper.getFor(ModelComponent.class);

	private static final float PLAYER_EFFECT_RANGE = 1;
	// Speed at which evasion is attempted
	private static final float EVASION_SPEED = 1;
	// Distance to calculate crowding (^ 2 to avoid sqrt)
	private static final float CROWDING_RANGE = 10*10;
	private static final float CROWDING_SPEED = 0.5f;
	// Distance to calculate cohesion (^ 2 to avoid sqrt)
	private static final float COHESION_RANGE = 30*30;
	private static final float COHESION_SPEED = 0.05f;

	private static final float MAX_SPEED = 2;

	private static final float DIRECTIONAL_DAMPING = 0.5f;

	private Entity createNewCreature(Vector3 position, Vector3 rotation) {
		Entity creature = new Entity();
		creature.add(new Position(position, rotation));
		creature.add(new Movement(0, 0, 0, 0, 0, 0));
		return creature;
	}

	public void init() {
		engine = new Engine();
		player = new Entity();
		player.add(new Position(new Vector3(), new Vector3()));
		player.add(new Movement(0, 0, 0, 0, 0, 0));
		player.add(new Mouseable());
		engine.addEntity(player);
		for (int i = 0; i < NUM_CREATURES; i++) {
			engine.addEntity(createNewCreature(new Vector3(5f + 10f*i, 5f, 0f), new Vector3()));
		}
		engine.addEntity(createNewCreature(new Vector3(20f, 25f, 0f), new Vector3()));

		creatures = Family.all(Position.class, Movement.class).exclude(Mouseable.class).get();
		models = createModels(engine.getEntitiesFor(creatures));
	}

	public void update() {
		// Move the player towards the 3d position the mouse is pointing to?
		float mousePosnX = 0;
		float mousePosnY = 0;
		float mousePosnZ = 0;
		Position posn = POSN_MAPPER.get(player);
		calculateMovementChanges();
	}

	/**
	 * Calculate the changes in the flock's movement. Note current algorithm is
	 * O(n^2) due to the need to look at all other members of the flock.
	 * Partitioning by location would improve this.
	 */
	private void calculateMovementChanges() {
		ImmutableArray<Entity> entities = engine.getEntitiesFor(creatures);
		Position playerPosn = POSN_MAPPER.get(player);
		for (int i = 0; i < entities.size(); i++) {
			Entity entity = entities.get(i);
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
			ImmutableArray<Entity> potentialNeighbours = engine.getEntitiesFor(creatures);
			for (int j = 0; j < potentialNeighbours.size(); j++) {
				if (i == j) {
					continue;
				}
				Entity potentialNeighbor = potentialNeighbours.get(j);
				Position otherPosn = POSN_MAPPER.get(potentialNeighbor);
				float distance = posn.position.dst2(otherPosn.position);
				if (distance <= CROWDING_RANGE) {
					// crowding acceleration is inversely proportional to distance
					Vector3 crowdDirection = new Vector3(posn.position).sub(otherPosn.position).nor();
					crowdingChange.add(crowdDirection.scl(CROWDING_RANGE/(distance*distance)));
				}
				if (distance <= COHESION_RANGE) {
					cohesionChange.add(new Vector3(otherPosn.position).sub(posn.position).nor());
				}
			}
			crowdingChange.clamp(0, CROWDING_SPEED);
			cohesionChange.clamp(0, COHESION_SPEED);
			change.add(crowdingChange).add(cohesionChange).clamp(0, MAX_SPEED);
			mvmnt.velocity.add(change).scl(0.5f);
		}

		// now apply the velocity to the position
		for (Entity entity : entities) {
			Position posn = POSN_MAPPER.get(entity);
			Vector3 change = MVMNT_MAPPER.get(entity).velocity;
			posn.position.add(change);
			MODEL_MAPPER.get(entity).model.transform.translate(change);
		}
	}

	public List<ModelInstance> getModels() {
		return models;
	}

	public List<ModelInstance> createModels(Iterable<Entity> entities) {
		ArrayList<ModelInstance> models = new ArrayList<ModelInstance>();
		for (Entity entity : entities) {
			ModelInstance model = ModelFactory.createSphereModel(5f, 5f, 5f, Color.FIREBRICK, 16);
			model.transform.setToTranslation(POSN_MAPPER.get(entity).position);
			entity.add(new ModelComponent(model));
			models.add(model);
		}
		return models;
	}

	// Simple reference to the model
	private static class ModelComponent implements Component {
		private ModelInstance model;
		public ModelComponent(ModelInstance model) {
			this.model = model;
		}
	}

	// Differentiate player
	private static class Mouseable implements Component {}
}
