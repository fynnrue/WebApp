package com.gpse.basis.web;

import com.fasterxml.jackson.annotation.JsonView;
import com.gpse.basis.domain.*;
import com.gpse.basis.domain.service.BuildingService;
import com.gpse.basis.domain.service.FloorService;
import com.gpse.basis.domain.service.ImageService;
import com.gpse.basis.domain.service.RoomGroupService;
import com.gpse.basis.web.cmd.RoomGroupCreation;
import com.gpse.basis.web.exceptions.BadRequestException;
import com.gpse.basis.web.exceptions.NotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Responsible for handling HTTP requests related to floors.
 * It provides methods for retrieving, storing, updating, and deleting floors.
 */

@RestController
@CrossOrigin
@RequestMapping("/api")
public class FloorController {

    @Value("${app.max-image-size}")
    private long MAX_IMAGE_SIZE; // 10 MiB by default

    private final FloorService floorService;
    private final ImageService imageService;
    private final BuildingService buildingService;
    private final RoomGroupService roomGroupService;

    private static final Logger log = LoggerFactory.getLogger(FloorController.class);

    @Autowired
    public FloorController(
            final FloorService floorService,
            final ImageService imageService,
            final RoomGroupService roomGroupService,
            final BuildingService buildingService) {
        this.roomGroupService = roomGroupService;
        this.floorService = floorService;
        this.imageService = imageService;
        this.buildingService = buildingService;
    }

    /**
     * Retrieves the list of floors.
     *
     * @return the list of floor objects
     */
    @GetMapping("/floors")
    @ResponseStatus(code = HttpStatus.OK)
    public List<Floor> getFloors() {
        log.debug("Request all floors");
        return floorService.getFloors();
    }

    /**
     * Retrieves a floor by its ID.
     *
     * @param id The ID of the floor to retrieve.
     * @return An optional containing the floor with the given ID, if it exists.
     */
    @GetMapping("/floors/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    @JsonView(Views.Public.class)
    public Optional<Floor> getFloorById(@PathVariable("id") final Long id) {
        log.debug("Request floor by ID: {}", id);
        return floorService.getFloorById(id);
    }

    /**
     * Stores a new floor in a building.
     *
     * @param id The ID of the building where the floor will be added.
     * @param name The name of the floor.
     * @param image The image file of the floor.
     * @return The created floor object.
     * @throws SQLException If an error occurs during the database operation.
     * @throws BadRequestException If the size of the image exceeds the maximum allowed size.
     * @throws NotFoundException If the building with the specified ID is not found.
     */
    @PostMapping("/buildings/{id}/floors")
    @ResponseStatus(code = HttpStatus.CREATED)
    @Transactional
    @Secured("ROLE_EDITOR")
    public Floor store(@PathVariable("id") final Long id, @RequestPart final String name, @RequestPart final MultipartFile image) throws SQLException {
        if (image.getSize() > MAX_IMAGE_SIZE) {
            throw new BadRequestException();
        }
        try {
            final Image im = imageService.addImage(image);
            final var floor = floorService.addFloor(name, im.getId());
            final var building = buildingService.getBuildingById(id).orElseThrow(NotFoundException::new);
            building.getFloors().add(floor);
            floor.setBuilding(building);
            log.debug("New floor is saved: {}", name);
            log.info("Floor successfully added: {}", name);
            return floor;
        } catch (IOException e) {
            log.error("Error adding the floor: {}", e.getMessage());
            throw new BadRequestException();
        }
    }

    /**
     * Updates the name and image of a floor for a given ID.
     * Access to this method is restricted to users with the "ROLE_EDITOR" role.
     *
     * @param name The new name of the floor.
     * @param image The new image file of the floor.
     * @param idd The ID of the floor to be updated.
     * @return The updated floor object.
     * @throws SQLException If there is an error communicating with the database.
     * @throws BadRequestException If there is an error adding the image file.
     */
    @PutMapping("/floors/{id}/nameImage")
    @Secured("ROLE_EDITOR")
    public Floor update(
            @RequestPart final String name,
            @RequestPart final MultipartFile image,
            @PathVariable("id") final String idd
    ) throws SQLException {
        final Image im;
        try {
            im = imageService.addImage(image);
            log.info("Floor successfully updated. ID: {}, Name: {}", idd, name);
        } catch (IOException e) {
            log.error("Error while updating the floor: {}", e.getMessage());
            throw new BadRequestException();
        }
        log.debug("Update floor with ID: {}", idd);
        return floorService.updateFloor(idd, name, im.getId());
    }

    /**
     * Updates the image of a floor.
     *
     * @param id    the ID of the floor to update the image for
     * @param image the new image to be assigned to the floor
     * @return the updated floor
     * @throws SQLException        if there is a database error
     * @throws BadRequestException if there is an error with the request or the image cannot be processed
     */
    @PutMapping("/floors/{id}/image")
    @Secured("ROLE_EDITOR")
    public Floor updateImage(@PathVariable("id") final String id,
                             @RequestPart final MultipartFile image) throws SQLException {
        final Image im;
        try {
            im = imageService.addImage(image);
            log.info("Floor image successfully updated. ID: {}, Name: {}", id, image);
        } catch (IOException e) {
            log.error("Error while updating the floor image: {}", e.getMessage());
            throw new BadRequestException();
        }
        log.debug("Update floor image with ID: {}", id);
        return floorService.updateImage(id, im.getId());
    }

    /**
     * Updates the name of a floor.
     *
     * @param id   The ID of the floor to update the name for.
     * @param name The new name for the floor.
     * @return The updated floor object.
     */
    @PutMapping("/floors/{id}/name")
    @Secured("ROLE_EDITOR")
    public Floor updateName(@PathVariable("id") final String id,
                            @RequestPart final String name) {
        log.debug("Update name for floor with ID: {}", id);
        return floorService.updateName(id, name);
    }

    /**
     * Retrieves the room groups by building for a given floor ID.
     *
     * @param floorId the ID of the floor to retrieve room groups from
     * @return a collection of room groups belonging to the specified floor
     * @throws NotFoundException if the floor with the specified ID is not found
     */
    @GetMapping("/floors/{fId}/groups")
    Collection<RoomGroup> roomsByBuilding(@PathVariable("fId") final Long floorId) {
        log.debug("Retrieve room groups for floor with ID: {}", floorId);
        return floorService.getFloorById(floorId).orElseThrow(NotFoundException::new).getRoomGroups();
    }

    /**
     * Adds a new room group to a specific floor.
     *
     * @param floorId       the ID of the floor where the room group will be added
     * @param roomCreation  the room group creation details
     * @return the newly added room group
     * @throws NotFoundException if the floor with the given ID is not found
     */
    @PostMapping("/floors/{fId}/roomGroups")
    @Secured(SesamUser.ROLE_EDITOR)
    @Transactional
    public RoomGroup addRoomGroup(@PathVariable("fId") final Long floorId, @RequestBody final RoomGroupCreation roomCreation) {
        log.debug("Adding a room group to the floor with ID: {}", floorId);
        final var floor = floorService.getFloorById(floorId).orElseThrow(NotFoundException::new);
        final var group = roomGroupService.addRoomGroup(roomCreation);
        final var floorRoomGroups = floor.getRoomGroups();
        floorRoomGroups.add(group);
        floor.setRoomGroups(floorRoomGroups);
        group.setFloor(floor);
        return group;
    }

    /**
     * Retrieves a collection of rooms by floor ID.
     *
     * @param floorId the ID of the floor
     * @return a collection of rooms belonging to the specified floor
     */
    @GetMapping("/floors/{fId}/rooms")
    Collection<Room> roomsByFloor(@PathVariable("fId") final Long floorId) {
        log.debug("Request rooms from floor with ID: {}", floorId);
        return floorService.getRoomsOfFloor(floorId);
    }

    /**
     * Retrieves the collection of room groups associated with a given floor.
     *
     * @param floorId The ID of the floor.
     * @return The collection of room groups associated with the given floor.
     */
    @JsonView(Views.Public.class)
    @GetMapping("/floors/{fId}/roomGroups")
    Collection<RoomGroup> roomGroupsByFloor(@PathVariable("fId") final Long floorId) {
        log.debug("Request rooms groups from floor with ID: {}", floorId);
        return floorService.getRoomGroupsOfFloor(floorId);
    }

    /**
     * Retrieves the collection of room groups with rooms for a given floor.
     *
     * @param floorId the ID of the floor
     * @return the collection of room groups with rooms for the specified floor
     */
    @JsonView(Views.Internal.class)
    @GetMapping("/floors/{fId}/roomGroupsWithRooms")
    Collection<RoomGroup> roomGroupsWithRoomsByFloor(@PathVariable("fId") final Long floorId) {
        log.debug("Retrieve the room groups with rooms for floor with ID: {}", floorId);
        return floorService.getRoomGroupsOfFloor(floorId);
    }

    /**
     * Deletes a floor.
     *
     * @param id the ID of the floor to delete
     * @return "valid" if the floor was successfully deleted, "invalid" if the floor couldn't be deleted
     * @throws NotFoundException if the floor with the given ID doesn't exist
     */
    @DeleteMapping("/deleteFloors/{id}")
    @Secured("ROLE_ISSUER")
    public String deleteFloor(@PathVariable Long id) {
        Floor floor = floorService.getFloorById(id).orElseThrow(NotFoundException::new);
        log.debug("Request to delete the floor with ID: {}", id);
        boolean deleteFloor = floorService.deleteFloor(floor);
        if (deleteFloor) {
            log.info("Floor successfully deleted. ID: {}", id);
            return "valid";
        } else {
            log.warn("Error while deleting the floor. ID: {}", id);
            return "invalid";
        }
    }
}
