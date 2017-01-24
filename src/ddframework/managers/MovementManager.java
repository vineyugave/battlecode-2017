package ddframework.managers;

import battlecode.common.RobotController;
import ddframework.algorithms.AStar;
import ddframework.algorithms.BaseAlgorithm;

/**
 * Created by Viney Ugave (viney@vinzzz.com) on 1/23/17
 */

public class MovementManager {

    RobotController rc;

    public enum Algorithms {
        AStar, Dijkstra
    }

    public MovementManager(RobotController rc) {
        this.rc = rc;
    }

    public BaseAlgorithm getAlgorithmStrategy(Algorithms algorithm){
        BaseAlgorithm baseAlgorithm = null;
        switch (algorithm){
            case AStar:
                baseAlgorithm = new AStar();
                break;

            case Dijkstra:
                //TODO
        }
        return baseAlgorithm;
    }
}
