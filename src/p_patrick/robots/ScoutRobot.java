package p_patrick.robots;

import battlecode.common.*;
import ddframework.robots.BaseRobot;
import ddframework.util.RandomUtil;
import ddframework.util.VectorMath;

public class ScoutRobot extends BaseRobot {

    static public Direction exploreDirection = new Direction((float)Math.random() * 2 * (float)Math.PI);

    public ScoutRobot(RobotController controller) {
        super(controller);
    }

    @Override
    protected void onGameRound(RobotController rc) throws Exception {

        treeShakeHop();

        boolean foundHighPriorityTarget = false;

        if (visibleHostiles.length > 0) {
            // search for all gardeners first
            for (RobotInfo robot : visibleHostiles) {
                if (robot.type == RobotType.GARDENER) {
                    attackAndFollow(robot, rc);
                    foundHighPriorityTarget = true;
                }
            }
            if (!foundHighPriorityTarget){
                // search for archons if we didn't find a gardener
                for (RobotInfo robot : visibleHostiles) {
                    if (robot.type == RobotType.GARDENER) {
                        attackAndFollow(robot, rc);
                        foundHighPriorityTarget = true;
                    }
                }
            }
            if (!foundHighPriorityTarget) {
                attackAndFollow(visibleHostiles[0], rc);
            }

        } else {
            explore();
        }
    }

    private void treeShakeHop() throws GameActionException {
        RobotController rc = getRc();
        if (rc.hasMoved()) {
            return;
        }

        for (TreeInfo tree : visibleTrees) {
            if (tree.containedBullets > 0) {
                if (rc.canShake(tree.location) && tree.containedBullets > 0 && rc.canInteractWithTree(tree.getID())) {
                    System.out.print("SHAKIN' DAT TREE.  Previous Bullets: " + rc.getTeamBullets());
                    rc.shake(tree.location);
                    System.out.print("SHOOK DAT TREE.  After Bullets: " + rc.getTeamBullets());
                    rc.setIndicatorDot(tree.location,0,155,155);
                } else {
                    Direction dir = myLocation.directionTo(tree.location);
                    tryMove(dir);
                }
                return;
            }
        }
    }

    private void explore() throws GameActionException {
        RobotController rc = getRc();

        if (rc.hasMoved()) {
            return;
        }

        // TODO: Check for global target instead of going random

        System.out.println("Exploring in a random direction: " + exploreDirection);
        if (!tryMove(exploreDirection)) {
            exploreDirection = exploreDirection.rotateLeftDegrees(90);
            tryMove(exploreDirection);
        }

    }

    private void attackAndFollow(RobotInfo robot, RobotController rc) throws GameActionException {
        MapLocation myLocation = rc.getLocation();
        MapLocation enemyLocation = robot.getLocation();
        Direction toEnemy = myLocation.directionTo(enemyLocation);

        tryMove(toEnemy);

        if (!rc.hasAttacked()) {
            if (rc.canFireSingleShot()) {
                rc.fireSingleShot(toEnemy);
            }
        }

    }



}
