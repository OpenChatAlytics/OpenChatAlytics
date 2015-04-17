package com.hipchalytics.web;

import com.hipchalytics.web.resources.TrendingTopicsResource;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

public class ServerMain {

    public static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        Server server = new Server(PORT);
        Context root = new Context(server, "/", Context.SESSIONS);
        String pkg = TrendingTopicsResource.class.getPackage().getName();
        PackagesResourceConfig packagesResourceConfig = new PackagesResourceConfig(pkg);
        ServletContainer servletContainer = new ServletContainer(packagesResourceConfig);
        ServletHolder servletHolder = new ServletHolder(servletContainer);
        root.addServlet(servletHolder, "/");
        server.start();
    }

}
