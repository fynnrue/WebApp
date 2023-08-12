package com.gpse.basis.web;

import com.gpse.basis.domain.*;
import com.gpse.basis.domain.credentials.Credential;
import com.gpse.basis.domain.credentials.CredentialGroupUnion;
import com.gpse.basis.domain.credentials.CredentialORConnection;
import com.gpse.basis.domain.service.*;
import com.gpse.basis.web.cmd.ORConnectionCmd;
import com.gpse.basis.web.cmd.RoomEditData;
import com.gpse.basis.web.exceptions.NotFoundException;
import jakarta.transaction.Transactional;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is a controller for managing rooms
 */

@RestController
@CrossOrigin
@RequestMapping("/api")
public class RoomController {

    final RoomService roomService;

    final RoomGroupService roomGroupService;

    final BuildingService buildingService;
    final CredentialService credentialService;
    final CredentialORConnectionService orConnectionService;
    final CredentialGroupService credentialGroupService;
    private static final Logger log = LoggerFactory.getLogger(BuildingController.class);

    @Autowired
    public RoomController(
            final RoomService roomService,
            final RoomGroupService roomGroupService,
            final BuildingService buildingService,
            final CredentialService credentialService,
            final CredentialORConnectionService orConnectionService,
            final CredentialGroupService credentialGroupService) {
        this.roomService = roomService;
        this.roomGroupService = roomGroupService;
        this.buildingService = buildingService;
        this.credentialService = credentialService;
        this.orConnectionService = orConnectionService;
        this.credentialGroupService = credentialGroupService;
    }


    /**
     * Updates a room in the system based on its ID.
     * Requires the user to have the role ROLE_EDITOR.
     *
     * @param id              The ID of the room to be updated.
     * @param updatedRoom     The updated room data.
     * @return The updated room.
     */
    @PutMapping("/rooms/{id}")
    @Secured(SesamUser.ROLE_EDITOR)
    @Transactional
    public Room updateRoom(@PathVariable("id") final Long id, @RequestBody final RoomEditData updatedRoom) {
        log.debug("Update room with ID: {}", id);
        final var room = roomService.getRoomById(id).orElseThrow(NotFoundException::new);

        // All fields are optional and can be null
        Optional.ofNullable(updatedRoom.getDescription()).ifPresent(room::setDescription);
        Optional.ofNullable(updatedRoom.getName()).ifPresent(room::setName);
        Optional.ofNullable(updatedRoom.getVertices()).ifPresent(room::setVertices);

        final Long groupId;
        if ((groupId = updatedRoom.getRoomGroupId()) != null) {
            log.info("Update room group of the room with ID: {}", room.getId());
            roomGroupService.updateRoomGroup(room.getId(), groupId);
        }

        final List<ORConnectionCmd> requiredCredentials;
        if ((requiredCredentials = updatedRoom.getRequiredCredentials()) != null) {
            log.info("Update required credentials of the room with ID:{}", room.getId());
            final var orConnections = new ArrayList<CredentialORConnection>();
            for (var cId : requiredCredentials) {
                final var credentials = cId.getCredentials();
                final var connection = new CredentialORConnection(
                        credentials.stream().filter((c) -> !c.getGroup()).map(
                                c -> credentialService.getCredentialById(c.getId()).orElseThrow(NotFoundException::new)
                        ).collect(Collectors.toList()),
                        credentials.stream().filter(CredentialGroupUnion::getGroup).map(
                                c -> credentialGroupService.getCredentialGroupById(c.getId()).orElseThrow(NotFoundException::new)
                        ).collect(Collectors.toList()),
                        cId.getPredicateRequirements(),
                        null
                );
                Optional.ofNullable(cId.getAttributeRequirement()).ifPresent(c -> connection.setAttributeRequirements(c.toAttributeRequirement()));

                if (!orConnectionService.validate(connection)) throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid ORConnection"
                );

                orConnectionService.save(connection);
                orConnections.add(
                        connection
                );
            }
            for (var connection : room.getOrConnections()) {
                orConnectionService.deleteById(connection.getId());
            }
            room.setOrConnections(orConnections);
            roomService.applyCredentialConfig(room);
        }

        if (updatedRoom.getDoors() != null) {
            log.debug("Update doors of the room with ID:{}", room.getId());
            room.setDoors(updatedRoom.getDoors());
            roomService.applyCredentialConfig(room);
        }
        return room;
    }

    /**
     * Deletes a room from the system based on its ID.
     *
     * @param id The ID of the room to be deleted.
     * @return A message confirming the successful removal of the room.
     */
    @DeleteMapping("/rooms/{id}")
    @Secured(SesamUser.ROLE_EDITOR)
    public String deleteRoom(@PathVariable("id") final Long id) {
        log.debug("Request delete room with ID: {}", id);
        roomService.removeRoom(id);
        log.debug("Room was deleted");
        return "Removed room " + id;
    }

    /**
     * Returns a specific room by its ID.
     *
     * @param id The ID of the room.
     * @return The room with the specified ID.
     * @throws NotFoundException if the room with the specified ID does not exist.
     */
    @GetMapping("/rooms/{id}")
    public Room getRoom(@PathVariable("id") final Long id) {
        log.debug("Request room with ID: {}", id);
        return roomService.getRoomById(id).orElseThrow(NotFoundException::new);
    }

    /**
     * Returns a list of all rooms.
     *
     * @return List of all rooms.
     */
    @GetMapping("/rooms")
    public List<Room> getRooms() {
        log.debug("Request all rooms");
        return (List<Room>) roomService.getAllRooms();
    }

    /**
     * Returns a list of all rooms with their required credentials.
     * Used in the frontend to display the rooms in the list view.
     *
     * @return List of rooms with required credentials, building, group and door_amount as JSON.
     */
    @GetMapping(value = "/rooms/plan", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getRoomsPlan() {
        log.debug("Request rooms plan");
        List<Building> buildings = this.buildingService.getBuildings();

        List<JSONObject> roomPlan = new ArrayList<>();

        for( Building building : buildings ) {
            List<Floor> floors = building.getFloors();

            for(Floor floor : floors) {
                List<RoomGroup> groups = (List<RoomGroup>) floor.getRoomGroups();

                for (RoomGroup group : groups) {
                    List<Room> rooms = (List<Room>) group.getRooms();

                    for(Room room : rooms){

                        JSONObject roomJson = new JSONObject();
                        roomJson.put("buildingId", building.getId());
                        roomJson.put("floorId", floor.getId());
                        roomJson.put("roomId", room.getId());
                        roomJson.put("building", building.getName());
                        roomJson.put("group", group.getName());
                        roomJson.put("name", room.getName());
                        roomJson.put("floor", floor.getName());

                        final List<List<String>> credentialNames = new ArrayList<>();
                        for(CredentialORConnection orConnection : room.getOrConnections()){
                            List<String> credentialNamesList = new ArrayList<>();
                            for(Credential credential : orConnection.getCredentials()){
                                credentialNamesList.add(credential.getName());
                            }
                            credentialNames.add(credentialNamesList);
                        }

                        roomJson.put("requiredCredentials", credentialNames);
                        roomJson.put("doors", room.getDoors().size());
                        roomPlan.add(roomJson);
                    }

                }

            }
        }
        return roomPlan.toString();
    }

    /**
     * Checks if a person can enter a specific room based on the provided credentials.
     *
     * @param id The ID of the room to check.
     * @param request The WebRequest object containing the request parameters.
     * @return "success" if the person can enter the room, "fail" otherwise.
     */
    @PostMapping("/rooms/{roomId}/checkValid")
    public String canEnterRoom(@PathVariable("roomId") final Long id, final WebRequest request) {
        log.debug("Check access to the room with ID: {}", id);
        Optional<Room> room = roomService.getRoomById(id);
        final String credentialIDs = request.getParameter("credentials");
        if (credentialIDs == null || credentialIDs.isEmpty()) return "fail";
        List <Credential> givenCredentials = new CredentialGroupController(credentialGroupService, credentialService).getCredentialsFromString(credentialIDs);
        if(room.isPresent() && givenCredentials != null) {
            List<CredentialORConnection> con = room.get().getOrConnections();
            if(con.isEmpty()) {
                return "success";
            }
            for(CredentialORConnection orCon : con) {
                Collection<Credential> credentials = orCon.getCredentials();
                if(new HashSet<>(givenCredentials).containsAll(credentials)) {
                    return "success";
                }
            }
        }
        return "fail";
    }

}