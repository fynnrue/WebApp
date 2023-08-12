package com.gpse.basis.domain.service;

import com.gpse.basis.domain.Building;
import com.gpse.basis.domain.Floor;

import java.util.List;
import java.util.Optional;

/**
 * This interface is used to define the methods that are used in the BuildingServiceImpl class.
 */
public interface BuildingService {
    /**
     * @return a list of all buildings stored in the repository.
     */
    List<Building> getBuildings();

    /**
     * Find a building using its id.
     *
     * @param id The id of the Building to search for.
     * @return The Building if it exists
     */
    Optional<Building> getBuildingById(Long id);

    /**
     * Add a Building to the repository.
     *
     * @return The created Building object.
     */
    Building addBuilding(Building building);

    /**
     * Update a Building already in the repository.
     *
     * @param id    The id of the Building
     * @param name  The updated name of the Building
     * @param image The updated image of the Building
     * @return The updated Building.
     */
    Building updateBuilding(String id, String name, Long image);

    boolean deleteBuilding(Building building);
}
