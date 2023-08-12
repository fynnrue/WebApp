package com.gpse.basis.web;

import com.fasterxml.jackson.annotation.JsonView;
import com.gpse.basis.domain.Building;
import com.gpse.basis.domain.Floor;
import com.gpse.basis.domain.Image;
import com.gpse.basis.domain.Views;
import com.gpse.basis.domain.service.BuildingService;
import com.gpse.basis.domain.service.ImageService;
import com.gpse.basis.web.exceptions.BadRequestException;
import com.gpse.basis.web.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * The BuildingController class handles requests related to buildings in the API.
 * It provides endpoints for retrieving, adding, updating, and deleting buildings.
 */

@RestController
@CrossOrigin
@RequestMapping("/api")
public class BuildingController {

    /**
     * Maximum allowable size for an image file.
     * The size is specified in bytes.
     */
    @Value("${app.max-image-size}")
    public long MAX_IMAGE_SIZE; // 10 MiB by default
    private final BuildingService buildingService;
    private final ImageService imageService;
    private static final Logger log = LoggerFactory.getLogger(BuildingController.class);

    /**
     * Constructs a new instance of BuildingController.
     *
     * @param buildingService The BuildingService used for interacting with building data.
     * @param imageService The ImageService used for interacting with image data.
     */
    @Autowired
    public BuildingController(
            final BuildingService buildingService,
            final ImageService imageService
    ) {
        this.buildingService = buildingService;
        this.imageService = imageService;
    }

    /**
     * Retrieves a list of buildings.
     *
     * @return The list of buildings.
     */
    @GetMapping("/buildings")
    @ResponseStatus(code = HttpStatus.OK)
    public List<Building> getBuildings() {
        log.debug("Request all buildings");
        return buildingService.getBuildings();
    }

    /**
     * Retrieves a building by its ID.
     *
     * @param id The ID of the building to retrieve.
     * @return A ResponseEntity object containing either the retrieved Building object with a 200 OK status,
     *         or HttpStatus.NOT_FOUND if the building does not exist.
     */
    @GetMapping("/buildings/{id}")
    public ResponseEntity<Building> getBuildingById(@PathVariable("id") Long id) {
        Optional<Building> building = buildingService.getBuildingById(id);
        log.debug("Request bulilding by ID: {}", id);
        return building.map(
                value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Stores a new building with the given name and image.
     *
     * @param name The name of the building to be stored.
     * @param image The image file of the building to be stored.
     * @return The created Building object.
     * @throws SQLException if there is an error with the database operation.
     * @throws BadRequestException if the size of the image exceeds the maximum allowed size.
     */
    @PostMapping("/buildings")
    @ResponseStatus(code = HttpStatus.CREATED)
    @Secured("ROLE_EDITOR")
    public Building store(@RequestPart final String name, @RequestPart final MultipartFile image) throws SQLException {
        if (image.getSize() > MAX_IMAGE_SIZE) {
            throw new BadRequestException();
        }
        try {
            final Image im = imageService.addImage(image);
            log.debug("New building is saved: {}", name);
            log.info("Building successfully added: {}", name);
            return buildingService.addBuilding(new Building(name, im.getId()));
        } catch (IOException e) {
            log.error("Error adding the building: {}", e.getMessage());
            throw new BadRequestException();
        }
    }

    /**
     * Updates a building with the specified ID.
     *
     * @param id The ID of the building to update.
     * @param name The new name of the building.
     * @param image The new image for the building.
     * @return The updated building.
     * @throws SQLException If an error occurs while updating the building in the database.
     * @throws BadRequestException If an error occurs while adding the image or if the request is malformed.
     */
    @PutMapping("/buildings/{id}")
    @Secured("ROLE_EDITOR")
    public Building update(
            @PathVariable("id") final String id,
            @RequestPart final String name,
            @RequestPart final MultipartFile image
    ) throws SQLException {
        final Image im;
        try {
            im = imageService.addImage(image);
            log.info("Building successfully updated. ID: {}, Name: {}", id, name);
        } catch (IOException e) {
            log.error("Error while updating the building: {}", e.getMessage());
            throw new BadRequestException();
        }
        log.debug("Update building with ID: {}", id);
        return buildingService.updateBuilding(id, name, im.getId());
    }

    /**
     * Retrieves the collection of floors for the specified building ID.
     *
     * @param id the ID of the building
     * @return the collection of floors for the specified building ID
     * @throws NotFoundException if the building with the specified ID is not found
     */
    @GetMapping("/buildings/{id}/floors")
    @JsonView(Views.Public.class)
    public Collection<Floor> getFloors(@PathVariable("id") final Long id) {
        log.debug("Request get all floors for building with ID: {}", id);
        return buildingService.getBuildingById(id).orElseThrow(NotFoundException::new).getFloors();
    }

    /**
     * Deletes a building with the given ID.
     *
     * @param id The ID of the building to delete.
     * @return A string indicating the result of the deletion. It will be "valid" if the deletion is successful, and "invalid" otherwise.
     * @throws NotFoundException If a building with the given ID is not found.
     */
    @DeleteMapping("/deleteBuildings/{id}")
    @Secured("ROLE_ISSUER")
    public String deleteBuilding(@PathVariable Long id) {
        Building building = buildingService.getBuildingById(id).orElseThrow(NotFoundException::new);
        log.debug("Request to delete the building with ID: {}", id);
        boolean status = buildingService.deleteBuilding(building);
        if (status) {
            log.info("Building successfully deleted. ID: {}", id);
            return "valid";
        } else {
            log.warn("Error while deleting the building. ID: {}", id);
            return "invalid";
        }
    }
}
