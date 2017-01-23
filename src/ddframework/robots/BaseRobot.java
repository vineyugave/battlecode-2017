package ddframework.robots;

import battlecode.common.*;
import ddframework.broadcast.SharedBuffer;
import ddframework.util.RandomUtil;

public abstract strictfp class BaseRobot {

	private final RobotController mRobotController;

	private final int mId;
	private final Team mTeam;
	private final Team mEnemyTeam;

	private SharedBuffer mSharedBuffer;

	public BaseRobot(RobotController rc) {
		mRobotController = rc;

		mId = rc.getID();
		mTeam = rc.getTeam();
		mEnemyTeam = mTeam.opponent();
	}

	@SuppressWarnings("InfiniteLoopStatement")
	public final void loop() {
		while (true) {
			try {
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

	/**
	 * Perform all logic for a single round in this method.
	 */
	protected abstract void onGameRound(RobotController rc) throws Exception;

	public int getId() {
		return mId;
	}

	public Team getTeam() {
		return mTeam;
	}

	public Team getEnemyTeam() {
		return mEnemyTeam;
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

	protected void attackAndFollow(RobotInfo enemyRobot) throws GameActionException {
		RobotController rc = getRc();
		MapLocation enemyLocation = enemyRobot.getLocation();
		MapLocation myLocation = rc.getLocation();
		Direction toEnemy = myLocation.directionTo(enemyLocation);
		Direction fromEnemy = enemyLocation.directionTo(myLocation);
		Integer keepDistance = 1;

		// We're scared of lumberjacks.  They're mean.
		if (enemyRobot.type == RobotType.LUMBERJACK) {
			keepDistance = 6;
		}
		try {
			if (!rc.hasMoved()){
				if (!enemyLocation.isWithinDistance(myLocation, rc.getType().strideRadius * keepDistance)) {
					tryMove(toEnemy);
				} else {
					tryMove(fromEnemy);
				}
			}

			if (!rc.hasAttacked()) {
				if (rc.canFireSingleShot() && safeToFire(toEnemy)) {
					rc.fireSingleShot(toEnemy);
				}
			}
		} catch (GameActionException e) {
			System.out.println("Exception in CombatUtil.attackAndFollow: " + e);
		}
	}

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
		final RobotController rc = getRc();

		// If we can't move, then return.
		if (rc.hasMoved()) {
			return false;
		}
		try {
			// First, try intended direction
			if (rc.canMove(dir)) {
				rc.move(dir);
				return true;
			}

			// Now try a bunch of similar angles
			int currentCheck = 1;

			while (currentCheck <= checksPerSide) {
				// Try the offset of the left side
				if (rc.canMove(dir.rotateLeftDegrees(degreeOffset * currentCheck))) {
					rc.move(dir.rotateLeftDegrees(degreeOffset * currentCheck));
					return true;
				}
				// Try the offset on the right side
				if (rc.canMove(dir.rotateRightDegrees(degreeOffset * currentCheck))) {
					rc.move(dir.rotateRightDegrees(degreeOffset * currentCheck));
					return true;
				}
				// No move performed, try slightly further
				currentCheck++;
			}
		} catch (GameActionException e) {
			System.out.println("Exception in Navigation.tryMove: "+e);
		}

		// A move never happened, so return false.
		return false;
	}

	protected RobotInfo findClosestRobot(RobotInfo[] robots) throws GameActionException {
		final RobotController rc = getRc();

		float closestDist = 1000000;
		RobotInfo closestRobot = null;
		for (RobotInfo robotInfo : robots) {
			float dist = robotInfo.location.distanceSquaredTo(rc.getLocation());
			if (dist < closestDist) {
				closestDist = dist;
				closestRobot = robotInfo;
			}
		}
		return closestRobot;
	}

	protected TreeInfo findClosestTree(TreeInfo[] trees) throws GameActionException {
		final RobotController rc = getRc();

		float closestDist = 1000000;
		TreeInfo closestTree = null;
		for (TreeInfo treeInfo : trees) {
			float dist = treeInfo.location.distanceSquaredTo(rc.getLocation());
			if (dist < closestDist) {
				closestDist = dist;
				closestTree = treeInfo;
			}
		}
		return closestTree;
	}

	protected boolean follow(RobotInfo robot) {
		final RobotController rc = getRc();
		if (rc.hasMoved()) {
			return false;
		}
		try {
			MapLocation robotLocation = robot.getLocation();
			MapLocation myLocation = rc.getLocation();
			Direction toEnemy = myLocation.directionTo(robotLocation);

			// Move toward robot if I can move and the target is not within stride distance
			if (!rc.hasMoved() && robotLocation.isWithinDistance(myLocation,rc.getType().strideRadius)) {
				return tryMove(toEnemy);
			}
		} catch (GameActionException e) {
			System.out.println("Exception in follow: "+e);
			return false;
		}
		return false;
	}

	protected boolean patrol(RobotInfo robot) {
		final RobotController rc = getRc();
		if (rc.hasMoved()) {
			return false;
		}
		try {
			MapLocation robotLocation = robot.getLocation();
			MapLocation myLocation = rc.getLocation();
			Direction toRobot = myLocation.directionTo(robotLocation);
			Direction awayFromRobot = robotLocation.directionTo(myLocation);

			float minDistance = rc.getType().strideRadius;
			float maxDistance = rc.getType().sensorRadius - rc.getType().strideRadius;
			float currentDistance = myLocation.distanceTo(robotLocation);

			System.out.print("PATROL distances: min:" + minDistance + " max:" + maxDistance + " cur:" + currentDistance);

			if (currentDistance > maxDistance)
			{
				rc.setIndicatorLine(myLocation, robotLocation.add(toRobot), 20, 20, 255);
				return tryMove(toRobot);
			}

			if (currentDistance < minDistance)
			{
				rc.setIndicatorLine(myLocation, robotLocation.add(awayFromRobot), 20, 20, 255);
				return tryMove(awayFromRobot);
			}

			return tryMove(RandomUtil.randomDirection());

		} catch (GameActionException e) {
			System.out.println("Exception in Navigation.patrol: "+e);
			return false;
		}
	}

	/**
	 * A slightly more complicated example function, this returns true if the given bullet is on a collision
	 * course with the current robot. Doesn't take into account objects between the bullet and this robot.
	 *
	 * @param bullet The bullet in question
	 * @return True if the line of the bullet's path intersects with this robot's current position.
	 */
	protected boolean willCollideWithMe(BulletInfo bullet) {
		final RobotController rc = getRc();
		MapLocation myLocation = rc.getLocation();

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
		// This is the distance of a line that goes from mRoundStartLocation and intersects perpendicularly with propagationDirection.
		// This corresponds to the smallest radius circle centered at our location that would intersect with the
		// line that is the path of the bullet.
		float perpendicularDist = (float) Math.abs(distToRobot * Math.sin(theta)); // soh cah toa :)

		return (perpendicularDist <= rc.getType().bodyRadius);
	}

	protected void dodgeIncomingBullets() {
		final RobotController rc = getRc();

		if (rc.hasMoved()){
			// If robot has already moved this turn then we shouldn't try dodging to save byte count
			return;
		}
		BulletInfo[] bullets = rc.senseNearbyBullets();
		for (BulletInfo bullet : bullets) {
			if (willCollideWithMe(bullet)) {
				trySidestep(bullet);
			}
		}
	}

	protected void trySidestep(BulletInfo b) {
		final RobotController rc = getRc();

		if (rc.hasMoved()){
			// If robot has already moved this turn then we shouldn't try dodging to save byte count
			return;
		}
		try {
			MapLocation myLoc = rc.getLocation();
			Direction bulletToRobot = b.location.directionTo(myLoc);
			float theta = b.dir.radiansBetween(bulletToRobot);
			if(theta < 0) {
				// Bullet on My Left
				Direction moveDir = myLoc.directionTo(b.location).rotateRightDegrees(90);
				tryMove(moveDir);
			} else {
				// Bullet on My Right
				Direction moveDir = myLoc.directionTo(b.location).rotateLeftDegrees(90);
				tryMove(moveDir);
			}
		} catch (GameActionException e) {
			System.out.println("Exception in trySidestep: " + e);
		}
	}

	protected boolean safeToFireAtTarget(RobotInfo target)
	{
		final RobotController rc = getRc();

		return safeToFire(rc.getLocation().directionTo(target.getLocation()));
	}

	protected boolean safeToFire(Direction dir)
	{
		final RobotController rc = getRc();

		float DISTANCE_INCREMENT = 0.3f; // Magic numbers!
		float maxTestDistance = rc.getType().sensorRadius;
		float testDistance = rc.getType().bodyRadius + 0.1f;

		MapLocation testLocation;
		MapLocation myLocation = rc.getLocation();

		// increment the test distance while it is less than the max.  testing for friendly robots all along the way.
		while ( testDistance < maxTestDistance)
		{
			testLocation = myLocation.add(dir, testDistance);
			try {
				if (rc.isLocationOccupiedByRobot(testLocation)) {
					RobotInfo bot = rc.senseRobotAtLocation(testLocation);
					return bot.team != rc.getTeam();
				}
			} catch (GameActionException e) {
				System.out.println("Exception in CombatUtil.safeToFire: "+e);
			}
			testDistance += DISTANCE_INCREMENT;
		}
		return true;
	}

	protected void buyPointsIfItWillMakeUsWin() {
		RobotController rc = getRc();
		try {
			if (rc.getTeamBullets() > (1000 - rc.getTeamVictoryPoints())* getPointCostThisRound() || rc.getRoundLimit() -rc.getRoundNum() < 5)
			{
				rc.donate(rc.getTeamBullets());
			}
			if (rc.getTeamBullets() > 1200)
				rc.donate(rc.getTeamBullets() -1000);
		} catch (GameActionException e) {
			System.out.println("Exception in buyPointsIfItWillMakeUsWin: "+e);
		}
	}

	protected float getPointCostThisRound() {
		RobotController rc = getRc();
		return (float) (7.5 + (rc.getRoundNum()*12.5 / rc.getRoundLimit()));
	}

}
