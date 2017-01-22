package ddframework.util;

import battlecode.common.*;

public class Navigation {

    /**
     * Attempts to move in a given direction, while avoiding small obstacles directly in the path.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     */
    public static boolean tryMove(Direction dir, RobotController rc) throws GameActionException {
        return tryMove(dir, 20, 3, rc);
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
    public static boolean tryMove(Direction dir, float degreeOffset, int checksPerSide, RobotController rc) throws GameActionException {
        // If we can't move, then return.
        if (rc.hasMoved()) {
            return false;
        }
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

        // A move never happened, so return false.
        return false;
    }

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

    public static TreeInfo findClosestTree(TreeInfo[] trees, RobotController rc) throws GameActionException {
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

    public static void follow(RobotInfo robot, RobotController rc) throws GameActionException {
        MapLocation robotLocation = robot.getLocation();
        MapLocation myLocation = rc.getLocation();
        Direction toEnemy = myLocation.directionTo(robotLocation);

        // Move toward robot if I can move and the target is not within stride distance
        if (!rc.hasMoved() && robotLocation.isWithinDistance(myLocation,rc.getType().strideRadius)) {
            Navigation.tryMove(toEnemy, rc);
        }
    }

    public static void patrol(RobotInfo robot, RobotController rc) throws GameActionException {
        MapLocation robotLocation = robot.getLocation();
        MapLocation myLocation = rc.getLocation();
        Direction toRobot = myLocation.directionTo(robotLocation);
        Direction awayFromRobot = robotLocation.directionTo(myLocation);
        // Move away from robot if it is within my stride radius
        if (!rc.hasMoved() && robotLocation.isWithinDistance(myLocation,rc.getType().strideRadius)) {
            Navigation.tryMove(awayFromRobot, rc);
            rc.setIndicatorLine(myLocation, robotLocation.add(awayFromRobot), 20, 20, 255);
        }
        // Move toward robot if the robot is not within sensor distance minus stride radius
        if (!rc.hasMoved() && !robotLocation.isWithinDistance(myLocation,rc.getType().sensorRadius - rc.getType().strideRadius)) {
            Navigation.tryMove(toRobot, rc);
            rc.setIndicatorLine(myLocation, robotLocation.add(toRobot), 255, 20, 20);
        }
    }
}
