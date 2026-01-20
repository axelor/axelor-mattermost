package com.axelor.apps.mattermost.mattermost.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.mattermost.app.service.AppMattermostService;
import com.axelor.apps.mattermost.exception.MattermostExceptionMessage;
import com.axelor.apps.mattermost.mattermost.MattermostRestChannel;
import com.axelor.apps.mattermost.mattermost.MattermostRestLinker;
import com.axelor.apps.mattermost.mattermost.MattermostRestTeam;
import com.axelor.apps.mattermost.mattermost.MattermostRestUser;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.db.tenants.TenantResolver;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppMattermost;
import com.axelor.studio.db.repo.AppMattermostRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.client.ClientProtocolException;
import wslite.json.JSONException;

public class MattermostServiceImpl implements MattermostService {

  protected final AppMattermostService appMattermostService;
  protected final AppMattermostRepository appMattermostRepository;
  protected String url;
  protected String token;
  protected String teamId;

  @Inject
  public MattermostServiceImpl(
      AppMattermostService appMattermostService, AppMattermostRepository appMattermostRepository) {
    this.appMattermostService = appMattermostService;
    this.appMattermostRepository = appMattermostRepository;
  }

  public void initialize() throws AxelorException {
    getUrl();
    getAuthentificationToken();
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void syncProject(Project project) {
    try {
      initialize();
      getTeamId();
      if (project.getChatVisibilitySelect() == ProjectRepository.CHAT_VISIBILITY_NO_CHAT) {
        removeChannel(project);
        project.setMattermostChannelId(null);
        return;
      }
      if (ObjectUtils.isEmpty(project.getMattermostChannelId())) {
        project.setMattermostChannelId(createChannel(project));
      } else {
        updateProjectName(project);
      }
      Collection<User> userCollection = project.getMembersUserSet();
      createUsers(userCollection);
      linkUsersToTeamAndChannel(project);
    } catch (Exception e) {
      TraceBackService.trace(e, "mattermost");
    }
  }

  protected void createUsers(Collection<User> userCollection) throws AxelorException {
    for (User user : userCollection) {
      createUser(user);
    }
  }

  @Override
  public void createUser(User user) {
    try {
      initialize();
      if (ObjectUtils.notEmpty(user.getMattermostUserId())) {
        return;
      }
      checkMail(user);
      String email = user.getEmail();
      String userId =
          new MattermostRestUser(url, token)
              .createUser(user.getId(), email, user.getName(), "", email);
      if (ObjectUtils.isEmpty(userId)) {
        return;
      }
      user.setMattermostUserId(userId);
      saveUser(user);
    } catch (Exception e) {
      TraceBackService.trace(e, "mattermost");
    }
  }

  @Override
  public String createUser(String name, String firstName, String email, String password) {
    try {
      initialize();
      return new MattermostRestUser(url, token).createUser(null, email, firstName, name, password);
    } catch (Exception e) {
      TraceBackService.trace(e, "mattermost");
      return null;
    }
  }

  @Transactional(rollbackOn = Exception.class)
  protected void saveUser(User user) {

    JPA.save(user);
  }

  @Transactional(rollbackOn = Exception.class)
  protected void saveAppMattermost(AppMattermost appMattermost) {

    appMattermostRepository.save(appMattermost);
  }

  @Override
  public void createTeam() throws AxelorException {

    initialize();
    AppMattermost appMattermost = appMattermostService.getAppMattermost();
    if (ObjectUtils.notEmpty(appMattermost.getTeamId())) {
      return;
    }
    String tenantName = TenantResolver.currentTenantIdentifier();
    String teamName = appMattermost.getTenantName();
    if (ObjectUtils.isEmpty(teamName) && ObjectUtils.isEmpty(tenantName)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(MattermostExceptionMessage.MATTERMOST_API_MISSING_TENANT_NAME));
    }
    String teamId =
        new MattermostRestTeam(url, token)
            .createTeam(!ObjectUtils.isEmpty(tenantName) ? tenantName : teamName);
    appMattermost.setTeamId(teamId);
    saveAppMattermost(appMattermost);
  }

  protected String createChannel(Project project) throws AxelorException {
    if (project.getChatVisibilitySelect() == ProjectRepository.CHAT_VISIBILITY_NO_CHAT) {
      return null;
    }

    checkProject(project);
    String name = project.getName();

    return new MattermostRestChannel(url, token).createChannel(teamId, name);
  }

  protected void linkUsersToTeamAndChannel(Project project) throws AxelorException {
    if (project.getChatVisibilitySelect() == ProjectRepository.CHAT_VISIBILITY_NO_CHAT) {
      return;
    }

    checkProject(project);
    String channelId = project.getMattermostChannelId();

    Collection<User> collectionUser = project.getMembersUserSet();
    MattermostRestLinker mattermostRestLinker = new MattermostRestLinker(url, token);
    List<String> userIDs = new ArrayList<String>();
    for (User user : collectionUser) {
      try {
        if (!user.getCanAccessChat()) {
          removeUserFromChannel(project, user.getMattermostUserId());
          continue;
        }
        userIDs.add(user.getMattermostUserId());
        mattermostRestLinker.linkUsersToTeamAndChannel(
            user.getMattermostUserId(), teamId, channelId);
      } catch (Exception e) {
        TraceBackService.trace(e, "mattermost");
      }
    }
    removeNotFoundUsers(userIDs, project);
    return;
  }

  public void removeNotFoundUsers(List<String> userIDs, Project project) {
    try {
      List<String> foundUserIds =
          new MattermostRestChannel(url, token).getMemberList(project.getMattermostChannelId());
      for (String id : foundUserIds) {
        if (!userIDs.contains(id)) {
          User user = findUserByMattermostId(id);
          if (user == null) {
            continue;
          }
          removeUserFromChannel(project, id);
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(e, "mattermost");
    }
  }

  protected User findUserByMattermostId(String mattermostUserId) {
    return Query.of(User.class)
        .filter("self.mattermostUserId = :mattermostUserId")
        .bind("mattermostUserId", mattermostUserId)
        .fetchOne();
  }

  protected void checkMail(User user) throws AxelorException {

    if (ObjectUtils.isEmpty(user.getEmail())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          MattermostExceptionMessage.MATTERMOST_MISSING_MAIL_ON_USER,
          user.getCode());
    }
  }

  protected void checkProject(Project project) throws AxelorException {

    String name = project.getName();
    if (ObjectUtils.isEmpty(name)) {

      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          MattermostExceptionMessage.MATTERMOST_MISSING_NAME_ON_PROJECT,
          project.getCode());
    }
  }

  protected void removeChannel(Project project) throws AxelorException {
    new MattermostRestChannel(url, token).deleteChannel(project.getMattermostChannelId());
  }

  @Override
  public void updateChannelForUser(User user, boolean canAccessChat) throws AxelorException {

    initialize();
    String userId = user.getMattermostUserId();
    if (ObjectUtils.isEmpty(userId)) {
      return;
    }
    List<Project> projectWithUser =
        JPA.all(Project.class)
            .filter(":user MEMBER OF self.membersUserSet AND self.chatVisibilitySelect > 1")
            .bind("user", user)
            .fetch();
    if (CollectionUtils.isEmpty(projectWithUser)) {
      return;
    }
    for (Project project : projectWithUser) {
      try {
        if (!canAccessChat) {
          removeUserFromChannel(project, userId);
        } else {
          addUserToChannel(project, userId);
        }
      } catch (Exception e) {
        TraceBackService.trace(e, "mattermost");
      }
    }
  }

  protected void addUserToChannel(Project project, String mattermostUserId) throws AxelorException {
    initialize();
    getTeamId();
    if (mattermostUserId == null) {
      return;
    }
    new MattermostRestLinker(url, token)
        .linkUser(mattermostUserId, teamId, project.getMattermostChannelId());
  }

  protected void removeUserFromChannel(Project project, String mattermostUserId)
      throws AxelorException {
    initialize();
    checkProject(project);
    if (mattermostUserId == null) {
      return;
    }
    new MattermostRestLinker(url, token)
        .unlinkUser(mattermostUserId, project.getMattermostChannelId());
  }

  @Override
  public void updateUser(User user, String name)
      throws AxelorException, ClientProtocolException, IOException, JSONException {
    initialize();
    checkMail(user);
    new MattermostRestUser(url, token)
        .updateUser(user.getId(), user.getEmail(), name, "", user.getMattermostUserId());
  }

  protected void updateProjectName(Project project)
      throws ClientProtocolException, AxelorException, IOException, JSONException {
    String name = project.getName();
    new MattermostRestChannel(url, token).updateChannel(name, project.getMattermostChannelId());
  }

  protected void getAuthentificationToken() throws AxelorException {
    AppMattermost appMattermost = appMattermostService.getAppMattermostNoFlush();
    String identificationToken = appMattermost.getMattermostToken();
    if (ObjectUtils.isEmpty(identificationToken)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(MattermostExceptionMessage.MATTERMOST_API_MISSING_IDENTIFICATION_TOKEN));
    }
    this.token = identificationToken;
  }

  protected void getUrl() throws AxelorException {
    AppMattermost appMattermost = appMattermostService.getAppMattermostNoFlush();
    String url = appMattermost.getMattermostUrl();
    if (ObjectUtils.isEmpty(url)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(MattermostExceptionMessage.MATTERMOST_API_MISSING_URL));
    }
    this.url = url;
  }

  protected void getTeamId() throws AxelorException {
    AppMattermost appMattermost = appMattermostService.getAppMattermostNoFlush();
    String teamId = appMattermost.getTeamId();
    if (ObjectUtils.isEmpty(url)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(MattermostExceptionMessage.MATTERMOST_API_MISSING_TEAM_ID));
    }
    this.teamId = teamId;
  }

  @Override
  public Long getUnreadMessage(String userId) throws AxelorException, IOException, JSONException {
    initialize();
    return new MattermostRestTeam(url, token).getUnreadMsg(userId);
  }

  @Override
  public void deleteUser(String mattermostUserId)
      throws AxelorException, IOException, JSONException {
    new MattermostRestUser(url, token).deleteUser(mattermostUserId);
  }
}
