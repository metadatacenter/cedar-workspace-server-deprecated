package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import org.metadatacenter.model.folderserver.CedarFSUser;
import org.metadatacenter.model.response.FSUserListResponse;
import org.metadatacenter.server.neo4j.Neo4JUserSession;
import org.metadatacenter.server.security.Authorization;
import org.metadatacenter.server.security.CedarAuthFromRequestFactory;
import org.metadatacenter.server.security.exception.CedarAccessException;
import org.metadatacenter.server.security.model.IAuthRequest;
import org.metadatacenter.server.security.model.auth.CedarPermission;
import org.metadatacenter.server.security.model.user.CedarUser;
import org.metadatacenter.util.json.JsonMapper;
import play.mvc.Result;
import utils.DataServices;

import java.util.List;

public class UserController extends AbstractFolderServerController {


  public static Result findUsers() {
    IAuthRequest frontendRequest = null;
    CedarUser currentUser = null;
    try {
      frontendRequest = CedarAuthFromRequestFactory.fromRequest(request());
      currentUser = Authorization.getUserAndEnsurePermission(frontendRequest, CedarPermission.LOGGED_IN);
    } catch (CedarAccessException e) {
      play.Logger.error("Access Error while reading the users", e);
      return forbiddenWithError(e);
    }

    try {
      Neo4JUserSession neoSession = DataServices.getInstance().getNeo4JSession(currentUser);

      List<CedarFSUser> users = neoSession.findUsers();

      FSUserListResponse r = new FSUserListResponse();

      r.setUsers(users);

      JsonNode resp = JsonMapper.MAPPER.valueToTree(r);
      return ok(resp);

    } catch (Exception e) {
      play.Logger.error("Error while listing users", e);
      return internalServerErrorWithError(e);
    }
  }

}