package it.polito.dp2.NFFG.sol2;

import it.polito.dp2.NFFG.*;
import it.polito.dp2.NFFG.lab2.*;
import it.polito.dp2.NFFG.sol2.generated_from_wadl.Node;
import it.polito.dp2.NFFG.sol2.generated_from_wadl.Paths;
import it.polito.dp2.NFFG.sol2.generated_from_wadl.Property;
import it.polito.dp2.NFFG.sol2.generated_from_wadl.Relationship;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.HashMap;

/**
 * Created by Francesco Longo(223428) 11/02/2017
 */
public class FLReachabilityTester implements ReachabilityTester {

    private WebTarget target;
    private String baseURL;
    private Client client;
    private HashMap<String, Node> myNeo4JNodes;
    private NffgVerifier nffgs;

    private String nffgName = null;

    /**
     * Class' constructor
     *
     * @throws ReachabilityTesterException
     */
    public FLReachabilityTester() throws ReachabilityTesterException {
        // create the basic URL as a String
        baseURL = System.getProperty("it.polito.dp2.NFFG.lab2.URL");

        if (baseURL == null) {
            throw new ReachabilityTesterException("Property not setted!");
        }

        baseURL = baseURL + "/resource";

        // create a new client
        client = ClientBuilder.newClient();
        // create a global target for all actions from the baseURL string
        target = client.target(getBaseURI(baseURL));

        try {
            NffgVerifierFactory factory = NffgVerifierFactory.newInstance();
            nffgs = factory.newNffgVerifier();
        } catch (NffgVerifierException e) {
            throw new ReachabilityTesterException("NffgVerifierException: Cannot instantiate NffgVerifierFactory");
        }

        // initialize node list
        myNeo4JNodes = new HashMap<>();
    }

    /**
     * Loads the NFFG with the given name into the remote service. Any
     * previously loaded graph has to be overwritten by this one.
     *
     * @param name the name of the NFFG to be loaded
     * @throws UnknownNameException if the name passed as argument does not correspond to a known
     *                              NFFG. No alteration of data on the server occurs in this
     *                              case.
     * @throws ServiceException     if any other error occurs when trying to upload the NFFG. The
     *                              load operation may have been executed partially in this case.
     */
    @Override
    public void loadNFFG(String name) throws UnknownNameException, ServiceException {
        deleteAllNodes();

        // retrieve nffg for the random generator
        NffgReader nffgReader = nffgs.getNffg(name);

        // if nffg doesn't exist
        if (nffgReader == null) {
            throw new UnknownNameException();
        }

        // upload all node
        for (NodeReader node : nffgReader.getNodes()) {
            createNodeWithProperties(node.getName());
        }

        for (NodeReader node : nffgReader.getNodes()) {
            for (LinkReader link : node.getLinks()) {
                createRelationship(node.getName(), link.getDestinationNode().getName());
            }
        }

        // correct load
        nffgName = name;
    }

    /**
     * Tests reachability from a source node to a destination node in the
     * previously uploaded graph by means of the remote service.
     *
     * @param srcName  the name of the source node
     * @param destName the name of the destination node
     * @return true if the destination node is reachable from the source node,
     * false otherwise
     * @throws UnknownNameException if at least one of the names passed as arguments does not
     *                              correspond to a node existing in the loaded graph
     * @throws NoGraphException     if no graph is currently loaded
     * @throws ServiceException     if any other error occurs when trying to test reachability
     */
    @Override
    public boolean testReachability(String srcName, String destName)
            throws UnknownNameException, ServiceException, NoGraphException {
        // check existence of the two nodes
        if (myNeo4JNodes.get(srcName) == null) {
            throw new UnknownNameException("(e) - UnknownNameException: srcName");
        }
        if (myNeo4JNodes.get(destName) == null) {
            throw new UnknownNameException("(e) - UnknownNameException: destName");
        }

        // if there isn't a graph loaded
        if (nffgName == null) {
            throw new NoGraphException("(e) - NoGraphException: nffgName");
        }

        return pathExists(srcName, destName);
    }

    /**
     * Gets the name of the currently loaded graph
     *
     * @return the name of the currently loaded graph or null if no graph is
     * currently loaded (includes the case of failure of the last
     * attempt to load a graph). This is a local operation that cannot
     * fail.
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
        myNeo4JNodes = new HashMap<>();
        myNeo4JNodes.clear();

        // delete all nodes on Neo4J
        Response response = target.path("nodes").request().accept("application/xml").delete();

        if (response.getStatus() != 200) {
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

        Response response = target.path("node").request().accept("application/xml")
                .post(Entity.entity(node, "application/xml"));

        if (response.getStatus() != 200) {
            throw new ServiceException("(!-2) Error uploading node into Neo4j DB!");
        }

        node.setId(response.readEntity(Node.class).getId());
        myNeo4JNodes.put(p.getValue(), node);
    }

    private boolean pathExists(String srcName, String destName) throws ServiceException {
        Response response = target.path("node").path(myNeo4JNodes.get(srcName).getId()).path("paths")
                .queryParam("dst", myNeo4JNodes.get(destName).getId()).request().accept("application/xml").get();

        if (response.getStatus() != 200) {
            throw new ServiceException("(!-3) Error retrieving paths from Neo4J DB");
        }

        Paths paths = response.readEntity(Paths.class);

        if (paths.getPath().size() != 0) {
            return true;
        } else {
            return false;
        }
    }

    private void createRelationship(String srcNode, String destNode) throws ServiceException {

        Relationship relationship = new Relationship();
        relationship.setType("Link");
        relationship.setDstNode(myNeo4JNodes.get(destNode).getId());

        Response response = target.path("node").path(myNeo4JNodes.get(srcNode).getId()).path("relationship").request()
                .accept("application/xml").post(Entity.entity(relationship, "application/xml"));

        if (response.getStatus() != 200) {
            throw new ServiceException("(!-3) Error creating relationship");
        }
    }
}
