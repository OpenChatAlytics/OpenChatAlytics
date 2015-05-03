package com.hipchalytics.web;

import com.hipchalytics.compute.config.HipChalyticsConfig;
import com.hipchalytics.compute.util.YamlUtils;
import com.hipchalytics.web.resources.TrendingTopicsResource;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import org.apache.storm.guava.collect.Sets;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import java.util.Set;

import javax.ws.rs.core.Application;

/**
 * Entry point to the web resources
 *
 * @author giannis
 *
 */
public class ServerMain extends Application {

    public static final int PORT = 8080;

    private final HipChalyticsConfig config;

    public ServerMain(HipChalyticsConfig config) {
        this.config = config;
    }

    public static void main(String[] args) throws Exception {
        HipChalyticsConfig config = YamlUtils.readYamlFromResource("hipchat.yaml",
                                                                   HipChalyticsConfig.class);
        ServerMain serverMain = new ServerMain(config);

        // Start the server
        Server server = new Server(PORT);
        Context root = new Context(server, "/", Context.SESSIONS);
        ServletContainer servletContainer = new ServletContainer(serverMain);
        ServletHolder servletHolder = new ServletHolder(servletContainer);
        root.addServlet(servletHolder, "/");
        server.start();
    }

    @Override
    public Set<Object> getSingletons() {
        return Sets.newHashSet(new TrendingTopicsResource(config));
    }

}
