package demobot.robots;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import ddframework.robots.BaseRobot;
import ddframework.util.RandomUtil;

public class ArchonRobot extends BaseRobot {

    public ArchonRobot(RobotController controller) {
        super(controller);
    }

    @Override
    protected void onGameRound(RobotController rc) throws Exception {
        // Generate a random direction
        Direction dir = RandomUtil.randomDirection();

        // Randomly attempt to build a gardener in this direction
        if (rc.canHireGardener(dir) && Math.random() < .01) {
            rc.hireGardener(dir);
        }

        // Move randomly
        tryMove(RandomUtil.randomDirection());

        // Broadcast archon's location for other robots on the team to know
        MapLocation myLocation = rc.getLocation();
        rc.broadcast(0, (int) myLocation.x);
        rc.broadcast(1, (int) myLocation.y);
    }
}
