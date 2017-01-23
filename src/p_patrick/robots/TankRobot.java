package p_patrick.robots;

import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import ddframework.robots.SmartBaseRobot;
import ddframework.util.RandomUtil;

public class TankRobot extends SmartBaseRobot {

    public TankRobot(RobotController controller) {
        super(controller);
    }

    @Override
    protected void onGameRound(RobotController rc) throws Exception {
        super.onGameRound(rc);

        // See if there are any nearby enemy robots
        RobotInfo[] robots = rc.senseNearbyRobots(-1, getEnemyTeam());

        // If there are some...
        if (robots.length > 0) {
            // And we have enough bullets, and haven't attacked yet this turn...
            if (rc.canFireSingleShot()) {
                // ...Then fire a bullet in the direction of the enemy.
                rc.fireSingleShot(rc.getLocation().directionTo(robots[0].location));
            }
        }

        // Move randomly
        tryMove(RandomUtil.randomDirection());
    }
}
