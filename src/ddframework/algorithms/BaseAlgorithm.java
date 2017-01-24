package ddframework.algorithms;

/**
 * Created by Viney Ugave (viney@vinzzz.com) on 1/23/17
 */

public class BaseAlgorithm {

    public int roundLocation(float loc){
        return (int) (Math.round(loc*100.0)/100.0);
    }

}
