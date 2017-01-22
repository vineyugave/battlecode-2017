package ddframework.robots;

import battlecode.common.*;
import ddframework.broadcast.SharedBuffer;

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

	@SuppressWarnings("InfiniteLoopStatement")
	public void loop() {
		while (true) {
			try {
				// do one game round
				myLocation = mRobotController.getLocation();
				dodge();
				senseNearbyBots();
				senseNearbyTrees();
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

	/**
	 * Perform all logic for a single round in this method.
	 */
	protected abstract void onGameRound(RobotController rc) throws Exception;

	/**
	 * Attempts to move in a given direction, while avoiding small obstacles directly in the path.
	 *
	 * @param dir The intended direction of movement
	 * @return true if a move was performed
	 */
	protected boolean tryMove(Direction dir) throws GameActionException {
		return tryMove(dir, 20, 3);
	}

	/**
	 * Attempts to move in a given direction, while avoiding small obstacles direction in the path.
	 *
	 * @param dir           The intended direction of movement
	 * @param degreeOffset  Spacing between checked directions (degrees)
	 * @param checksPerSide Number of extra directions checked on each side, if intended direction was unavailable
	 * @return true if a move was performed
	 * @throws GameActionException
	 */
	protected boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {
		// First, try intended direction
		if (mRobotController.canMove(dir)) {
			mRobotController.move(dir);
			return true;
		}

		// Now try a bunch of similar angles
		int currentCheck = 1;

		while (currentCheck <= checksPerSide) {
			// Try the offset of the left side
			if (mRobotController.canMove(dir.rotateLeftDegrees(degreeOffset * currentCheck))) {
				mRobotController.move(dir.rotateLeftDegrees(degreeOffset * currentCheck));
				return true;
			}
			// Try the offset on the right side
			if (mRobotController.canMove(dir.rotateRightDegrees(degreeOffset * currentCheck))) {
				mRobotController.move(dir.rotateRightDegrees(degreeOffset * currentCheck));
				return true;
			}
			// No move performed, try slightly further
			currentCheck++;
		}

		// A move never happened, so return false.
		return false;
	}

	/**
	 * A slightly more complicated example function, this returns true if the given bullet is on a collision
	 * course with the current robot. Doesn't take into account objects between the bullet and this robot.
	 *
	 * @param bullet The bullet in question
	 * @return True if the line of the bullet's path intersects with this robot's current position.
	 */
	protected boolean willCollideWithMe(BulletInfo bullet) {
		MapLocation myLocation = mRobotController.getLocation();

		// Get relevant bullet information
		Direction propagationDirection = bullet.dir;
		MapLocation bulletLocation = bullet.location;

		// Calculate bullet relations to this robot
		Direction directionToRobot = bulletLocation.directionTo(myLocation);
		float distToRobot = bulletLocation.distanceTo(myLocation);
		float theta = propagationDirection.radiansBetween(directionToRobot);

		// If theta > 90 degrees, then the bullet is traveling away from us and we can break early
		if (Math.abs(theta) > Math.PI / 2) {
			return false;
		}

		// distToRobot is our hypotenuse, theta is our angle, and we want to know this length of the opposite leg.
		// This is the distance of a line that goes from myLocation and intersects perpendicularly with propagationDirection.
		// This corresponds to the smallest radius circle centered at our location that would intersect with the
		// line that is the path of the bullet.
		float perpendicularDist = (float) Math.abs(distToRobot * Math.sin(theta)); // soh cah toa :)

		return (perpendicularDist <= mRobotController.getType().bodyRadius);
	}

	public void senseNearbyTrees() {
		visibleTrees = getRc().senseNearbyTrees();
	}

	public void senseNearbyBots() {
		visibleHostiles = mRobotController.senseNearbyRobots(myLocation, mRobotController.getType().sensorRadius, enemyTeam);
		visibleFriendlies = mRobotController.senseNearbyRobots(myLocation, mRobotController.getType().sensorRadius, myTeam);
	}

	protected void dodge()
	{
		BulletInfo[] bullets = mRobotController.senseNearbyBullets();
		for (BulletInfo bullet : bullets) {
			if (willCollideWithMe(bullet)) {
				trySidestep(bullet);
			}
		}
	}

	protected void trySidestep(BulletInfo b)
	{
		try
		{
			MapLocation myLoc = mRobotController.getLocation();
			Direction bulletToRobot = b.location.directionTo(myLoc);
			float theta = b.dir.radiansBetween(bulletToRobot);
			if(theta < 0)
			{
				// Bullet on My Left
				Direction moveDir = myLoc.directionTo(b.location).rotateRightDegrees(90);
				tryMove(moveDir);
			}
			else
			{
				// Bullet on My Right
				Direction moveDir = myLoc.directionTo(b.location).rotateLeftDegrees(90);
				tryMove(moveDir);
			}
		}
		catch (GameActionException e)
		{
			System.out.println("Sidestep Attempt Failed!");
			e.printStackTrace();
		}
	}
}
