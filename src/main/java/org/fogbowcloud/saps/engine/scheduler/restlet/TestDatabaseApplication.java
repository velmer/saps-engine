package org.fogbowcloud.saps.engine.scheduler.restlet;

import org.fogbowcloud.saps.engine.scheduler.restlet.resource.TestResource;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;
import org.restlet.service.ConnectorService;
import org.restlet.service.CorsService;

import java.util.Collections;
import java.util.HashSet;

public class TestDatabaseApplication extends Application {

    private Component restletComponent;

    public TestDatabaseApplication() {
        // CORS configuration
        CorsService cors = new CorsService();
        cors.setAllowedOrigins(new HashSet<>(Collections.singletonList("*")));
        cors.setAllowedCredentials(true);
        getServices().add(cors);
    }

    public void startServer() throws Exception {
        ConnectorService corsService = new ConnectorService();
        this.getServices().add(corsService);

        this.restletComponent = new Component();
        this.restletComponent.getServers().add(Protocol.HTTP, 5000);
        this.restletComponent.getClients().add(Protocol.HTTP);
        this.restletComponent.getClients().add(Protocol.FILE);
        this.restletComponent.getDefaultHost().attach(this);
        this.restletComponent.start();
        System.out.println("TestDatabaseApplication started");
    }

    public void stopServer() throws Exception {
        this.restletComponent.stop();
    }

    @Override
    public Restlet createInboundRoot() {
        // TODO: change endpoints for new SAPS dashboard
        Router router = new Router(getContext());
        router.attach("/test", TestResource.class);
        return router;
    }

}
