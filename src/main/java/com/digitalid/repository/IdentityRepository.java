package com.digitalid.repository;

import java.util.List;
import com.digitalid.domain.DigitalID;

public interface IdentityRepository {

    void save(DigitalID identity);

    DigitalID findById(String id);

    List<DigitalID> findAll();

    void delete(String id);

    boolean exists(String id);
}