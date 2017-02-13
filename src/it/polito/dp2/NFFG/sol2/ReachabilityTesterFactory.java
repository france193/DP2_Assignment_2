package it.polito.dp2.NFFG.sol2;

import it.polito.dp2.NFFG.lab2.ReachabilityTester;
import it.polito.dp2.NFFG.lab2.ReachabilityTesterException;

/**
 * Created by Francesco Longo(223428) 11/02/2017
 */
public class ReachabilityTesterFactory extends it.polito.dp2.NFFG.lab2.ReachabilityTesterFactory {

    /**
     * Void constructor
     */
    public ReachabilityTesterFactory() {
    }

    /**
     * Creates a new instance of a {@link ReachabilityTester} implementation.
     *
     * @return a new instance of a {@link ReachabilityTester} implementation.
     * @throws ReachabilityTesterException
     *             if an implementation of {@link ReachabilityTester} cannot be
     *             created.
     */
    @Override
    public ReachabilityTester newReachabilityTester() throws ReachabilityTesterException {
        FLReachabilityTester myFLReachabilityTester = new FLReachabilityTester();
        return myFLReachabilityTester;
    }
}