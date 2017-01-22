package ddframework.robots;

import battlecode.common.*;
import ddframework.broadcast.SharedBuffer;
import ddframework.util.CombatUtil;
import ddframework.util.Navigation;

public abstract strictfp class BaseRobot {
	private SharedBuffer mSharedBuffer;
	private final RobotController mRobotController;

	protected static Team myTeam;
	protected static Team enemyTeam;

	protected int myId;
	protected MapLocation myLocation;
	protected TreeInfo[] visibleTrees;
	protected RobotInfo[] visibleHostiles;
	protected RobotInfo[] visibleFriendlies;

	public BaseRobot(RobotController controller) {
		mRobotController = controller;
		myTeam = controller.getTeam();
		enemyTeam = myTeam.opponent();
		myId = controller.getID();
	}

	protected RobotController getRc() {
		return mRobotController;
	}

	protected SharedBuffer getSharedBuffer() throws Exception {
		if (mSharedBuffer == null) {
			mSharedBuffer = new SharedBuffer(mRobotController);
		}
		return mSharedBuffer;
	}

	/**
	 * Perform all logic for a single round in this method.
	 */
	@SuppressWarnings("InfiniteLoopStatement")
	public void loop() {
		while (true) {
			try {
				// Get current location every round
				myLocation = mRobotController.getLocation();
				// Sense nearby bots and trees every round
				senseNearbyBots();
				senseNearbyTrees();
				// Prioritize dodging incoming bullets every round
				CombatUtil.dodgeIncomingBullets(mRobotController);
				// do one game round
				onGameRound(mRobotController);
			} catch (Throwable t) {
				System.err.println("Error in " + mRobotController.getType());
				t.printStackTrace();
			}

			try {
				// flush the buffer
				if (mSharedBuffer != null) {
					mSharedBuffer.flush();
				}
			} catch (Throwable t) {
				System.err.println("Error cleaning up " + mRobotController.getType());
				t.printStackTrace();
			}

			Clock.yield();
		}
	}

	protected abstract void onGameRound(RobotController rc) throws Exception;

	private void senseNearbyTrees() {
		visibleTrees = getRc().senseNearbyTrees();
	}

	private void senseNearbyBots() {
		visibleHostiles = mRobotController.senseNearbyRobots(myLocation, mRobotController.getType().sensorRadius, enemyTeam);
		visibleFriendlies = mRobotController.senseNearbyRobots(myLocation, mRobotController.getType().sensorRadius, myTeam);
	}
}
