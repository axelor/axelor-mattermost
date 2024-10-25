package com.axelor.apps.mattermost.project.fix;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.mattermost.mattermost.service.MattermostService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.google.inject.Inject;

public class ProjectFix {

  protected final ProjectRepository projectRepository;
  protected final MattermostService matterMostService;
  protected final UserRepository userRepository;
  protected final PartnerRepository partnerRepository;
  protected static final int FETCH_LIMIT = 10;
  protected static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject
  public ProjectFix(
      ProjectRepository projectRepository,
      MattermostService matterMostService,
      UserRepository userRepository,
      PartnerRepository partnerRepository) {
    this.projectRepository = projectRepository;
    this.matterMostService = matterMostService;
    this.userRepository = userRepository;
    this.partnerRepository = partnerRepository;
  }

  public void initializeMattermost() throws AxelorException {

    matterMostService.createTeam();
    createUsers();
    createProject();
  }

  protected void createProject() {
    Query<Project> projectQuery =
        projectRepository
            .all()
            .filter("self.archived is null OR self.archived = false")
            .order("id");
    List<Project> projectList = new ArrayList<Project>();
    int OFFSET = 0;
    long projectToUpdate = projectQuery.count();
    while (!(projectList = projectQuery.fetch(FETCH_LIMIT, OFFSET)).isEmpty()) {
      projectList.forEach(matterMostService::syncProject);
      JPA.clear();
      OFFSET += projectList.size();
      LOG.debug("Done " + OFFSET + "/" + projectToUpdate);
    }
  }

  protected void createUsers() {
    Query<User> userQuery =
        userRepository
            .all()
            .filter("(self.archived is null OR self.archived = false) AND self.email != null")
            .order("id");
    List<User> userList = new ArrayList<User>();
    int OFFSET = 0;
    long userToUpdate = userQuery.count();
    while (!(userList = userQuery.fetch(FETCH_LIMIT, OFFSET)).isEmpty()) {
      userList.forEach(matterMostService::createUser);
      JPA.clear();
      OFFSET += userList.size();
      LOG.debug("Done " + OFFSET + "/" + userToUpdate);
    }
  }
}
