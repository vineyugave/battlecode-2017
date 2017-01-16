package baseline.robots;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;
import com.dd.framework.BaseRobot;
import com.dd.framework.Utils;

public class SoldierRobot extends BaseRobot {

    private Team mEnemy;

    public SoldierRobot(RobotController controller) {
        super(controller);

        mEnemy = controller.getTeam().opponent();
    }

    @Override
    protected void onGameRound(RobotController rc) throws Exception {
        MapLocation myLocation = rc.getLocation();

        // See if there are any nearby enemy robots
        RobotInfo[] robots = rc.senseNearbyRobots(-1, mEnemy);

        // If there are some...
        if (robots.length > 0) {
            // And we have enough bullets, and haven't attacked yet this turn...
            if (rc.canFireSingleShot()) {
                // ...Then fire a bullet in the direction of the enemy.
                rc.fireSingleShot(rc.getLocation().directionTo(robots[0].location));
            }
        }

        // Move randomly
        tryMove(Utils.randomDirection());
    }
}
