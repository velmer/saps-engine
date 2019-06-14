package org.fogbowcloud.saps.engine.scheduler.restlet.resource;

import org.fogbowcloud.saps.engine.core.model.ImageTask;
import org.restlet.data.Form;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.restlet.util.Series;

import java.util.Arrays;

public class TestResource extends ServerResource {

    private static final String REQUEST_ATTR_PROCESSED_IMAGES = "images_id[]";
    public static final String RESTLET_HTTP_HEADERS = "org.restlet.http.headers";

    @SuppressWarnings("unchecked")
    @Post
    public Representation testPost(Representation representation) {
        Form form = new Form(representation);
        ClientResource clientResource = new ClientResource("http://localhost:5000/test");
//        clientResource.setAttribute("images_id[]", "X");
        clientResource.addQueryParameter("images_id[]", "X");
//        clientResource.addQueryParameter("images_id[]", "Y");
//        clientResource.addQueryParameter("images_id[]", "Z");
        clientResource.get(MediaType.APPLICATION_JSON);
        return new StringRepresentation("TestResource.POST", MediaType.TEXT_PLAIN);
    }

    @SuppressWarnings("unchecked")
    @Get
    public Representation testGet() {
        Series<Header> series = (Series<Header>) getRequestAttributes()
                .get("org.restlet.http.headers");

        String[] imageTasksIdsHeader = series.getValuesArray(REQUEST_ATTR_PROCESSED_IMAGES, true);
        System.out.println("==> Arrays.toString(imageTasksIdsHeader): " + Arrays.toString(imageTasksIdsHeader));

        Form query = getQuery();
        String[] imageTasksIdsQuery = query.getValues("images_id[]").split(",");
        System.out.println("==> Arrays.toString(imageTasksIdsQuery): " + Arrays.toString(imageTasksIdsQuery));
        return new StringRepresentation("TestResource.GET", MediaType.APPLICATION_JSON);
    }

}
