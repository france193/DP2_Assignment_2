package it.polito.dp2.NFFG.sol2;

import it.polito.dp2.NFFG.*;
import it.polito.dp2.NFFG.lab2.NoGraphException;
import it.polito.dp2.NFFG.lab2.ReachabilityTester;
import it.polito.dp2.NFFG.lab2.ServiceException;
import it.polito.dp2.NFFG.lab2.UnknownNameException;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * ant -Dit.polito.dp2.NFFG.lab2.URL="http://2.238.140.10:9090/Neo4JXML/rest" runFuncTest
 */

/**
 * Created by FLDeviOS on 01/12/2016.
 */
public class FLReachabilityTester implements ReachabilityTester {

    public final String NODES = "nodes";
    public final String NODE = "node";

    private WebTarget target;
    private String baseURL;
    private Client client;
    private HashMap<String, Node> myNodes;
    private NffgVerifier networkServices;

    /**
     * Class' constructor
     */
    public FLReachabilityTester() throws MalformedURLException, URISyntaxException, NffgVerifierException {
        // create the basic URL as a String
        baseURL = System.getProperty("it.polito.dp2.NFFG.lab2.URL") + "/resource";
        // create a new client
        client = ClientBuilder.newClient();
        // create a global target for all actions from the baseURL string
        target = client.target(getBaseURI(baseURL));
        // initialize node list
        myNodes = new HashMap<>();

        NffgVerifierFactory factory = NffgVerifierFactory.newInstance();
        networkServices = factory.newNffgVerifier();
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

        // clean Neo4J DB
        cleanDB();

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

    private URI getBaseURI(String url) {
        return UriBuilder.fromUri(url).build();
    }

    private void cleanDB() {
        if (getAllNodes() > 0) {
            deleteAllNodes();
        }
    }

    private int getAllNodes() {
        Response response = target.path(NODES)
                .request()
                .accept("application/xml")
                .get();

        if (response.getStatus() == 200) {
            List<Nodes.Node> availablesNodes = ((Nodes) response.getEntity()).getNode();
            String name = null;
            int size = availablesNodes.size();

            if (size == 0) {
                System.out.println(response.getStatus() + " - OK: retrieved " + size + " nodes from Neo4J DB");
                return 0;
            } else {
                for (Nodes.Node node : availablesNodes) {
                    Node n = new Node();
                    n.setId(node.getId());
                    for (Property property : node.getProperty()) {
                        name = property.getValue();
                        Property p = new Property();
                        p.setName(property.getName());
                        p.setValue(name);
                        n.getProperty().add(p);
                    }
                    myNodes.put(name, n);
                }
                System.out.println(response.getStatus() + " - OK: retrieved " + size + " nodes from Neo4J DB");
                return size;
            }

        } else {
            System.out.println("(!-0) Error getting nodes from Neo4j DB!");
            return 0;
        }
    }

    private void deleteAllNodes() {
        Response response = target.path(NODES)
                .request()
                .accept("application/xml")
                .delete();

        if (response.getStatus() == 200) {
            System.out.println(response.getStatus() + " - OK: All nodes deleted!");
        } else {
            System.out.println("(!-1) Error deleting nodes from Neo4j DB!");
        }
    }

    private void createNodeWithProperties(String nodeName, String nodeType, String nffgName) {
        // create a new node
        Node node = new Node();

        Property p = new Property();
        p.setName("name");
        p.setValue(nodeName);
        node.getProperty().add(p);

        Property p1 = new Property();
        p1.setName("nodeType");
        p1.setValue(nodeType);
        node.getProperty().add(p1);

        Property p2 = new Property();
        p2.setName("nffgName");
        p2.setValue(nffgName);
        node.getProperty().add(p2);

        Response response = target.path(NODE)
                .request()
                .accept("application/xml")
                .post(Entity.entity(node, "application/xml"));

        System.out.println("--> Inserting node: " + response.getStatus());

        if (response.getStatus() == 200) {
            System.out.println(response.getStatus() + " - OK: Node correctly inserted into Neo4J DB");
        } else {
            System.out.println("(!-2) Error inserting node into Neo4j DB!");
        }
    }
}
