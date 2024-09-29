package com.wellnest.comic.dao;

import com.wellnest.comic.model.Comic;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComicRepo extends CrudRepository<Comic, Long> {
    List<Comic> findByUserId(Integer userId);

}
