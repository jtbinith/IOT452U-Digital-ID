package com.digitalid.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import com.digitalid.domain.DigitalID;

public class InMemoryIdentityRepository implements IdentityRepository {

    private final ConcurrentHashMap<String, DigitalID> identities = new ConcurrentHashMap<>();

    @Override
    public void save(DigitalID identity) {
        identities.put(identity.getId(), identity);
    }

    @Override
    public DigitalID findById(String id) {
        return identities.get(id);
    }

    @Override
    public List<DigitalID> findAll() {
        return new ArrayList<>(identities.values());
    }
}
