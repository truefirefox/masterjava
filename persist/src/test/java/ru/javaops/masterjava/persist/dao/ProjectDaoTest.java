package ru.javaops.masterjava.persist.dao;

import org.junit.Test;
import ru.javaops.masterjava.persist.model.Project;

/**
 * Created by val on 2017-04-06.
 */
public class ProjectDaoTest extends AbstractDaoTest<ProjectDao>{

    public ProjectDaoTest() {
        super(ProjectDao.class);
    }

    @Test
    public void insertGeneratedId() throws Exception {
        Project project = new Project(100200, "Topjava", "topjava");
        int result = dao.insertGeneratedId(project);
        System.out.println(result);
    }

    @Test
    public void insertWitId() throws Exception {
        Project project = new Project(100201, "Topjava", "topjava");
        System.out.println(dao.insertWitId(project));

    }

}