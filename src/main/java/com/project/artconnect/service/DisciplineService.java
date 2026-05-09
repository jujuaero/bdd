package com.project.artconnect.service;

import com.project.artconnect.model.Discipline;

import java.util.List;

public interface DisciplineService {
    List<Discipline> getAllDisciplines();
    void createDiscipline(Discipline d);
}

