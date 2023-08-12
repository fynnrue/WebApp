package com.gpse.basis.domain.service;

import com.gpse.basis.domain.Building;
import com.gpse.basis.domain.repository.BuildingRepository;
import com.gpse.basis.web.exceptions.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/*
 * This class is responsible for the building management.
 */
@Service
public class BuildingServiceImpl implements BuildingService {

    private final BuildingRepository buildingRepository;

    @Autowired
    public BuildingServiceImpl(final BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }

    @Override
    public List<Building> getBuildings() {
        final List<Building> buildings = new ArrayList<>();
        buildingRepository.findAll().forEach(buildings::add);
        return buildings;
    }

    @Override
    public Optional<Building> getBuildingById(final Long id) {
        return buildingRepository.findById(id);
    }

    @Override
    public Building addBuilding(final Building building) {
        return buildingRepository.save(building);
    }

    @Override
    public Building updateBuilding(final String id, final String name, Long image) {
        final Long arg = Long.valueOf(id);
        final Building building = buildingRepository.findById(arg).orElseThrow(BadRequestException::new);
        building.setName(name);
        building.setImageId(image);

        return buildingRepository.save(building);
    }

    @Override
    public boolean deleteBuilding(final Building building) {
        boolean exists = true;
        if (building.getId() != null) {
            buildingRepository.delete(building);
        } else {
            exists = false;
        }
        return exists;
    }
}
