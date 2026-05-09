package com.project.artconnect.service.impl;

import com.project.artconnect.model.Discipline;
import com.project.artconnect.persistence.JdbcDisciplineDao;
import com.project.artconnect.service.DisciplineService;

import java.util.List;

public class JdbcDisciplineService implements DisciplineService {
    private final JdbcDisciplineDao dao = new JdbcDisciplineDao();

    @Override
    public List<Discipline> getAllDisciplines() {
        return dao.findAll();
    }

    @Override
    public void createDiscipline(Discipline d) {
        dao.save(d);
    }
}

