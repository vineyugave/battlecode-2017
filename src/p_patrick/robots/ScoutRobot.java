package p_patrick.robots;

import battlecode.common.*;
import ddframework.robots.SmartBaseRobot;
import ddframework.util.RandomUtil;

public class ScoutRobot extends SmartBaseRobot {

    static private Direction exploreDirection = RandomUtil.randomDirection();

    public ScoutRobot(RobotController controller) {
        super(controller);
    }

    @Override
    protected void onGameRound(RobotController rc) throws Exception {
        super.onGameRound(rc);

        treeShakeHop();

        boolean foundHighPriorityTarget = false;

        RobotInfo[] hostiles = getCachedVisibleHostiles();
        if (hostiles.length > 0) {
            // TODO: avoid lumberjacks like the plague.

            // search for all gardeners first
            for (RobotInfo robot : hostiles) {
                if (robot.type == RobotType.GARDENER) {
                    attackAndFollow(robot);
                    foundHighPriorityTarget = true;
                    break;
                }
            }
            if (!foundHighPriorityTarget){
                // search for archons if we didn't find a gardener
                for (RobotInfo robot : hostiles) {
                    if (robot.type == RobotType.ARCHON) {
                        attackAndFollow(robot);
                        foundHighPriorityTarget = true;
                        break;
                    }
                }
            }
            if (!foundHighPriorityTarget) {
                attackAndFollow(hostiles[0]);
            }

        } else {
            // TODO: Check for global target using buffer instead of going random

            explore();
        }
    }

    private void treeShakeHop() throws GameActionException {
        RobotController rc = getRc();
        // If we've already moved this turn, don't continue with tree shaking to save byte count
        if (rc.hasMoved()) {
            return;
        }

        for (TreeInfo tree : getCachedVisibleTrees()) {
            if (tree.containedBullets > 0) {
                if (rc.canShake(tree.location) && tree.containedBullets > 0 && rc.canInteractWithTree(tree.getID())) {
                    System.out.print("SHAKIN' DAT TREE.  Pre-Shake Bullet Count: " + rc.getTeamBullets());
                    rc.shake(tree.location);
                    System.out.print("SHOOK DAT TREE.  Post-Shake Bullet Count: " + rc.getTeamBullets());
                    rc.setIndicatorDot(tree.location,0,155,155);
                } else {
                    Direction dir = getCachedLocation().directionTo(tree.location);
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

        System.out.println("Exploring in a random direction: " + exploreDirection);
        if (!tryMove(exploreDirection)) {
            exploreDirection = exploreDirection.rotateLeftDegrees(90);
            tryMove(exploreDirection);
        }

    }



}
