package p_patrick.robots;

import battlecode.common.*;
import ddframework.robots.BaseRobot;
import ddframework.util.CombatUtil;
import ddframework.util.Navigation;

public class ScoutRobot extends BaseRobot {

    static private Direction exploreDirection = new Direction((float)Math.random() * 2 * (float)Math.PI);

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
                    CombatUtil.attackAndFollow(robot, rc);
                    foundHighPriorityTarget = true;
                    break;
                }
            }
            if (!foundHighPriorityTarget){
                // search for archons if we didn't find a gardener
                for (RobotInfo robot : visibleHostiles) {
                    if (robot.type == RobotType.ARCHON) {
                        CombatUtil.attackAndFollow(robot, rc);
                        foundHighPriorityTarget = true;
                        break;
                    }
                }
            }
            if (!foundHighPriorityTarget) {
                CombatUtil.attackAndFollow(visibleHostiles[0], rc);
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

        for (TreeInfo tree : visibleTrees) {
            if (tree.containedBullets > 0) {
                if (rc.canShake(tree.location) && tree.containedBullets > 0 && rc.canInteractWithTree(tree.getID())) {
                    System.out.print("SHAKIN' DAT TREE.  Pre-Shake Bullet Count: " + rc.getTeamBullets());
                    rc.shake(tree.location);
                    System.out.print("SHOOK DAT TREE.  Post-Shake Bullet Count: " + rc.getTeamBullets());
                    rc.setIndicatorDot(tree.location,0,155,155);
                } else {
                    Direction dir = myLocation.directionTo(tree.location);
                    Navigation.tryMove(dir, rc);
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
        if (!Navigation.tryMove(exploreDirection, rc)) {
            exploreDirection = exploreDirection.rotateLeftDegrees(90);
            Navigation.tryMove(exploreDirection, rc);
        }

    }



}
