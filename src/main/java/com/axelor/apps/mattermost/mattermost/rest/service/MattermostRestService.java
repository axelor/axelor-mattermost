package com.axelor.apps.mattermost.mattermost.rest.service;

import com.axelor.apps.mattermost.mattermost.dto.CreateUserPostRequest;
import com.axelor.apps.mattermost.mattermost.dto.CreateUserResponse;
import com.axelor.apps.mattermost.mattermost.service.MattermostService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.RequestValidator;
import com.axelor.utils.api.ResponseConstructor;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/user")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MattermostRestService {

  @Path("/createUser")
  @POST
  @HttpExceptionHandler
  public Response createUser(CreateUserPostRequest request) {
    RequestValidator.validateBody(request);

    String userId =
        Beans.get(MattermostService.class)
            .createUser(
                request.getName(),
                request.getFirstName(),
                request.getMail(),
                request.getPassword());
    return ResponseConstructor.build(
        Response.Status.OK, I18n.get("User created."), new CreateUserResponse(0, userId));
  }
}
