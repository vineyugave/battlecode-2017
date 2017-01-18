package player.experiment.robots;

import battlecode.common.*;
import ddframework.robots.BaseRobot;
import ddframework.util.RandomUtil;

public class LumberjackRobot extends BaseRobot {

    private Team mEnemy;

    public LumberjackRobot(RobotController controller) {
        super(controller);

        mEnemy = controller.getTeam().opponent();
    }

    @Override
    protected void onGameRound(RobotController rc) throws Exception {
        // See if there are any enemy robots within striking range (distance 1 from lumberjack's radius)
        RobotInfo[] robots = rc.senseNearbyRobots(RobotType.LUMBERJACK.bodyRadius + GameConstants.LUMBERJACK_STRIKE_RADIUS, mEnemy);

        if (robots.length > 0 && !rc.hasAttacked()) {
            // Use strike() to hit all nearby robots!
            rc.strike();
        } else {
            // No close robots, so search for robots within sight radius
            robots = rc.senseNearbyRobots(-1, mEnemy);

            // If there is a robot, move towards it
            if (robots.length > 0) {
                MapLocation myLocation = rc.getLocation();
                MapLocation enemyLocation = robots[0].getLocation();
                Direction toEnemy = myLocation.directionTo(enemyLocation);

                tryMove(toEnemy);
            } else {
                // Move Randomly
                tryMove(RandomUtil.randomDirection());
            }
        }
    }
}
