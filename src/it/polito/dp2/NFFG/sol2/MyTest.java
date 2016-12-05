package it.polito.dp2.NFFG.sol2;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * Created by FLDeviOS on 05/12/2016.
 */
public class MyTest {
    public static void main(String[] args) {

        WebTarget myTarget;
        Client myClient = null;
        String myBaseURL = "http://2.238.140.10:9090/Neo4JXML/rest/resource";
        Node node = new Node();
        Node response;

        try {
            // create a webtarget from the baseURL string
            myTarget = myClient.target(getBaseURI(myBaseURL));

            // create a new client
            myClient = ClientBuilder.newClient();

            // set the new node
            node.setId("0");

            // Send the request (create node) and receive a response automatically
            // translated as a node instance class. Setting a request as "application/xml".
            response = myTarget.path("/node")
                    .request("application/xml")
                    .post(Entity.entity(node, "application/xml"), Node.class);

            System.out.println("--- Response of Post received --- \n");
            System.out.println(" node " + response.toString());
        } catch (ProcessingException pe) {
            System.out.println("Error during JAX-RS request processing");
            pe.printStackTrace();
        } catch (WebApplicationException wae) {
            System.out.println("Server returned error");
            wae.printStackTrace();
        } catch (Exception e) {
            System.out.println("Unexpected exception");
            e.printStackTrace();
        }
    }

    private static URI getBaseURI(String url) {
        return UriBuilder.fromUri(url).build();
    }
}
