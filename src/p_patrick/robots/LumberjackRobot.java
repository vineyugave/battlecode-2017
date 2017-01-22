package p_patrick.robots;

import battlecode.common.*;
import ddframework.robots.BaseRobot;
import ddframework.util.Navigation;
import ddframework.util.RandomUtil;

import java.util.Arrays;

public class LumberjackRobot extends BaseRobot {

    static private Direction exploreDirection = new Direction((float)Math.random() * 2 * (float)Math.PI);

    public LumberjackRobot(RobotController controller) {
        super(controller);

    }

    @Override
    protected void onGameRound(RobotController rc) throws Exception {
        // See if there are any enemy robots within striking range (distance 1 from lumberjack's radius)
        RobotInfo[] robots = rc.senseNearbyRobots(GameConstants.LUMBERJACK_STRIKE_RADIUS, enemyTeam);

        if (robots.length > 0 && !rc.hasAttacked()) {
            RobotInfo closestRobot = Navigation.findClosestRobot(robots, rc);
            attackAndStrike(closestRobot, rc);
        } else {
            // No close robots, so search for robots within sight radius
            robots = visibleHostiles;

            // If there is a robot, move towards it
            if (robots.length > 0) {
                MapLocation myLocation = rc.getLocation();
                MapLocation enemyLocation = robots[0].getLocation();
                Direction toEnemy = myLocation.directionTo(enemyLocation);
                // Move and chop toward enemy
                tryMoveChopDir(toEnemy, rc);
            } else {
                // Move and Chop Randomly
                tryMoveChopDir(RandomUtil.randomDirection(), rc);
            }
        }
    }

    private void tryMoveChopDir(Direction dir, RobotController rc) throws GameActionException {

        // move toward and target trees.
        TreeInfo[] trees = rc.senseNearbyTrees(rc.getType().sensorRadius);
        if (trees.length > 0) {

            TreeInfo[] enemyNeutralTrees = Arrays.stream(trees).filter(x -> x.getTeam() != rc.getTeam()).toArray(TreeInfo[]::new);

            // Filter the trees down to only ones that are already damaged and don't belong to me.
            TreeInfo[] damagedTrees = Arrays.stream(enemyNeutralTrees).filter(x -> x.getHealth() < x.maxHealth).toArray(TreeInfo[]::new);

            if (damagedTrees.length > 0) {
                TreeInfo closestTree = Navigation.findClosestTree(damagedTrees, rc);
                if (closestTree != null) {
                    System.out.print("Found a damaged enemy/neutral tree");
                    dir = myLocation.directionTo(closestTree.location);
                    rc.setIndicatorLine(myLocation, closestTree.location, 255, 20, 0);
                }
            } else if (enemyNeutralTrees.length > 0) {
                TreeInfo closestTree = Navigation.findClosestTree(enemyNeutralTrees, rc);
                if (closestTree != null) {
                    System.out.print("Found an enemy/neutral tree");
                    dir = myLocation.directionTo(closestTree.location);
                    rc.setIndicatorLine(myLocation, closestTree.location, 155, 0, 0);
                }
            }
        }

        // get the location of where we're moving within strike radius
        MapLocation dirLoc = myLocation.add(dir,GameConstants.LUMBERJACK_STRIKE_RADIUS);

        // See if there's a tree within strike radius
        TreeInfo treeAhead = rc.senseTreeAtLocation(dirLoc);
        // First, try intended direction
        if (!rc.hasMoved() && rc.canMove(dir, 0.75f)) {
            System.out.print("DOING MOVE");
            rc.move(dir);
        }
        // If moving isn't possible, check for tree.
        else if (treeAhead != null && (treeAhead.team == Team.NEUTRAL || treeAhead.team == enemyTeam)) {
            // If we can shake the tree and it has bullets, lets get those out!
            if (rc.canShake(dirLoc) && treeAhead.containedBullets > 0) {
                System.out.print("SHAKIN' DAT TREE.  Previous Bullets: " + rc.getTeamBullets());
                rc.shake(dirLoc);
                System.out.print("SHOOK DAT TREE.  After Bullets: " + rc.getTeamBullets());
                rc.setIndicatorDot(dirLoc,0,155,155);
            }
            // else lets chop that sucka down.
            else if (rc.canChop(dirLoc)) {
                rc.chop(dirLoc);
                System.out.print("CHOPPED DAT TREE!");
                rc.setIndicatorDot(dirLoc,155,0,155);
            } else {
                // if we can't chop it, something went wrong... scramble
                explore();
            }
        } else {
            // Else trying moving randomly
            System.out.print("NO TREE! MOVING ON");
            explore();
        }
    }

    private void attackAndStrike(RobotInfo robot, RobotController rc) throws GameActionException {
        MapLocation myLocation = rc.getLocation();
        MapLocation enemyLocation = robot.getLocation();
        Direction toEnemy = myLocation.directionTo(enemyLocation);
        if (rc.canStrike()) {
            rc.strike();
        }
        Navigation.tryMove(toEnemy, rc);
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
