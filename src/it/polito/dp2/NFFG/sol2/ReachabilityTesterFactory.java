package it.polito.dp2.NFFG.sol2;


import it.polito.dp2.NFFG.NffgVerifierException;
import it.polito.dp2.NFFG.lab2.ReachabilityTester;
import it.polito.dp2.NFFG.lab2.ReachabilityTesterException;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

/**
 * Created by FLDeviOS on 05/12/2016.
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
     * @throws ReachabilityTesterException if an implementation of {@link ReachabilityTester} cannot be created.
     */
    @Override
    public ReachabilityTester newReachabilityTester() throws ReachabilityTesterException {

        FLReachabilityTester myFLReachabilityTester = null;

        try {
            myFLReachabilityTester = new FLReachabilityTester();
        } catch (MalformedURLException e) {
            System.err.println("MalformedURLException Error: " + e.getMessage());
            e.printStackTrace();
        } catch (URISyntaxException e) {
            System.err.println("URISyntaxException Error: " + e.getMessage());
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.err.println("NullPointerException Error: " + e.getMessage());
            e.printStackTrace();
        }

        return myFLReachabilityTester;
    }

}
