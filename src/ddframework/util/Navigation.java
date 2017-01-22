package ddframework.util;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.TreeInfo;

public class Navigation {

    public static RobotInfo findClosestRobot(RobotInfo[] robots, RobotController rc) throws GameActionException {
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

    public static  TreeInfo findClosestTree(TreeInfo[] trees, RobotController rc) throws GameActionException {
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
}
