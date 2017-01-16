package baseline;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import com.dd.framework.BaseRobot;
import baseline.robots.*;

@SuppressWarnings("unused")
public strictfp class RobotPlayer {

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        BaseRobot robot = createRobot(rc);
        if (robot != null) {
            robot.loop();
        } else {
            System.err.println("Unhandled Robot! Type: " + rc.getType());
        }
    }

    private static BaseRobot createRobot(RobotController rc) {
        switch (rc.getType()) {
            case ARCHON:
                return new ArchonRobot(rc);
            case GARDENER:
                return new FarmerGardenerRobot(rc);
            case LUMBERJACK:
                return new LumberjackRobot(rc);
            case SOLDIER:
                return new SoldierRobot(rc);
            case TANK:
                return new TankRobot(rc);
            case SCOUT:
                return new ScoutRobot(rc);
            default:
                return null;
        }
    }

}
