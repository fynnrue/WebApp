package com.gpse.basis.domain.service;

import com.gpse.basis.domain.RoomGroup;
import com.gpse.basis.web.cmd.RoomGroupCreation;

import java.util.Optional;

/**
 * Service for managing RoomGroups.
 */
public interface RoomGroupService {

    /**
     * Add a roomGroup to the db.
     * @param data The roomGroup to add.
     * @return The roomGroup which was added.
     */
    RoomGroup addRoomGroup(RoomGroupCreation data);

    /**
     * Get all roomGroups in the db.
     * @return All roomGroups stored in the db.
     */
    Iterable<RoomGroup> getAllRoomGroups();

    /**
     * Get a roomGroup with a specified id.
     * @param id The id of the desired roomGroup.
     * @return The roomGroup if it exists in the database.
     */
    Optional<RoomGroup> findRoomGroupById(Long id);

    /**
     * Remove a roomGroup from the database.
     * @param id The id of the roomGroup to delete.
     */
    void deleteRoomGroup(Long id);


    void updateRoomGroup(Long roomId, Long groupId);
}
