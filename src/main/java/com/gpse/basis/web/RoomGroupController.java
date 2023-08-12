package com.gpse.basis.web;

import com.fasterxml.jackson.annotation.JsonView;
import com.gpse.basis.domain.Room;
import com.gpse.basis.domain.RoomGroup;
import com.gpse.basis.domain.SesamUser;
import com.gpse.basis.domain.Views;
import com.gpse.basis.domain.service.RoomGroupService;
import com.gpse.basis.domain.service.RoomService;
import com.gpse.basis.web.cmd.RoomCreationData;
import com.gpse.basis.web.cmd.RoomGroupInfo;
import com.gpse.basis.web.exceptions.NotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Optional;

/**
 * The RoomGroupController class is a RestController that handles API requests related to room groups.
 * <p>
 * RoomGroups are used to group rooms together, e.g. by floor or by building.
 * Each room group has a name and a color associated with it.
 * <p>
 * The RoomGroupController provides methods for retrieving, storing, updating, and deleting room groups.
 */

@RestController
@CrossOrigin
@RequestMapping("/api")
public class RoomGroupController {

    private final RoomGroupService roomGroupService;
    private final RoomService roomService;

    private static final Logger log = LoggerFactory.getLogger(RoomGroupController.class);

    /**
     * Constructor for a new RoomGroupController
     *
     * @param roomGroupService the service used for managing room groups
     * @param roomService the service used for managing rooms
     */
    @Autowired
    public RoomGroupController(final RoomGroupService roomGroupService, final RoomService roomService) {
        this.roomGroupService = roomGroupService;
        this.roomService = roomService;
    }

    /**
     * Adds a new room to a room group identified by the given ID.
     *
     * @param id The ID of the room group to add the room to.
     * @param creationData The data of the room to be created.
     * @return The newly created room.
     */
    @PostMapping("roomGroups/{id}/rooms")
    @Secured(SesamUser.ROLE_EDITOR)
    @Transactional
    public Room addRoom(@PathVariable("id") final Long id, @RequestBody RoomCreationData creationData) {
        log.debug("Adding room to room group with ID: {}", id);
        final var group = roomGroupService.findRoomGroupById(id).orElseThrow(NotFoundException::new);
        final var room = roomService.addRoom(creationData);
        final var rooms = group.getRooms();
        rooms.add(room);
        group.setRooms(rooms);
        room.setRoomGroup(group);
        log.debug("Room {} successfully added to the room group with id: {}", room.getName(), id);
        return room;
    }

    /**
     * Retrieves the list of rooms for a given room group ID.
     *
     * @param id The ID of the room group.
     * @return The list of rooms associated with the room group.
     * @throws NotFoundException if the room group with the provided ID does not exist.
     */
    @GetMapping("roomGroups/{id}/rooms")
    @JsonView(Views.Internal.class)
    public Collection<Room> getRooms(@PathVariable("id") final Long id) {
        log.debug("Retrieving rooms from room group with id: {}", id);
        return roomGroupService.findRoomGroupById(id).orElseThrow(NotFoundException::new).getRooms();
    }

    /**
     * Removes a group with the specified ID.
     *
     * @param groupId the ID of the group to be removed
     * @return a message indicating that the group has been removed
     */
    @DeleteMapping("roomGroups/{id}")
    @Secured(SesamUser.ROLE_EDITOR)
    @Transactional
    public String removeGroup(@PathVariable("id") final Long groupId) {
        log.debug("Removing room group with id: {}", groupId);
        roomGroupService.deleteRoomGroup(groupId);
        log.debug("Room group with id: {} successfully removed", groupId);
        return String.format("Removed group with id %s", groupId);
    }

    /**
     * Updates a room group with the given information.
     *
     * @param groupId   The ID of the room group to be updated.
     * @param editData  The updated information for the room group.
     * @return The updated room group after the changes have been applied.
     */
    @PutMapping("roomGroups/{id}")
    @Secured(SesamUser.ROLE_EDITOR)
    @Transactional
    public RoomGroup updateGroup(@PathVariable("id") final Long groupId, @RequestBody RoomGroupInfo editData) {
        final var group = roomGroupService.findRoomGroupById(groupId).orElseThrow(NotFoundException::new);
        log.debug("Updating room group with id: {}", groupId);
        Optional.ofNullable(editData.getName()).ifPresent(group::setName);
        Optional.ofNullable(editData.getColor()).ifPresent(group::setColor);

        log.debug("Room group {} with id: {} successfully updated", group.getName(), groupId);
        return group;
    }

}
