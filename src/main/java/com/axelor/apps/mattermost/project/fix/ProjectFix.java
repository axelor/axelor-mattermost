package com.axelor.apps.mattermost.project.fix;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.mattermost.mattermost.service.MattermostService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectFix {

  protected final ProjectRepository projectRepository;
  protected final MattermostService matterMostService;
  protected static final int FETCH_LIMIT = 10;
  protected static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject
  public ProjectFix(ProjectRepository projectRepository, MattermostService matterMostService) {
    this.projectRepository = projectRepository;
    this.matterMostService = matterMostService;
  }

  public void initializeMattermost() throws AxelorException {

    matterMostService.createTeam();
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
}
