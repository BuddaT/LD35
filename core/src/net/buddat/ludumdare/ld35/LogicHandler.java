package net.buddat.ludumdare.ld35;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;

import net.buddat.ludumdare.ld35.entity.*;

public class LogicHandler {
	private Engine engine;
	private static final int NUM_CREATURES = 5;
	private Family creatures;
	private Family sheep;
	private Entity player;
	private static final ComponentMapper<Position> POSN_MAPPER =
			ComponentMapper.getFor(Position.class);
	private static final ComponentMapper<Movement> MVMNT_MAPPER =
			ComponentMapper.getFor(Movement.class);
	private static final ComponentMapper<ModelComponent> MODEL_MAPPER =
			ComponentMapper.getFor(ModelComponent.class);
	private static final ComponentMapper<CrowdingAttractor> CROWDING_MAPPER =
			ComponentMapper.getFor(CrowdingAttractor.class);
	private static final ComponentMapper<CohesionAttractor> COHESION_MAPPER =
			ComponentMapper.getFor(CohesionAttractor.class);

	// Distance to player within which evasion is attempted (^ 2)
	private static final float PLAYER_EFFECT_RANGE = 20*20;
	// Speed at which evasion is attempted
	private static final float EVASION_SPEED = 5;

	// Distance to calculate cohesion (^ 2 to avoid sqrt)
	private static final float COHESION_RANGE = 30*30;
	private static final float COHESION_SPEED = 0.05f;

	private static final float MAX_SPEED = 2;
	private static final EnumMap<AttractorType, Float> PREY_ATTRACTOR_SPEEDS;
	static {
		EnumMap<AttractorType, Float> speeds =
				new EnumMap<AttractorType, Float>(AttractorType.class);
		speeds.put(AttractorType.CROWDING, 0.5f);
		speeds.put(AttractorType.COHESION, 0.05f);
		speeds.put(AttractorType.PLAYER, EVASION_SPEED);
		speeds.put(AttractorType.PREDATOR, EVASION_SPEED);
		for (AttractorType type : AttractorType.values()) {
			if (speeds.get(type) == null) {
				speeds.put(type, 0.05f);
			}
		}
		PREY_ATTRACTOR_SPEEDS = speeds;
	}

	private ProjectionTranslator projectionTranslator;

	private class Prey implements Component {};

	private Entity createNewPrey(Vector3 position, Vector3 rotation) {
		Entity creature = new Entity();
		creature.add(new Position(position, rotation));
		creature.add(new Movement(0, 0, 0, 0, 0, 0));
		CrowdingAttractor creatureFlockAttractor = new CrowdingAttractor();
		creature.add(creatureFlockAttractor);
		creature.add(new Prey());
		return creature;
	}

	private List<FlockAttractor> getPreyAttractors(Entity entity) {
		List<FlockAttractor> attractors = new ArrayList<FlockAttractor>();
		FlockAttractor crowdingAttractor = CROWDING_MAPPER.get(entity);
		if (crowdingAttractor != null) {
			attractors.add(CROWDING_MAPPER.get(entity));
		}
		// attractors.add(COHESION_MAPPER.get(entity));
		return attractors;
	}

	public void init() {
		engine = new Engine();
		player = new Entity();
		player.add(new Position(new Vector3(), new Vector3()));
		player.add(new Movement(0, 0, 0, 0, 0, 0));
		player.add(new Mouseable());

		engine.addEntity(player);
		for (int i = 0; i < NUM_CREATURES; i++) {
			engine.addEntity(createNewPrey(new Vector3(5f + 10f * i, 0f, 5f), new Vector3()));
		}
		engine.addEntity(createNewPrey(new Vector3(20f, 0f, 25f), new Vector3()));

		creatures = Family.all(Position.class, Movement.class).exclude(Mouseable.class).get();
		sheep = Family.all(Prey.class).get();
	}

	public void update() {
		// Move the player towards the 3d position the mouse is pointing to?
		Vector3 worldMousePosn = projectionTranslator.unproject(Gdx.input.getX(), Gdx.input.getY());
		POSN_MAPPER.get(player).position.set(worldMousePosn);
		calculateMovementChanges();
		if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
			StringBuilder builder = new StringBuilder();
			for (Entity entity : engine.getEntitiesFor(creatures)) {
				builder.append(POSN_MAPPER.get(entity).position).append(", ");
			}
			System.out.println(builder.toString());
		}
	}

	/**
	 * Calculate the changes in the flock's movement. Note current algorithm is
	 * O(n^2) due to the need to look at all other members of the flock.
	 * Partitioning by location would improve this.
	 */
	private void calculateMovementChanges() {
		ImmutableArray<Entity> entities = engine.getEntitiesFor(creatures);
		Position playerPosn = POSN_MAPPER.get(player);

		// First calculate change in position for all objects.
		for (int i = 0; i < entities.size(); i++) {
			Entity entity = entities.get(i);
			ArrayList<Entity> neighbours = new ArrayList<Entity>();
			Position posn = POSN_MAPPER.get(entity);
			Movement mvmnt = MVMNT_MAPPER.get(entity);
			Vector3 change;

			// Attempt to move away from the player
			float dist2ToPlayer = posn.position.dst2(playerPosn.position);
			if (dist2ToPlayer <= PLAYER_EFFECT_RANGE) {
				float acceleration = PLAYER_EFFECT_RANGE/(dist2ToPlayer * dist2ToPlayer);
				change = awayFrom(posn, playerPosn).nor().scl(acceleration).clamp(0, EVASION_SPEED);
			} else {
				change = new Vector3();
			}
			Vector3 cohesionChange = new Vector3();

			// Try to avoid crowding neighbors, yet keep in distance
			ImmutableArray<Entity> others = engine.getEntitiesFor(sheep);
			// Get all the possible attractor types
			EnumMap<AttractorType, Vector3> attractors =
					new EnumMap<AttractorType, Vector3>(AttractorType.class);
			for (AttractorType attractorType : AttractorType.values()) {
				attractors.put(attractorType, new Vector3());
			}
			for (int j = 0; j < others.size(); j++) {
				if (i == j) {
					continue;
				}
				Entity other = others.get(j);
				Position otherPosn = POSN_MAPPER.get(other);
				float distance = posn.position.dst2(otherPosn.position);
				// Crowding mechanic means that entities acceleration away from
				// neighbors increases the closer you are
				for (FlockAttractor attractor : getPreyAttractors(entity)) {
					if (distance <= attractor.getMaxRange()) {
						float acceleration = attractor.getAcceleration(distance);
						Vector3 direction = towards(posn, otherPosn).nor();
						attractors.get(attractor.getAttractantType()).add(direction.scl(acceleration));
					}
				}
				if (distance <= COHESION_RANGE) {
					cohesionChange.add(towards(posn, otherPosn).nor());
				}
			}
			cohesionChange.clamp(0, COHESION_SPEED);

			for (AttractorType attractorType : AttractorType.values()) {
				if (!(attractorType.equals(AttractorType.CROWDING))) {
					continue;
				}
				Vector3 attractantChange = attractors.get(attractorType);
				change.add(attractantChange.clamp(0, PREY_ATTRACTOR_SPEEDS.get(attractorType)));
			}

			change.add(cohesionChange);
			change.clamp(0, MAX_SPEED);
			// zero any y values for now
			change.y = 0;
			// velocity changes to the average between the desired and current
			mvmnt.velocity.add(change).scl(0.5f);
		}

		// now apply the changes to each position
		Vector3 up = new Vector3(0, 1, 0);
		for (Entity entity : entities) {
			Position posn = POSN_MAPPER.get(entity);
			Vector3 change = MVMNT_MAPPER.get(entity).velocity;
			posn.position.add(change);
			ModelInstance model = MODEL_MAPPER.get(entity).model;
			model.transform.setToLookAt(change, up).setTranslation(posn.position);
		}
	}

	// Calculates a new vector from the origin away from the target
	private Vector3 awayFrom(Position origin, Position target) {
		return new Vector3(origin.position).sub(target.position);
	}

	// Calculates a new vector from the origin to the target
	private Vector3 towards(Position origin, Position target) {
		return new Vector3(target.position).sub(origin.position);
	}

	public void createCreatures(ModelInstanceProvider modelProvider) {
		for (Entity entity : engine.getEntitiesFor(creatures)) {
			ModelInstance model = modelProvider.createModel(POSN_MAPPER.get(entity).position);
			entity.add(new ModelComponent(model));
		}
	}

	public void setProjectionTranslator(ProjectionTranslator translator) {
		this.projectionTranslator = translator;
	}

	public Vector3 getPlayerPosn() {
		return new Vector3(POSN_MAPPER.get(player).position);
	}

	// Simple reference to the model
	private static class ModelComponent implements Component {
		private final ModelInstance model;
		public ModelComponent(ModelInstance model) {
			this.model = model;
		}
	}

	// Differentiate player
	public static class Mouseable implements Component {
	}

	public interface ModelInstanceProvider {
		ModelInstance createModel(Vector3 position);
	}
}
