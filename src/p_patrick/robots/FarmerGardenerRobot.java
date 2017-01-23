package p_patrick.robots;

import battlecode.common.*;
import ddframework.robots.SmartBaseRobot;
import ddframework.util.RandomUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class FarmerGardenerRobot extends SmartBaseRobot {

	private static final int RAND_EXPLORES = 3;
	private static final int REVERSES = 3;
	private static final float TREE_RING_SEARCH_INCREMENT_RAD = (float) ((2d * Math.PI) / 12d);

	private static final int STATE_EXPLORE_1 = 0;
	private static final int STATE_EXPLORE_RAND = 1;
	private static final int STATE_EXPLORE_2 = 2;
	private static final int STATE_REVERSE = 3;
	private static final int STATE_TREE_RING = 4;

	private static final int BULLET_BUILD_ROBOT_THRESHOLD = 150;

	private final Team mMyTeam;

	private int mCurrentState;
	private Direction mExploreDir;
	private int mRandExplores;
	private int mReverses;
	private HashMap<RobotType, Integer> productionCounts = new HashMap<>();
	private HashMap<RobotType, Integer> targetUnitProductionCounts = new HashMap<>();


	public FarmerGardenerRobot(RobotController controller) {
		super(controller);

		mMyTeam = controller.getTeam();

		MapLocation myLocation = controller.getLocation();

		// Initialize our target production counts
		targetUnitProductionCounts.put(RobotType.SCOUT, 2);
		targetUnitProductionCounts.put(RobotType.LUMBERJACK, 1);
		targetUnitProductionCounts.put(RobotType.SOLDIER, 1);
		//targetUnitProductionCounts.put(RobotType.TANK, 0);

		targetUnitProductionCounts.forEach((t, c) -> {
			// Initialize our production counts per robot type
			productionCounts.put(t, 0);
		});

		// move directly away from the Archon that spawned us (or whichever one is first in the array...)
		final RobotInfo[] nearbyRobots = controller.senseNearbyRobots();
		for (RobotInfo robot : nearbyRobots) {
			if (robot.getType() == RobotType.ARCHON) {
				mExploreDir = new Direction(robot.getLocation(), myLocation);
				break;
			}
		}

		// no Archon nearby, (very improbable) so pick a random direction
		if (mExploreDir == null) {
			Random rand = RandomUtil.getRandom();
			mExploreDir = new Direction(rand.nextInt(3) - 1, rand.nextInt(3) - 1);
		}

		mCurrentState = STATE_EXPLORE_1;
	}

	@Override
	protected void onGameRound(RobotController rc) throws Exception {
		super.onGameRound(rc);

		int nextState;
		final float bulletCount = rc.getTeamBullets();

		// For each target unit production count we have, check to see if we should built it and if so, do it.
		// TODO: this isn't great because it doesn't track deaths.  Need to keep producing.
		for (Map.Entry<RobotType, Integer> entry : targetUnitProductionCounts.entrySet()) {
			RobotType type = entry.getKey();
			if (shouldBuildRobot(type, bulletCount)) {
				Direction randDir = RandomUtil.randomDirection();
				attemptBuildRobotType(type, randDir, rc);
			}
		}

		switch (mCurrentState) {
			default:
			case STATE_EXPLORE_1:
				// move in the explore direction until we hit something

				if (!rc.hasMoved() && rc.canMove(mExploreDir)) {
					rc.move(mExploreDir);
					nextState = STATE_EXPLORE_1;
				} else {
					nextState = STATE_EXPLORE_RAND;
					mRandExplores = RAND_EXPLORES;
				}

				break;
			case STATE_EXPLORE_RAND:
				// move randomly in case the thing we hit was also moving

				Direction randDir = RandomUtil.randomDirection();
				if (!rc.hasMoved() && rc.canMove(randDir)) {
					rc.move(randDir);
				}
				mRandExplores--;
				if (mRandExplores <= 0) {
					nextState = STATE_EXPLORE_2;
				} else {
					nextState = STATE_EXPLORE_RAND;
				}
				break;
			case STATE_EXPLORE_2:
				// continue exploring in the explore direction until we hit something again

				if (!rc.hasMoved() && rc.canMove(mExploreDir)) {
					rc.move(mExploreDir);
					nextState = STATE_EXPLORE_2;
				} else {
					mReverses = REVERSES;
					nextState = STATE_REVERSE;
				}
				break;
			case STATE_REVERSE:
				// back up a bit to make room for our tree ring and the thing we hit

				Direction opposite = mExploreDir.opposite();
				if (!rc.hasMoved() && rc.canMove(opposite)) {
					rc.move(opposite);
				}
				mReverses--;
				if (mReverses <= 0) {
					nextState = STATE_TREE_RING;
				} else {
					nextState = STATE_REVERSE;
				}
				break;
			case STATE_TREE_RING:
				// build and maintain the tree ring

				// TODO instead of a ring, build trees within x distance of other trees and archons.
				// ...This will prevent getting stuck and allow room for building units

				// try to fill in the ring
				final float startDir = mExploreDir.radians;
				final float endDir = (float) (startDir + (Math.PI)); // Temporary fix. semi-circle to prevent blocking
				for (float f = startDir; f < endDir; f += TREE_RING_SEARCH_INCREMENT_RAD) {
					Direction treeDir = new Direction(f);

					if (rc.canPlantTree(treeDir)) {
						rc.plantTree(treeDir);
						break;
					}
				}

				// water
				TreeInfo[] treeInfos = rc.senseNearbyTrees();
				RandomUtil.shuffle(treeInfos);
				for (TreeInfo tree : treeInfos) {
					if (tree.team.equals(mMyTeam)) {
						float health = tree.getHealth();
						float maxHealth = tree.getMaxHealth();
						if (health / maxHealth < 0.7f) {
							int treeId = tree.getID();
							if (rc.canWater(treeId)) {
								rc.water(treeId);
								break;
							}
						}
					}
				}

				nextState = STATE_TREE_RING;
				break;
		}

		mCurrentState = nextState;
	}

	private boolean shouldBuildRobot(RobotType type, float bulletCount) throws Exception {
		System.out.print("productionCounts for " + type + ": " + productionCounts.get(type));
		return productionCounts.get(type) < targetUnitProductionCounts.get(type) &&
				bulletCount > BULLET_BUILD_ROBOT_THRESHOLD;
	}

	private void attemptBuildRobotType(RobotType type, Direction dir, RobotController rc) throws Exception {
		// attempt to build the robot by rotating in 45 degree increments until able to build.
		for (int i = 0; i < 8; ++i) {
			// can I build at this degree?
			if (rc.canBuildRobot(type, dir)) {
				rc.buildRobot(type, dir);
				// increment the production count for this unit
				int newCount = productionCounts.get(type) + 1;
				productionCounts.put(type, newCount);
				return;
			}
			// do the rotate if can't build here
			dir = dir.rotateRightDegrees(45);
		}

	}

}
