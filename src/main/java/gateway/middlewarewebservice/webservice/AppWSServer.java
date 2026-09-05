package gateway.middlewarewebservice.webservice;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import pk.vaulsys.apigateway.network.channel.base.Channel;
import pk.vaulsys.apigateway.network.remote.webservice.resource.CustomErrorHandler;
import pk.vaulsys.apigateway.protocols.PaymentSchemes.base.ChannelCodes;
import pk.vaulsys.apigateway.protocols.webservice.base.entity.WebServiceEntity;
import pk.vaulsys.apigateway.protocols.webservice.base.handler.CORSFilter;
import pk.vaulsys.apigateway.util.Util;
import pk.vaulsys.apigateway.util.WSEncryptionUtil;
import pk.vaulsys.apigateway.util.WebServiceUtil;
import pk.vaulsys.apigateway.wfe.GlobalContext;

/**
 * Created by Raza on 02-Feb-18.
 */
public class AppWSServer implements Runnable {
    Logger logger = LogManager.getLogger(this.getClass());
    public static long id=0;
    //public static ExecutorService executor;// = MessageManager.threadPool;

    public AppWSServer()
    {
        //executor = MessageManager.threadPool;
    }

    public void run() { //TODO: Raza update Below Implementation... 04-05-2020
        try {

            Channel channel = GlobalContext.getInstance().getChannelbyId(ChannelCodes.MOBILEAPP);

            if (channel == null) {
                logger.error("No Channel Found against ChannelId [{}] in DB, cannot start Channel!", ChannelCodes.MOBILEAPP);
                return;
            }

            Server server = null;
            QueuedThreadPool threadPool = new QueuedThreadPool(200, 8, 1000);

            if(channel.getSslEnable() != null && channel.getSslEnable())
            {
                if(!Util.hasText(channel.getKeystorepath()) || !Util.hasText(channel.getKeystorealias()) || !Util.hasText(channel.getKeystorepass()))
                {
                    logger.error("Invalid SSL configuration found against Channel [" + ChannelCodes.MOBILEAPP + "], not starting server...!");
                    return;
                }

                server = new Server(threadPool);

                // HTTP Configuration
                HttpConfiguration http = new HttpConfiguration();
                http.addCustomizer(new SecureRequestCustomizer());

                // Configuration for HTTPS redirect
                http.setSecurePort(8443);
                http.setSecureScheme("https");
                http.setSendServerVersion( false );
                ServerConnector connector = new ServerConnector(server);
                connector.addConnectionFactory(new HttpConnectionFactory(http));
                // Setting HTTP port
                connector.setPort(channel.getPort());

                // HTTPS configuration
                HttpConfiguration https = new HttpConfiguration();
                https.addCustomizer(new SecureRequestCustomizer());
                https.setSendServerVersion(false);
                // Configuring SSL
                SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();

                // Defining keystore path and passwords
                logger.info("KeyStore Path [{}]", WSEncryptionUtil.getDecryptedChannelKeyStoreField(channel.getKeystorepath()));
                logger.info("KeyStore Alias [{}]", WSEncryptionUtil.getDecryptedChannelKeyStoreField(channel.getKeystorealias()));
                logger.info("KeyStore Pass [{}]", WSEncryptionUtil.getDecryptedChannelKeyStoreField(channel.getKeystorepass()));

                sslContextFactory.setKeyStorePath(WSEncryptionUtil.getDecryptedChannelKeyStoreField(channel.getKeystorepath()));
                sslContextFactory.setCertAlias(WSEncryptionUtil.getDecryptedChannelKeyStoreField(channel.getKeystorealias()));
                sslContextFactory.setKeyStorePassword(WSEncryptionUtil.getDecryptedChannelKeyStoreField(channel.getKeystorepass()));

                sslContextFactory.addExcludeProtocols("SSLv3");

                // Configuring the connector
                ServerConnector sslConnector = new ServerConnector(server, new SslConnectionFactory(sslContextFactory, "http/1.1"), new HttpConnectionFactory(https));
                sslConnector.setPort(channel.getSslPort());

                // Setting HTTP and HTTPS connectors
                server.setConnectors(new Connector[]{connector, sslConnector});

            }
            else
            {
                server = new Server(channel.getPort());

                HttpConfiguration httpConfig = new HttpConfiguration();
                httpConfig.setSendServerVersion( false );
                HttpConnectionFactory httpFactory = new HttpConnectionFactory( httpConfig );
                ServerConnector httpConnector = new ServerConnector( server,httpFactory );
                server.setConnectors( new Connector[] { httpConnector } );
            }

            //ServletHolder sh = new ServletHolder(ServletContainer.class);
            ServletHolder sh = new ServletHolder(new ServletContainer());
            sh.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
            sh.setInitParameter(WebServiceEntity.class.getName(), "com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider");
            sh.setInitParameter("com.sun.jersey.config.property.packages", "pk.vaulsys.apigateway.protocols.webservice.middlewarewebservice.resource");
            sh.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");
            sh.setInitParameter("com.sun.jersey.config.feature.DisableWADL", "true");

            ServletContextHandler context = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
            context.addServlet(sh, "/*");
            context.setErrorHandler(new CustomErrorHandler());

            // Add the CORS Filter
            CORSFilter corsFilter = new CORSFilter();
            FilterHolder corsFilterHolder = new FilterHolder(corsFilter);
            context.addFilter(corsFilterHolder, "/*", null);


            while(true)
            {
                Thread.sleep(7000);
//                logger.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
//                logger.info("Server isRunning [" + server.isRunning() + "]");
//                logger.info("Server isFailed [" + server.isFailed() + "]");
//                logger.info("Server getState [" + server.getState() + "]");
//                logger.info("Server getSessionIdManager().isRunning [" + server.getSessionIdManager().isRunning() + "]");
//                logger.info("Server getSessionIdManager().isFailed [" + server.getSessionIdManager().isFailed() + "]");
//                logger.info("Server getSessionIdManager().isStarted [" + server.getSessionIdManager().isStarted() + "]");
//                logger.info("Server getSessionIdManager().isStopped [" + server.getSessionIdManager().isStopped() + "]");
//                logger.info("Server getSessionIdManager().isStopping [" + server.getSessionIdManager().isStopping() + "]");
//                logger.info("Context isStarted [" + context.isStarted() + "]");
//                logger.info("Context isAvailable [" + context.isAvailable() + "]");
//                logger.info("Context getState [" + context.getState() + "]");
//                logger.info("Context isRunning [" + context.isRunning() + "]");
//                logger.info("Context isFailed [" + context.isFailed() + "]");
//                logger.info("Context isStarted [" + context.isStarted() + "]");
//                logger.info("Context isStopping [" + context.isStopping() + "]");
//                logger.info("Context isShutdown [" + context.isShutdown() + "]");
//                logger.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");


                if(!server.isRunning())
                {
                    logger.info("Starting AppWSServer....!");
                    server.start();
                    //server.join();
                }
                else if(!context.isAvailable() )
                {
                    server.destroy();
                    logger.info("Context not available, restarting AppWSServer....!");
                    server.start();
                }
                else  //TODO: Please delete me
                {
                    logger.info("AppWSServer already running...!");
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Exception caught while starting AppWSServer");
            logger.error(WebServiceUtil.getStrException(e));
        }
    }
}
