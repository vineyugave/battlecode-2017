package p_experiment.robots;

import battlecode.common.*;
import ddframework.robots.BaseRobot;
import ddframework.util.RandomUtil;

import java.util.Random;

public class FarmerGardenerRobot extends BaseRobot {

	private static final int RAND_EXPLORES = 3;
	private static final int REVERSES = 3;
	private static final float TREE_RING_SEARCH_INCREMENT_RAD = (float) ((2d * Math.PI) / 12d);

	private static final int STATE_EXPLORE_1 = 0;
	private static final int STATE_EXPLORE_RAND = 1;
	private static final int STATE_EXPLORE_2 = 2;
	private static final int STATE_REVERSE = 3;
	private static final int STATE_TREE_RING = 4;

	private final Team mMyTeam;

	private int mCurrentState;
	private Direction mExploreDir;
	private int mRandExplores;
	private int mReverses;

	public FarmerGardenerRobot(RobotController controller) {
		super(controller);

		mMyTeam = controller.getTeam();

		MapLocation myLocation = controller.getLocation();

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
		int nextState;
		switch (mCurrentState) {
			default:
			case STATE_EXPLORE_1:
				// move in the explore direction until we hit something

				if (rc.canMove(mExploreDir)) {
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
				if (rc.canMove(randDir)) {
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

				if (rc.canMove(mExploreDir)) {
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
				if (rc.canMove(opposite)) {
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

				// try to fill in the ring
				final float startDir = mExploreDir.radians;
				final float endDir = (float) (startDir + (2 * Math.PI));
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

}
