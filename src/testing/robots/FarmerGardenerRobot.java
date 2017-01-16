package testing.robots;

import battlecode.common.*;
import com.dd.framework.BaseRobot;
import com.dd.framework.Utils;

import java.util.Random;

public class FarmerGardenerRobot extends BaseRobot {

	private static final int RAND_EXPLORES = 10;
	private static final int REVERSES = 2;
	private static final float TREE_RING_SEARCH_INCREMENT_RAD = (float) ((2d * Math.PI) / 12d);

	private static final int STATE_EXPLORE_1 = 0;
	private static final int STATE_EXPLORE_RAND = 1;
	private static final int STATE_EXPLORE_2 = 2;
	private static final int STATE_REVERSE = 3;
	private static final int STATE_TREE_RING = 4;

	private Team mMyTeam;
	private Team mEnemyTeam;
	private int mCurrentState;
	private Direction mExploreDir;
	private int mRandExplores;
	private int mReverses;

	public FarmerGardenerRobot(RobotController controller) {
		super(controller);

		mMyTeam = controller.getTeam();
		mEnemyTeam = mMyTeam.opponent();

		MapLocation location = controller.getLocation();
		Random rand = new Random((long) (location.x + location.y) + Utils.RANDOM_SEED);

		mCurrentState = STATE_EXPLORE_1;
		mExploreDir = new Direction(rand.nextInt(3) - 1, rand.nextInt(3) - 1);
	}

	@Override
	protected void onGameRound(RobotController rc) throws Exception {
		int nextState;
		switch (mCurrentState) {
			default:
			case STATE_EXPLORE_1:
				if (rc.canMove(mExploreDir)) {
					rc.move(mExploreDir);
					nextState = STATE_EXPLORE_1;
				} else {
					nextState = STATE_EXPLORE_RAND;
					mRandExplores = RAND_EXPLORES;
				}
				break;
			case STATE_EXPLORE_RAND:
				Direction randDir = Utils.randomDirection();
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
				if (rc.canMove(mExploreDir)) {
					rc.move(mExploreDir);
					nextState = STATE_EXPLORE_2;
				} else {
					mReverses = REVERSES;
					nextState = STATE_REVERSE;
				}
				break;
			case STATE_REVERSE:
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
				Utils.shuffle(treeInfos);
				for (TreeInfo tree : treeInfos) {
					if (tree.team.equals(mMyTeam)) {
						float health = tree.getHealth();
						float maxHealth = tree.getMaxHealth();
						if (health / maxHealth <0.7f) {
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
