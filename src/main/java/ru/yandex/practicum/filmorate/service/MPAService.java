package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.DBMPARepository;
import ru.yandex.practicum.filmorate.exception.MPANotFoundException;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.repository.MPARepository;

import java.util.List;

@Service
public class MPAService {
    private final MPARepository mpaRepository;

    public MPAService(DBMPARepository mpaRepository) {
        this.mpaRepository = mpaRepository;
    }

    public List<MPA> findAll(){
        return mpaRepository.findAll();
    }

    public MPA getMPA(int id){
        MPA mpa = mpaRepository.getMPA(id);
        if (mpa == null){
            throw new MPANotFoundException("MPA с id " + id + " не существует");
        }
        return mpa;
    }
}
