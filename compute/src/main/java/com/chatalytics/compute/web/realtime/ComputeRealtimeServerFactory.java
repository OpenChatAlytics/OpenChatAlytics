package com.chatalytics.compute.web.realtime;

import com.chatalytics.compute.web.resources.StatusResource;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import javax.servlet.ServletException;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;

/**
 * Factory for building the realtime compute server
 *
 * @author giannis
 */
public class ComputeRealtimeServerFactory {

    /**
     * Creates a new {@link ComputeRealtimeServer}
     *
     * @param config
     *            The chatalytics config
     * @return A newly created {@link ComputeRealtimeServer}
     */
    public static ComputeRealtimeServer createComputeRealtimeServer(ChatAlyticsConfig config) {
        Server server = new Server(config.computeConfig.rtComputePort);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        ServletHolder jerseyServlet = context.addServlet(ServletContainer.class, "/*");
        jerseyServlet.setInitParameter(PackagesResourceConfig.PROPERTY_PACKAGES,
                                       StatusResource.class.getPackage().toString());
        server.setHandler(context);
        ServerContainer wscontainer;
        try {
            wscontainer = WebSocketServerContainerInitializer.configureContext(context);
            wscontainer.addEndpoint(RealtimeResource.class);
        } catch (ServletException | DeploymentException e) {
            throw new RuntimeException("Can't instantiate websocket. Reason: " + e.getMessage());
        }

        return new ComputeRealtimeServer(server);
    }

}
