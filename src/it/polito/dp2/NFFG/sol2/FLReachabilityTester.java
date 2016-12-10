package it.polito.dp2.NFFG.sol2;

import it.polito.dp2.NFFG.*;
import it.polito.dp2.NFFG.lab2.NoGraphException;
import it.polito.dp2.NFFG.lab2.ReachabilityTester;
import it.polito.dp2.NFFG.lab2.ServiceException;
import it.polito.dp2.NFFG.lab2.UnknownNameException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.HashMap;

/**
 * ant -Dit.polito.dp2.NFFG.lab2.URL="http://2.238.140.10:9090/Neo4JXML/rest" -Dseed="100000" runFuncTest
 */

/**
 * Created by FLDeviOS on 01/12/2016.
 */
public class FLReachabilityTester implements ReachabilityTester {

    private final boolean VERBOSE = true;

    public final String NODES = "nodes";
    public final String NODE = "node";
    private final String PATHS = "paths";

    private WebTarget target;
    private String baseURL;
    private Client client;
    private HashMap<String, Node> myNeo4JNodes;
    private NffgVerifier networkServices;
    private String nffgName = null;

    /**
     * Class' constructor
     */
    public FLReachabilityTester() throws NffgVerifierException {

        // create the basic URL as a String
        baseURL = System.getProperty("it.polito.dp2.NFFG.lab2.URL") + "/resource";
        // create a new client
        client = ClientBuilder.newClient();
        // create a global target for all actions from the baseURL string
        target = client.target(getBaseURI(baseURL));

        NffgVerifierFactory factory = NffgVerifierFactory.newInstance();
        networkServices = factory.newNffgVerifier();

        // initialize node list
        myNeo4JNodes = new HashMap<>();
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
        deleteAllNodes();

        if (name == null) {
            throw new ServiceException();
        }

        // read required nffg
        NffgReader nffg = networkServices.getNffg(name);

        if (nffg == null) {
            throw new UnknownNameException();
        }

        nffgName = nffg.getName();

        if (nffgName == null) {
            throw new ServiceException();
        }

        // load nffg to server
        for (NodeReader node : nffg.getNodes()) {
            createNodeWithProperties(node.getName());
        }
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

        // check existence of the two nodes
        if (cantFindOneOfTheTwoNodes(srcName, destName)) {
            System.out.println("(e) - UnknownNameException");
            throw new UnknownNameException();
        }

        // if there isn't a graph loaded
        if (myNeo4JNodes.size() == 0) {
            System.out.println("(e) - NoGraphException");
            throw new NoGraphException();
        }

        return pathExists(srcName, destName);
    }

    /**
     * Gets the name of the currently loaded graph
     *
     * @return the name of the currently loaded graph or null if no graph is currently loaded (includes the case of failure of the last attempt to load a graph).
     * This is a local operation that cannot fail.
     */
    @Override
    public String getCurrentGraphName() {
        if (nffgName != null) {
            return nffgName;
        }
        return null;
    }

    private URI getBaseURI(String url) {
        return UriBuilder.fromUri(url).build();
    }

    private void deleteAllNodes() throws ServiceException {
        Response response = target.path(NODES)
                .request()
                .accept("application/xml")
                .delete();

        if (response.getStatus() == 200) {
            if (VERBOSE) {
                System.out.println(response.getStatus() + " - OK: All nodes DELETED!");
            }
        } else {
            System.out.println("(!-1) Error deleting nodes from Neo4j DB!");
            throw new ServiceException();
        }
    }

    private void createNodeWithProperties(String nodeName) throws ServiceException {
        // create a new node
        Node node = new Node();

        Property p = new Property();
        p.setName("name");
        p.setValue(nodeName);
        node.getProperty().add(p);

        Response response = target.path(NODE)
                .request()
                .accept("application/xml")
                .post(Entity.entity(node, "application/xml"));

        if (response.getStatus() == 200) {
            int status = response.getStatus();
            node.setId(response.readEntity(Node.class).getId());

            if (VERBOSE) {
                System.out.println(status + " - OK: Node " +
                        node.getId() +
                        " " +
                        p.getValue() +
                        " UPLOADED");
            }

            myNeo4JNodes.put(p.getValue(), node);
        } else {
            System.out.println("(!-2) Error uploading node into Neo4j DB!");
            throw new ServiceException();
        }
    }

    private boolean cantFindOneOfTheTwoNodes(String srcName, String destName) {
        if (myNeo4JNodes.get(srcName) == null || myNeo4JNodes.get(destName) == null) {
            return true;
        }
        return false;
    }

    private boolean pathExists(String srcName, String destName) throws ServiceException {
        Boolean res;

        Response response = target.path(NODE)
                .path(myNeo4JNodes.get(srcName).getId())
                .path(PATHS)
                .queryParam("dst", myNeo4JNodes.get(destName).getId())
                .request()
                .accept("application/xml")
                .get();

        if (response.getStatus() == 200) {
            Paths paths = response.readEntity(Paths.class);

            if (paths.getPath().size() != 0) {
                if (VERBOSE) {
                    System.out.println(response.getStatus() + " - OK: A path EXISTS!");
                }
                res = true;
            } else {
                if (VERBOSE) {
                    System.out.println(response.getStatus() + " - OK: A path NOT EXISTS!");
                }
                res = false;
            }

        } else {
            System.out.println("(!-3) Error retrieving paths from Neo4J DB");
            System.exit(3);
            throw new ServiceException();
        }

        return res;
    }
}
