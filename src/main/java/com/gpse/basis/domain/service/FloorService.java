package com.gpse.basis.domain.service;

import com.gpse.basis.domain.Floor;
import com.gpse.basis.domain.Room;
import com.gpse.basis.domain.RoomGroup;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FloorService {
    /**
     * @return a list of all buildings stored in the repository.
     */
    List<Floor> getFloors();

    /**
     * Find a floor using its id.
     *
     * @param id The id of the Building to search for.
     * @return The Building if it exists
     */
    Optional<Floor> getFloorById(Long id);

    /**
     * Add a Floor to the repository.
     *
     * @param name  The name of the floor.
     * @param image The id of the preview image.
     * @return The created Floor object.
     */
    Floor addFloor(String name, Long image);

    /**
     * Update a Floor already in the repository.
     *
     * @param id    The id of the Floor
     * @param name  The updated name of the Floor
     * @param image The updated image of the Floor
     * @return The updated Floor.
     */
    Floor updateFloor(String id, String name, Long image);

    Floor updateImage(String id, Long image);

    Floor updateName(String id, String name);

    /**
     * Get all Rooms of a Floor.
     *
     * @param floorId The id of the Floor
     * @return The Rooms of the Floor
     */
    List<Room> getRoomsOfFloor(Long floorId);

    /**
     * Get all RoomGroups of a Floor.
     *
     * @param floorId The id of the Floor
     * @return The RoomGroups of the Floor
     */
    Collection<RoomGroup> getRoomGroupsOfFloor(Long floorId);

    boolean deleteFloor(Floor floor);
}