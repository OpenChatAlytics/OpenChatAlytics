package com.chatalytics.web;

import com.chatalytics.compute.util.YamlUtils;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.web.resources.TopEmojisResource;
import com.chatalytics.web.resources.TrendingTopicsResource;
import com.google.common.collect.Sets;
import com.sun.jersey.spi.container.servlet.ServletContainer;

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

    private final ChatAlyticsConfig config;

    public ServerMain(ChatAlyticsConfig config) {
        this.config = config;
    }

    public static void main(String[] args) throws Exception {
        ChatAlyticsConfig config = YamlUtils.readYamlFromResource("chatalytics.yaml",
                                                                  ChatAlyticsConfig.class);
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
        return Sets.newHashSet(new TrendingTopicsResource(config),
                               new TopEmojisResource(config));
    }

}
