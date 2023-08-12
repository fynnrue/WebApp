package com.gpse.basis.domain.service;

import com.gpse.basis.domain.Building;
import com.gpse.basis.domain.Floor;
import com.gpse.basis.domain.Room;
import com.gpse.basis.domain.RoomGroup;
import com.gpse.basis.domain.repository.BuildingRepository;
import com.gpse.basis.domain.repository.FloorRepository;
import com.gpse.basis.domain.repository.RoomGroupRepository;
import com.gpse.basis.web.cmd.RoomGroupCreation;
import com.gpse.basis.web.exceptions.BadRequestException;
import com.gpse.basis.web.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/*
 * This class is responsible for the floor management.
 */
@Service
public class FloorServiceImpl implements FloorService {

    private static final Integer DEFAULT_COLOR = 0xa2fc6a;

    private final FloorRepository floorRepository;
    private final RoomGroupService roomGroupService;

    private final BuildingRepository buildingRepository;

    private final RoomGroupRepository roomGroupRepository;

    @Autowired
    public FloorServiceImpl(
            final FloorRepository floorRepository,
            final RoomGroupService roomGroupService,
            final BuildingRepository buildingRepository,
            final RoomGroupRepository roomGroupRepository) {
        this.floorRepository = floorRepository;
        this.roomGroupRepository = roomGroupRepository;
        this.roomGroupService = roomGroupService;
        this.buildingRepository = buildingRepository;
    }

    @Override
    public List<Floor> getFloors() {
        final List<Floor> floors = new ArrayList<>();
        floorRepository.findAll().forEach(floors::add);
        return floors;
    }

    @Override
    public Optional<Floor> getFloorById(final Long id) {
        return floorRepository.findById(id);
    }

    @Override
    public Floor addFloor(final String name, final Long image) {
        final var defaultGroup = roomGroupService.addRoomGroup(new RoomGroupCreation("Default", DEFAULT_COLOR));
        final Floor floor = new Floor(name, image, defaultGroup);
        floorRepository.save(floor);
        defaultGroup.setFloor(floor);
        roomGroupRepository.save(defaultGroup);
        return floor;
    }


    @Override
    public Floor updateFloor(final String id, final String name, final Long image) {
        final Long arg = Long.valueOf(id);
        final Floor floor = floorRepository.findById(arg).orElseThrow(BadRequestException::new);
        floor.setName(name);
        floor.setImageId(image);

        return floorRepository.save(floor);
    }

    @Override
    public Floor updateImage(final String id, final Long image) {
        final Long arg = Long.valueOf(id);
        final Floor floor = floorRepository.findById(arg).orElseThrow(BadRequestException::new);
        floor.setImageId(image);

        return floorRepository.save(floor);
    }

    @Override
    public Floor updateName(final String id, final String name) {
        final Long arg = Long.valueOf(id);
        final Floor floor = floorRepository.findById(arg).orElseThrow(BadRequestException::new);
        floor.setName(name);

        return floorRepository.save(floor);
    }

    /**
     * Get all the rooms of all the groups of one floor.
     *
     * @param id The id of the Floor
     * @return A list of Rooms
     */
    @Override
    public List<Room> getRoomsOfFloor(final Long id) {
        final Floor floor = getFloorById(id).orElseThrow(BadRequestException::new);
        List<RoomGroup> allRoomGroups = floor.getRoomGroups().stream().toList();

        // Collect all Rooms from all RoomGroups
        List<Room> allRooms = new ArrayList<>();
        for (RoomGroup roomGroup : allRoomGroups) {
            allRooms.addAll(roomGroup.getRooms());
        }
        return allRooms;
    }

    /**
     * Get all RoomGroups of one Floor.
     *
     * @param id The id of the Floor
     * @return A list of RoomGroups
     */
    @Override
    public List<RoomGroup> getRoomGroupsOfFloor(final Long id) {
        final Floor floor = floorRepository.findById(id).orElseThrow(NotFoundException::new);
        return floor.getRoomGroups().stream().toList();
    }

    @Override
    public boolean deleteFloor(final Floor floor) {
        boolean exists = true;
        if (floor.getId() != null) {
            Building building = floor.getBuilding();
            building.getFloors().remove(floor);
            buildingRepository.save(building);
            floorRepository.delete(floor);
        } else {
            exists = false;
        }
        return exists;
    }
}
