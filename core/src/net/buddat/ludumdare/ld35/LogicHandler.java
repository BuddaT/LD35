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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import net.buddat.ludumdare.ld35.entity.Attractor;
import net.buddat.ludumdare.ld35.entity.AttractorType;
import net.buddat.ludumdare.ld35.entity.CohesionAttractor;
import net.buddat.ludumdare.ld35.entity.CrowdingRepulsor;
import net.buddat.ludumdare.ld35.entity.FixedObjectRepulsor;
import net.buddat.ludumdare.ld35.entity.Movement;
import net.buddat.ludumdare.ld35.entity.Position;
import net.buddat.ludumdare.ld35.entity.PredatorPreyRepulsor;
import net.buddat.ludumdare.ld35.entity.Prey;
import net.buddat.ludumdare.ld35.entity.PreyPredatorAttractor;
import net.buddat.ludumdare.ld35.entity.ProjectionTranslator;
import net.buddat.ludumdare.ld35.game.Level;
import net.buddat.ludumdare.ld35.gfx.IntersectableModel;
import net.buddat.ludumdare.ld35.math.ImmutableVector3;
import net.buddat.ludumdare.ld35.math.MovementCalculator;

public class LogicHandler {
	private static final int NUM_CREATURES = 5;
	private final Family creatures =
			Family.all(Position.class, Movement.class).exclude(Mouseable.class).get();
	private final Family preyFamily =
			Family.all(Prey.class).exclude(Mouseable.class).get();
	private final Family obstacleFamily =
			Family.all(FixedObjectRepulsor.class).get();
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
	private static final ComponentMapper<PredatorPreyRepulsor> PREDATOR_PREY_MAPPER =
			ComponentMapper.getFor(PredatorPreyRepulsor.class);
	private static final ComponentMapper<PreyPredatorAttractor> PREY_PREDATOR_MAPPER =
			ComponentMapper.getFor(PreyPredatorAttractor.class);
	// Lists what the entity is attracted to
	private static final ComponentMapper<AttractedByLister> ATTRACTED_BY_MAPPER =
			ComponentMapper.getFor(AttractedByLister.class);
	private static final ComponentMapper<Prey> PREY_MAPPER =
			ComponentMapper.getFor(Prey.class);
	private static final ComponentMapper<Predator> PREDATOR_MAPPER =
			ComponentMapper.getFor(Predator.class);
	private static final ComponentMapper<PredatorHidden> HIDDEN_PREDATOR_MAPPER =
			ComponentMapper.getFor(PredatorHidden.class);
	private static final ComponentMapper<FixedObjectRepulsor> OBSTACLE_MAPPER =
			ComponentMapper.getFor(FixedObjectRepulsor.class);

	// Distance to player within which evasion is attempted (^ 2)
	private static final float PLAYER_EFFECT_RANGE = 20*20;
	// Speed at which evasion is attempted, units/sec
	private static final float EVASION_SPEED = 30;

	// Speed at which chasing is attempted, units/sec
	private static final float CHASE_SPEED = 30;

	private static final float MAX_SPEED = 120;

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
		speeds.put(AttractorType.CROWDING, 30f);
		speeds.put(AttractorType.COHESION, 3f);
		speeds.put(AttractorType.PLAYER, EVASION_SPEED);
		speeds.put(AttractorType.PREY_PREDATOR, EVASION_SPEED);
		speeds.put(AttractorType.PREDATOR_PREY, CHASE_SPEED);
		for (AttractorType type : AttractorType.values()) {
			if (speeds.get(type) == null) {
				speeds.put(type, 3f);
			}
		}
		PREY_ATTRACTOR_SPEEDS = speeds;
	}

	/**
	 * Provides all attractors in which a prey is interested
	 */
	private static final AttractedByLister PREY_ATTRACTORS_LISTER = new AttractedByLister(new AttractorProvider() {
		@Override
		public List<Attractor> getAttractorsFoundIn(Entity entity) {
			List<Attractor> attractors = new ArrayList<Attractor>();
			for (Attractor attractor : new Attractor[]{
					CROWDING_MAPPER.get(entity),
					COHESION_MAPPER.get(entity),
					PREDATOR_PREY_MAPPER.get(entity)
			}) {
				if (attractor != null) {
					attractors.add(attractor);
				}
			}
			return attractors;
		}
	});

	/**
	 * Provides all attractors in which a predator is interested
	 */
	private static final AttractedByLister PREDATOR_ATTRACTORS_LISTER = new AttractedByLister(new AttractorProvider() {
		@Override
		public List<Attractor> getAttractorsFoundIn(Entity entity) {
			List<Attractor> attractors = new ArrayList<Attractor>();
			Prey prey = PREY_MAPPER.get(entity);
			if (prey != null && !prey.isDead()) {
				PreyPredatorAttractor attractor = PREY_PREDATOR_MAPPER.get(entity);
				if (attractor != null) {
					attractors.add(attractor);
				}
			}
			return attractors;
		}
	});

	private ProjectionTranslator projectionTranslator;

	private final ArrayList<Entity> staleModelEntities = new ArrayList<Entity>();

	/**
	 * @return All entities with stale models
	 */
	public List<Entity> getStaleModelEntities() {
		return staleModelEntities;
	}

	private float getDelta() {
		return Gdx.graphics.getDeltaTime();
	}

	/**
	 * Creates a new creature basic position and movement attributes
	 * @param position Creature position
	 * @param rotation Creature rotation
	 * @return Newly created creature
	 */
	public Entity createNewCreature(Vector3 position, Vector3 rotation) {
		Entity creature = new Entity();
		if (rotation.isZero()) {
			creature.add(new Position(position, DEFAULT_ROTATION.copy()));
		}
		creature.add(new Position(position, rotation));
		creature.add(new Movement(new Vector3(), new Vector3(0, 0, 0)));
		return creature;
	}

	/**
	 * Creates a new prey creature, assigned with all prey-related components
	 * @param position Initial position
	 * @param rotation Initial rotation
	 * @return Prey entity
	 */
	public Entity createNewPrey(Vector3 position, Vector3 rotation) {
		Entity creature = createNewCreature(position, rotation);
		creature.add(new CrowdingRepulsor())
				.add(new CohesionAttractor())
				.add(new PreyPredatorAttractor())
				.add(new Prey())
				.add(PREY_ATTRACTORS_LISTER);
		return creature;
	}

	/**
	 * Creates a new predator creature, assigned with all predator-related components
	 * @param position Initial position
	 * @param rotation Initial rotation
	 * @return Predator entity
	 */
	public Entity createNewPredator(Vector3 position, Vector3 rotation) {
		Entity creature = createNewCreature(position, rotation);
		creature.add(new PredatorPreyRepulsor())
				.add(new Predator())
				.add(PREDATOR_ATTRACTORS_LISTER);
		return creature;
	}

	public void init() {
	}

	private Level getCurrentLevel() {
		return GraphicsHandler.getGraphicsHandler().getWorldRenderer().getCurrentLevel();
	}
	
	public float getRotationAngleTowards(Vector3 from, Vector3 to) {
		double dir = Math.atan2(from.x - to.x, from.z - to.z);
		dir = dir * (180 / Math.PI);
		
		return (float) dir;
	}

	public void update() {
		staleModelEntities.clear();
		Engine engine = getCurrentLevel().getEngine();
		Entity player = getCurrentLevel().getPlayer();
		
		// Move the player towards the 3d position the mouse is pointing to?
		Vector3 worldMousePosn = projectionTranslator.unproject(Gdx.input.getX(), Gdx.input.getY());
		Vector3 position = POSN_MAPPER.get(player).position;
		Vector3 velocity = MVMNT_MAPPER.get(player).velocity;
		velocity.set(worldMousePosn).sub(position);

		Array<IntersectableModel> collisionModels = getCurrentLevel().getCollisionModels();
		IntersectableModel model = MODEL_MAPPER.get(player).model;

		// 90f because the model is wrong
		model.transform.setToRotation(Vector3.Y, getRotationAngleTowards(position, worldMousePosn) + 90f);
		
		if (!collides(player, collisionModels)) {
			position.set(worldMousePosn);
			model.updateCollisions();
		}
		ImmutableArray<Entity> entities = engine.getEntitiesFor(creatures);
		calculateMovementChanges(engine, player, entities);
		applyMovementChanges(entities);
	}

	/**
	 * Calculate the changes in the flock's movement. Note current algorithm is
	 * O(n^2) due to the need to look at all other members of the flock.
	 * Partitioning by location would improve this.
	 */
	private void calculateMovementChanges(Engine engine, Entity player, ImmutableArray<Entity> entities) {

		Position playerPosn = POSN_MAPPER.get(player);

		// First calculate change in position for all objects.
		// Note that re-used of entity collection iterators means you can't
		// have inner loops, hence use of indices.
		for (int i = 0; i < entities.size(); i++) {
			Entity entity = entities.get(i);
			Position posn = POSN_MAPPER.get(entity);
			Movement movement = MVMNT_MAPPER.get(entity);
			Vector3 change;

			Predator predator = entity.getComponent(Predator.class);
			// Attempt to move away from the player
			float dist2ToPlayer = posn.position.dst2(playerPosn.position);
			if (dist2ToPlayer <= PLAYER_EFFECT_RANGE) {
				float speed = 60 * PLAYER_EFFECT_RANGE / (dist2ToPlayer * dist2ToPlayer);
				change = movementCalculator.awayFrom(posn, playerPosn).nor().scl(speed).clamp(0, EVASION_SPEED);
				if (predator != null) {
					// Moving away from the player overrides all else
					prepareMovement(movement, change.scl(getDelta()));
					continue;
				}
			} else {
				change = new Vector3();
			}

			// Try to avoid crowding neighbors, yet keep in distance
			ImmutableArray<Entity> others = engine.getEntitiesFor(creatures);
			// Find out what this entity is attracted to
			EnumMap<AttractorType, Vector3> attractors =
					new EnumMap<AttractorType, Vector3>(AttractorType.class);
			for (AttractorType attractorType : AttractorType.values()) {
				attractors.put(attractorType, new Vector3());
			}
			// Get a lister of attractors in which this entity is interested
			AttractedByLister lister = ATTRACTED_BY_MAPPER.get(entity);
			for (int j = 0; j < others.size(); j++) {
				Entity other = others.get(j);
				if (entity == other) {
					continue;
				}
				Position otherPosn = POSN_MAPPER.get(other);
				float distance = posn.position.dst2(otherPosn.position);
				// List all interesting attractors on the other entity
				for (Attractor attractor : lister.getAttractorsFoundIn(other)) {

					if (distance <= attractor.getMaxRange()) {
						float speed = attractor.getBaseSpeed(distance);
						Vector3 direction = movementCalculator.towards(posn, otherPosn).nor();
						attractors.get(attractor.getAttractorType()).add(direction.scl(speed));
					}
				}
			}
			// Add fixed attractors
			ImmutableArray<Entity> obstacles = engine.getEntitiesFor(obstacleFamily);
			Vector3 obstacleChange = attractors.get(AttractorType.FIXED_OBSTACLE);
			for (int obstacleIdx = 0; obstacleIdx < obstacles.size(); obstacleIdx++) {
				Entity obstacle = obstacles.get(obstacleIdx);
				FixedObjectRepulsor repulsor = OBSTACLE_MAPPER.get(obstacle);
				Position otherPosn = POSN_MAPPER.get(obstacle);
				if (repulsor == null || otherPosn == null) {
					System.out.println("Null found for obstacle " + repulsor + ", " + otherPosn);
					continue;
				}
				float distance = posn.position.dst2(otherPosn.position);
				if (distance <= repulsor.getMaxRange()) {
					float speed = repulsor.getBaseSpeed(distance);
					Vector3 direction = movementCalculator.towards(posn, otherPosn).nor();
					obstacleChange.add(direction.scl(speed));
				}
			}
			// Add all attractions together, clamped by their speed
			for (AttractorType attractorType : AttractorType.values()) {
				Vector3 attractantChange = attractors.get(attractorType);
				float speed = PREY_ATTRACTOR_SPEEDS.get(attractorType);
				change.add(attractantChange.clamp(0, speed));
			}
			// speed is per second, so scale according to seconds
			prepareMovement(movement, change.scl(getDelta()));
		}
	}

	/**
	 * Prepares movement updates based on change
	 * @param movement Movement to be updated
	 * @param change Changes to movement
	 */
	public void prepareMovement(Movement movement, Vector3 change) {
		change.clamp(0, MAX_SPEED);
		// any y values for now are set to default height
		change.y = DEFAULT_HEIGHT;
		// velocity changes to the average between the desired and current. This doesn't work as intended yet
		movement.velocity.add(change).scl(0.5f);
		movement.rotation.set(change).nor();
	}

	private Vector2 createXZVector(Vector3 vector3) {
		return new Vector2(vector3.x, vector3.z);
	}

	private boolean collides(Entity entity, Array<IntersectableModel> boxes) {
		Position posn = POSN_MAPPER.get(entity);
		Movement movement = MVMNT_MAPPER.get(entity);
		IntersectableModel model = MODEL_MAPPER.get(entity).model;
		Vector2 start = createXZVector(posn.position);
		Vector2 end = new Vector2(start).add(createXZVector(movement.velocity));
		for (IntersectableModel boxModel : boxes) {
			if (boxModel == model) {
				continue;
			}

			if (boxModel.lineIntersectsCollision(start, end)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Applies movement changes to positions IFF the entity is not moving into
	 * the bounding box of another.
	 * @param entities All entities to be moved
	 */
	public void applyMovementChanges(ImmutableArray<Entity> entities) {
		// now apply the changes to each position
		Vector3 up = UP.copy();
		float buffer = 0.01f;
		Engine engine = getCurrentLevel().getEngine();

		Array<IntersectableModel> boxes = getCurrentLevel().getCollisionModels();
		for (Entity entity : entities) {
			Position posn = POSN_MAPPER.get(entity);
			Movement movement = MVMNT_MAPPER.get(entity);
			IntersectableModel model = MODEL_MAPPER.get(entity).model;
			// Don't change position if it results in a collision
			if (collides(entity, boxes)) {
				continue;
			}

			Vector3 originalPosition = new Vector3(posn.position);
			posn.position.add(movement.velocity);
			// 90f because the models are wrong
			model.transform.setToRotation(Vector3.Y,
					getRotationAngleTowards(originalPosition, posn.position) + 90f);
			model.transform.setTranslation(posn.position);
			model.updateCollisions();
			Prey prey = PREY_MAPPER.get(entity);
			if (PREY_MAPPER.get(entity) != null) {
				if (!prey.isDead()) {
					prey.setPenned(getCurrentLevel().getSheepPenModel().contains(model));
					if (!prey.isPenned() && HIDDEN_PREDATOR_MAPPER.get(entity) != null) {
						for (IntersectableModel transformer : getCurrentLevel().getWolfTransformModels()) {
							if (transformer.contains(model)) {
								// Remove all prey components and add predator
								entity.remove(Prey.class);
								entity.remove(CrowdingRepulsor.class);
								entity.remove(CohesionAttractor.class);
								entity.remove(PreyPredatorAttractor.class);
								entity.remove(Prey.class);
								entity.remove(AttractedByLister.class);
								entity.remove(PredatorHidden.class);

								entity.add(new PredatorPreyRepulsor())
										.add(new Predator())
										.add(PREDATOR_ATTRACTORS_LISTER);
								staleModelEntities.add(entity);
								break;
							}
						}
					}
					
					if (prey.isPenned()) {
						model.materials.get(1).set(ColorAttribute.createDiffuse(Color.GREEN));
					} else {
						model.materials.get(1).set(ColorAttribute.createDiffuse(Color.WHITE));
					}
				}
			}
			Predator predator = PREDATOR_MAPPER.get(entity);
			if (predator != null) {
				// for each prey, if this predator intersects that prey, kill it.
				ImmutableArray<Entity> potentialPreys = engine.getEntitiesFor(preyFamily);
				for (int i = 0; i < potentialPreys.size(); i++) {
					Entity potentialPrey = potentialPreys.get(i);
					prey = PREY_MAPPER.get(potentialPreys.get(i));
					if (prey == null) {
						continue;
					}
					IntersectableModel preyModel = MODEL_MAPPER.get(potentialPrey).model;
					if (preyModel.intersects(model)) {
						if (!prey.isDead()) {
							preyModel.materials.get(1).set(ColorAttribute.createDiffuse(Color.RED));
							preyModel.transform.translate(0f, 1f, 0f).rotate(Vector3.X, 90).translate(0f, -1f, 0f);
							GraphicsHandler.getGraphicsHandler().getWorldRenderer().getAnimController(preyModel).paused = true;
							
							GraphicsHandler.getGraphicsHandler().getWorldRenderer().wolfHowl.play();
							GraphicsHandler.getGraphicsHandler().getWorldRenderer().killSound.play(0.5f);
						}
						prey.kill();
						// dead prey can't move
						potentialPrey.remove(Movement.class);
						// dead prey has no flock or predator attractors
						potentialPrey.remove(CrowdingRepulsor.class);
						potentialPrey.remove(CohesionAttractor.class);
						potentialPrey.remove(PreyPredatorAttractor.class);
					}
				}
			}
		}
	}

	public int getNumPenned() {
		ImmutableArray<Entity> entities = getCurrentLevel().getEngine().getEntitiesFor(preyFamily);
		int count = 0;
		for (Entity entity : entities) {
			Prey prey = PREY_MAPPER.get(entity);
			if (!prey.isDead() && prey.isPenned()) {
				count++;
			}
		}
		return count;
	}

	public int getNumDead() {
		ImmutableArray<Entity> entities = getCurrentLevel().getEngine().getEntitiesFor(preyFamily);
		int count = 0;
		for (Entity entity : entities) {
			Prey prey = PREY_MAPPER.get(entity);
			if (prey.isDead()) {
				count++;
			}
		}
		return count;
	}

	/**
	 * 	Creates models for each of the creatures, using the callback and assigning models to each creature
	 */
	public void createCreatures(ModelInstanceProvider modelProvider) {
		Engine engine = GraphicsHandler.getGraphicsHandler().getWorldRenderer().getCurrentLevel().getEngine();
		
		for (Entity entity : engine.getEntitiesFor(creatures)) {
			IntersectableModel model = modelProvider.createModel(entity, POSN_MAPPER.get(entity).position);
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
	public static class ModelComponent implements Component {
		public final IntersectableModel model;
		public ModelComponent(IntersectableModel model) {
			this.model = model;
		}
	}

	// Differentiate player
	public static class Mouseable implements Component {
	}

	public interface ModelInstanceProvider {
		IntersectableModel createModel(Entity e, Vector3 position);
	}

	private interface AttractorProvider {
		List<Attractor> getAttractorsFoundIn(Entity entity);
	}

	/**
	 * When attached to an entity, lists all attractors on another entity in
	 * which this one is interested.
	 */
	private static class AttractedByLister implements Component {
		private final AttractorProvider provider;

		public AttractedByLister(AttractorProvider provider) {
			this.provider = provider;
		}

		/**
		 * Lists all attractors on the specified entity in which this entity
		 * is interested
		 * @param entity Entity on which interesting attractors are listed
		 * @return List of attractors
		 */
		List<Attractor> getAttractorsFoundIn(Entity entity) {
			return provider.getAttractorsFoundIn(entity);
		}
	}

	public static class PredatorHidden implements Component {};

	public static class Predator implements Component {};
}
