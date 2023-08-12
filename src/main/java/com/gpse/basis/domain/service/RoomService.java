package com.gpse.basis.domain.service;

import com.gpse.basis.domain.Room;
import com.gpse.basis.web.cmd.RoomCreationData;

import java.util.Optional;

/**
 * A Service for managing the rooms.
 */
public interface RoomService {
    /**
     * Add a room to the db.
     *
     * @param creationData Necessary data for initializing the room.
     * @return The created Room
     */
    Room addRoom(RoomCreationData creationData);

    /**
     * Remove a room from the db.
     *
     * @param id The id of the room to remove.
     */
    void removeRoom(Long id);

    /**
     * Get all rooms stored in the db.
     *
     * @return An Iterable of all rooms
     */
    Iterable<Room> getAllRooms();

    /**
     * Applies the credential configuration of a room in the Door API provided by Telekom.
     *
     * @param room The room whose credential configuration shall be applied.
     */
    void applyCredentialConfig(Room room);

    /**
     * Find a room with a certain id.
     *
     * @param id The id of the desired room.
     * @return The room if it exists.
     */
    Optional<Room> getRoomById(Long id);
}
