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
import net.buddat.ludumdare.ld35.math.ImmutableVector3;
import net.buddat.ludumdare.ld35.math.MovementCalculator;

public class LogicHandler {
	private static final int NUM_CREATURES = 5;
	private Family creatures;
	private Family sheep;
	private static final ComponentMapper<Position> POSN_MAPPER =
			ComponentMapper.getFor(Position.class);
	private static final ComponentMapper<Movement> MVMNT_MAPPER =
			ComponentMapper.getFor(Movement.class);
	private static final ComponentMapper<ModelComponent> MODEL_MAPPER =
			ComponentMapper.getFor(ModelComponent.class);
	private static final ComponentMapper<CrowdingRepulsor> CROWDING_MAPPER =
			ComponentMapper.getFor(CrowdingRepulsor.class);
	private static final ComponentMapper<CohesionAttractor> COHESION_MAPPER =
			ComponentMapper.getFor(CohesionAttractor.class);

	// Distance to player within which evasion is attempted (^ 2)
	private static final float PLAYER_EFFECT_RANGE = 20*20;
	// Speed at which evasion is attempted
	private static final float EVASION_SPEED = 5;

	private static final float MAX_SPEED = 2;

	// Default height above the "ground" at which to draw entities
	private static final float DEFAULT_HEIGHT = 0;
	private static final ImmutableVector3 DEFAULT_ROTATION =
			new ImmutableVector3(0, 0, 1);
	private static final ImmutableVector3 UP = new ImmutableVector3(0, 1, 0);

	private final MovementCalculator movementCalculator = new MovementCalculator(DEFAULT_ROTATION);

	/**
	 * Max speed at which prey is attracted to/repulsed by things
	 */
	private static final EnumMap<AttractorType, Float> PREY_ATTRACTOR_SPEEDS;
	static {
		EnumMap<AttractorType, Float> speeds =
				new EnumMap<AttractorType, Float>(AttractorType.class);
		speeds.put(AttractorType.CROWDING, 0.5f);
		speeds.put(AttractorType.COHESION, 0.05f);
		speeds.put(AttractorType.PLAYER, EVASION_SPEED);
		speeds.put(AttractorType.PREY_PREDATOR, EVASION_SPEED);
		for (AttractorType type : AttractorType.values()) {
			if (speeds.get(type) == null) {
				speeds.put(type, 0.05f);
			}
		}
		PREY_ATTRACTOR_SPEEDS = speeds;
	}

	private ProjectionTranslator projectionTranslator;

	public Entity createNewCreature(Vector3 position, Vector3 rotation) {
		Entity creature = new Entity();
		if (rotation.isZero()) {
			creature.add(new Position(position, DEFAULT_ROTATION.copy()));
		}
		creature.add(new Position(position, rotation));
		creature.add(new Movement(new Vector3(), new Vector3(0, 0, 0)));
		return creature;
	}

	public Entity createNewPrey(Vector3 position, Vector3 rotation) {
		Entity creature = createNewCreature(position, rotation);
		creature.add(new CrowdingRepulsor())
				.add(new CohesionAttractor())
				.add(new Prey());
		return creature;
	}

	public Entity createNewPredator(Vector3 position, Vector3 rotation) {
		Entity creature = createNewCreature(position, rotation);
		creature.add(new PredatorPreyRepulsor())
				.add(new Predator());
		return creature;
	}

	private List<FlockAttractor> getPreyAttractors(Entity entity) {
		List<FlockAttractor> attractors = new ArrayList<FlockAttractor>();
		for (FlockAttractor attractor : new FlockAttractor[] {
				CROWDING_MAPPER.get(entity),
				COHESION_MAPPER.get(entity)}) {
			if (attractor != null) {
				attractors.add(attractor);
			}
		}
		return attractors;
	}

	public void init() {
		creatures = Family.all(Position.class, Movement.class).exclude(Mouseable.class).get();
		sheep = Family.all(Prey.class).get();
	}

	public void update() {
		Engine engine = GraphicsHandler.getGraphicsHandler().getWorldRenderer().getCurrentLevel().getEngine();
		Entity player = GraphicsHandler.getGraphicsHandler().getWorldRenderer().getCurrentLevel().getPlayer();
		
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
		Engine engine = GraphicsHandler.getGraphicsHandler().getWorldRenderer().getCurrentLevel().getEngine();
		Entity player = GraphicsHandler.getGraphicsHandler().getWorldRenderer().getCurrentLevel().getPlayer();
		
		ImmutableArray<Entity> entities = engine.getEntitiesFor(sheep);
		Position playerPosn = POSN_MAPPER.get(player);

		// First calculate change in position for all objects.
		for (int i = 0; i < entities.size(); i++) {
			Entity entity = entities.get(i);
			Position posn = POSN_MAPPER.get(entity);
			Movement mvmnt = MVMNT_MAPPER.get(entity);
			Vector3 change;

			// Attempt to move away from the player
			float dist2ToPlayer = posn.position.dst2(playerPosn.position);
			if (dist2ToPlayer <= PLAYER_EFFECT_RANGE) {
				float speed = PLAYER_EFFECT_RANGE/(dist2ToPlayer * dist2ToPlayer);
				change = movementCalculator.awayFrom(posn, playerPosn).nor().scl(speed).clamp(0, EVASION_SPEED);
			} else {
				change = new Vector3();
			}

			// Try to avoid crowding neighbors, yet keep in distance
			ImmutableArray<Entity> others = engine.getEntitiesFor(creatures);
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
				// Calculate total attraction for each attraction type
				for (FlockAttractor attractor : getPreyAttractors(entity)) {
					if (distance <= attractor.getMaxRange()) {
						float speed = attractor.getBaseSpeed(distance);
						Vector3 direction = movementCalculator.towards(posn, otherPosn).nor();
						attractors.get(attractor.getAttractorType()).add(direction.scl(speed));
					}
				}
			}

			// Add all attractions together, clamped by their speed
			for (AttractorType attractorType : AttractorType.values()) {
				Vector3 attractantChange = attractors.get(attractorType);
				float speed = PREY_ATTRACTOR_SPEEDS.get(attractorType);
				change.add(attractantChange.clamp(0, speed));
			}

			change.clamp(0, MAX_SPEED);
			// any y values for now are set to default height
			change.y = DEFAULT_HEIGHT;
			// velocity changes to the average between the desired and current. This doesn't work as intended yet
			mvmnt.velocity.add(change).scl(0.5f);
			mvmnt.rotation.set(change).nor();
		}

		// now apply the changes to each position
		Vector3 up = UP.copy();
		for (Entity entity : entities) {
			Position posn = POSN_MAPPER.get(entity);
			Movement movement = MVMNT_MAPPER.get(entity);
			posn.position.add(movement.velocity);
			// maybe we shouldn't be transforming it from within the logic
			// but whatever. hack hack hack, hackity hack
			ModelInstance model = MODEL_MAPPER.get(entity).model;
			Vector3 lookDirection = movementCalculator.calculateNewDirection(posn.rotation, movement.rotation);
			if (lookDirection.isZero()) {
				System.out.println("zero lookdirection");
			}
			posn.rotation = lookDirection;
			model.transform.setToLookAt(lookDirection, up).setTranslation(posn.position);
		}
	}

	public void createCreatures(ModelInstanceProvider modelProvider) {
		Engine engine = GraphicsHandler.getGraphicsHandler().getWorldRenderer().getCurrentLevel().getEngine();
		
		for (Entity entity : engine.getEntitiesFor(creatures)) {
			ModelInstance model = modelProvider.createModel(POSN_MAPPER.get(entity).position);
			entity.add(new ModelComponent(model));
		}
	}

	public void setProjectionTranslator(ProjectionTranslator translator) {
		this.projectionTranslator = translator;
	}

	public Vector3 getPlayerPosn() {
		Entity player = GraphicsHandler.getGraphicsHandler().getWorldRenderer().getCurrentLevel().getPlayer();
		
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

	/**
	 * Marker class for prey
	 */
	private class Prey implements Component {};

	/**
	 * Marker class for predators
	 */
	private class Predator implements Component {};
}
