package testing.robots;

import battlecode.common.Direction;
import battlecode.common.RobotController;
import ddframework.robots.BaseRobot;
import ddframework.util.RandomUtil;
import ddframework.broadcast.SharedBuffer;

public class ArchonRobot extends BaseRobot {

	private static final int FAST_PRODUCE_FARMER_LIMIT = 18;
	private static final float BULLET_THRESHOLD_GARDENER_FAST_PRODUCE = 125f;
	private static final float BULLET_THRESHOLD_GARDENER_SLOW_PRODUCE = 600f;
	private static final float VP_BUY_THRESHOLD = 400f;
	private static final int MOVEMENT_BLOCKED_TRIES = 36;
	private static final float OBJECT_AVOID_DEGREES = 10f;

	private Direction mDirection;
	private int mMovementBlockedCount;
	private boolean mAvoidObjectRight;

	public ArchonRobot(RobotController controller) {
		super(controller);

		mDirection = new Direction(0);
		mMovementBlockedCount = MOVEMENT_BLOCKED_TRIES;
		mAvoidObjectRight = true;
	}

	@Override
	protected void onGameRound(RobotController rc) throws Exception {
		final float bulletCount = rc.getTeamBullets();

		// try to build a farmer if necessary
		if (shouldBuildFarmer(bulletCount)) {
			for (int i = 0; i < 8; i++ ) {
				Direction dir = RandomUtil.randomDirection();
				if (rc.canHireGardener(dir)) {
					rc.hireGardener(dir);
					incrementFarmerCount();
					break;
				}
			}
		}

		// buy victory points
		if (bulletCount >= VP_BUY_THRESHOLD) {
			int donationCount = (int) (bulletCount / 10);
			rc.donate((donationCount / 2) * 10);
		}

		// move
		if (rc.canMove(mDirection)) {
			// move in our movement direction
			rc.move(mDirection);
		} else {
			// the movement direction is blocked

			// adjust the angle slightly and try again
			mMovementBlockedCount++;
			if (mMovementBlockedCount >= MOVEMENT_BLOCKED_TRIES) {
				// if we have been blocked too many times in one direction, switch directions
				mMovementBlockedCount = 0;
				mAvoidObjectRight = !mAvoidObjectRight;
			}

			if (mAvoidObjectRight) {
				mDirection = mDirection.rotateRightDegrees(OBJECT_AVOID_DEGREES);
			} else {
				mDirection = mDirection.rotateLeftDegrees(OBJECT_AVOID_DEGREES);
			}
			tryMove(mDirection);
		}
	}

	private boolean shouldBuildFarmer(float bulletCount) throws Exception {
		// in fast-produce mode, favor building more gardeners sooner
		if (getFarmerCount() < FAST_PRODUCE_FARMER_LIMIT) {
			return bulletCount >= BULLET_THRESHOLD_GARDENER_FAST_PRODUCE;
		}

		// in slow-produce mode, favor buying VPs over building new gardeners
		return bulletCount >= BULLET_THRESHOLD_GARDENER_SLOW_PRODUCE;
	}

	private void incrementFarmerCount() throws Exception {
		final int currentCount = getFarmerCount();
		getSharedBuffer().write(SharedBuffer.KnownIndexes.FARMER_COUNT, currentCount + 1);
	}

	private int getFarmerCount() throws Exception {
		return getSharedBuffer().read(SharedBuffer.KnownIndexes.FARMER_COUNT);
	}
}
