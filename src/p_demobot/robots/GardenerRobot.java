package p_demobot.robots;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import ddframework.robots.BaseRobot;
import ddframework.util.Navigation;
import ddframework.util.RandomUtil;

public class GardenerRobot extends BaseRobot {

    public GardenerRobot(RobotController controller) {
        super(controller);
    }

    @Override
    protected void onGameRound(RobotController rc) throws Exception {
        // Listen for home archon's location
        int xPos = rc.readBroadcast(0);
        int yPos = rc.readBroadcast(1);
        MapLocation archonLoc = new MapLocation(xPos, yPos);

        // Generate a random direction
        Direction dir = RandomUtil.randomDirection();

        // Randomly attempt to build a soldier or lumberjack in this direction
        if (rc.canBuildRobot(RobotType.SOLDIER, dir) && Math.random() < .01) {
            rc.buildRobot(RobotType.SOLDIER, dir);
        } else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) && Math.random() < .01 && rc.isBuildReady()) {
            rc.buildRobot(RobotType.LUMBERJACK, dir);
        }

        // Move randomly
        Navigation.tryMove(RandomUtil.randomDirection(), rc);
    }
}
