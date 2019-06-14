package org.fogbowcloud.saps.engine.scheduler.restlet.resource;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import java.util.Arrays;
import java.util.Objects;

public class TestResource extends ServerResource {

    private static final String REQUEST_ATTR_PROCESSED_IMAGES = "images_id[]";

    @SuppressWarnings("unchecked")
    @Post
    public Representation testPost(Representation representation) {
        Form form = new Form(representation);
        ClientResource clientResource = new ClientResource("http://localhost:5000/test");
        clientResource.addQueryParameter("images_id[]", "X");
        clientResource.addQueryParameter("images_id[]", "Y");
        clientResource.addQueryParameter("images_id[]", "Z");
        clientResource.get(MediaType.APPLICATION_JSON);
        return new StringRepresentation("TestResource.POST", MediaType.TEXT_PLAIN);
    }

    @SuppressWarnings("unchecked")
    @Get
    public Representation testGet() {
        String imageTasksIdsValue = getQuery().getValues(REQUEST_ATTR_PROCESSED_IMAGES);
        if (Objects.nonNull(imageTasksIdsValue)) {
            String[] imageTasksIdsQuery = imageTasksIdsValue.split(",");
            System.out.println("==> Arrays.toString(imageTasksIdsQuery): " + Arrays.toString(imageTasksIdsQuery));
        }
        return new StringRepresentation("TestResource.GET", MediaType.APPLICATION_JSON);
    }

}
