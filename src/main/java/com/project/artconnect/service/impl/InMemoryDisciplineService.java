package com.project.artconnect.service.impl;

import com.project.artconnect.model.Discipline;
import com.project.artconnect.service.DisciplineService;

import java.util.ArrayList;
import java.util.List;

public class InMemoryDisciplineService implements DisciplineService {
    private final List<Discipline> list = new ArrayList<>();

    @Override
    public List<Discipline> getAllDisciplines() {
        return new ArrayList<>(list);
    }

    @Override
    public void createDiscipline(Discipline d) {
        list.add(d);
    }
}

