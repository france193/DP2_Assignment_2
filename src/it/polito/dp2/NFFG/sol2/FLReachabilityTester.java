package it.polito.dp2.NFFG.sol2;

import it.polito.dp2.NFFG.lab2.NoGraphException;
import it.polito.dp2.NFFG.lab2.ReachabilityTester;
import it.polito.dp2.NFFG.lab2.ServiceException;
import it.polito.dp2.NFFG.lab2.UnknownNameException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 * ant -Dit.polito.dp2.NFFG.lab2.URL="http://2.238.140.10:9090/Neo4JXML/rest" runFuncTest
 */

/**
 * Created by FLDeviOS on 01/12/2016.
 */
public class FLReachabilityTester implements ReachabilityTester {

    private WebTarget target;
    private String baseURL;
    private Client client;

    /**
     * Class' constructor
     */
    public FLReachabilityTester() throws MalformedURLException, URISyntaxException {
        baseURL = System.getProperty("it.polito.dp2.NFFG.lab2.URL") + "/resource";
    }

    /**
     * Loads the NFFG with the given name into the remote service.
     * Any previously loaded graph has to be overwritten by this one.
     *
     * @param name the name of the NFFG to be loaded
     * @throws UnknownNameException if the name passed as argument does not correspond to a known NFFG. No alteration of data on the server occurs in this case.
     * @throws ServiceException     if any other error occurs when trying to upload the NFFG. The load operation may have been executed partially in this case.
     */
    @Override
    public void loadNFFG(String name) throws UnknownNameException, ServiceException {

        // read baseURL and print for debug
        System.out.println("Now i received: " + baseURL);

        // create a new client
        client = ClientBuilder.newClient();

        // create a webtarget from the baseURL string
        target = client.target(getBaseURI(baseURL));

        Relationship r = new Relationship();
        r.setType("Link");
        r.setDstNode("node2");

        Response response = target.path("node")
                .path("70")
                .path("relationship")
                .request()
                .accept("application/xml")
                .post(Entity.entity(r, "application/xml"));

        System.out.println("--> Creating a relationship: " + response.getStatus());

        if (response.getStatus() == 200) {
            Relationship rel = (Relationship) response.getEntity();
            System.out.println("Id: " + rel.getId());
            System.out.println("Type: " + rel.getType());
            System.out.println("Src: " + rel.getSrcNode());
            System.out.println("Dst: " + rel.getDstNode());
        }

        //TODO check if there are some data on the server
        //TODO delete all data on the server
        //TODO read 1 NFFG from random generator
        //TODO load NFFG data to server
    }

    /**
     * Tests reachability from a source node to a destination node in the previously uploaded
     * graph by means of the remote service.
     *
     * @param srcName  the name of the source node
     * @param destName the name of the destination node
     * @return true if the destination node is reachable from the source node, false otherwise
     * @throws UnknownNameException if at least one of the names passed as arguments does not correspond to a node existing in the loaded graph
     * @throws NoGraphException     if no graph is currently loaded
     * @throws ServiceException     if any other error occurs when trying to test reachability
     */
    @Override
    public boolean testReachability(String srcName, String destName) throws UnknownNameException, ServiceException, NoGraphException {
        return false;
    }

    /**
     * Gets the name of the currently loaded graph
     *
     * @return the name of the currently loaded graph or null if no graph is currently loaded (includes the case of failure of the last attempt to load a graph).
     * This is a local operation that cannot fail.
     */
    @Override
    public String getCurrentGraphName() {
        return null;
    }

    private static URI getBaseURI(String url) {
        return UriBuilder.fromUri(url).build();
    }
}
