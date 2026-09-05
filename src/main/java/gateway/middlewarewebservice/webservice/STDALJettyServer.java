package gateway.middlewarewebservice.webservice;

//import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;


public class STDALJettyServer {
	
	public static void main(String[] args) throws Exception {
    
	ServletHolder sh = new ServletHolder(ServletContainer.class);
    sh.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
    sh.setInitParameter("com.sun.jersey.config.property.packages", "pk.vaulsys.apigateway.webservice.middlewarewebservice.restful.resource");
    sh.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");
    
    Server server = new Server(1002);
    ServletContextHandler context = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
    context.addServlet(sh, "/*");
    server.start();
    server.join();
    
	}


    public static void start()
    {
        try {
            ServletHolder sh = new ServletHolder(ServletContainer.class);
            sh.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
            sh.setInitParameter("com.sun.jersey.config.property.packages", "pk.vaulsys.apigateway.webservice.middlewarewebservice.restful.resource");
            sh.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");

            Server server = new Server(1002);
            ServletContextHandler context = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
            context.addServlet(sh, "/*");
            server.start();
            server.join();
        }
        catch (Exception e)
        {
            System.out.println("Exception caught while starting WSServer");
            e.printStackTrace();
        }
    }
}
