package testing1.robots;

import battlecode.common.Direction;
import battlecode.common.RobotController;
import com.dd.framework.BaseRobot;
import com.dd.framework.Utils;

public class ArchonRobot extends BaseRobot {

	private static final float BULLET_THRESHOLD_GARDENER_BUILD = 125f;
	private static final float ROUND_THRESHOLD_GARDENER_BUILD = 0.4f;
	private static final float VP_BUY_THRESHOLD = 500f;

	private final float mTotalRounds;

	public ArchonRobot(RobotController controller) {
		super(controller);

		mTotalRounds = controller.getRoundLimit();
	}

	@Override
	protected void onGameRound(RobotController rc) throws Exception {
		final float bulletCount = rc.getTeamBullets();
		final int round = rc.getRoundNum();
		final float roundPercent = (float) round / mTotalRounds;

		if (shouldBuildFarmer(bulletCount, roundPercent)) {
			for (int i = 0; i < 8; i++ ) {
				Direction dir = Utils.randomDirection();
				if (rc.canHireGardener(dir)) {
					rc.hireGardener(dir);
					break;
				}
			}
		}

		// buy victory points
		if (bulletCount >= VP_BUY_THRESHOLD) {
			int donationCount = (int) (bulletCount / 10);
			rc.donate((donationCount / 2) * 10);
		}

		// Move randomly
		tryMove(Utils.randomDirection());
	}

	private boolean shouldBuildFarmer(float bulletCount, float roundPercent) {
		return bulletCount >= BULLET_THRESHOLD_GARDENER_BUILD
				&& roundPercent < ROUND_THRESHOLD_GARDENER_BUILD;
	}
}
