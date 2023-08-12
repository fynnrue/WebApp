package com.gpse.basis.domain.service;

import com.gpse.basis.domain.RoomGroup;
import com.gpse.basis.domain.repository.RoomGroupRepository;
import com.gpse.basis.domain.repository.RoomRepository;
import com.gpse.basis.web.cmd.RoomGroupCreation;
import com.gpse.basis.web.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoomGroupServiceImpl implements RoomGroupService {

    private final RoomGroupRepository roomGroupRepository;
    private final RoomRepository roomRepository;

    @Autowired
    public RoomGroupServiceImpl(final RoomGroupRepository roomGroupRepository, final RoomRepository roomRepository) {
        this.roomGroupRepository = roomGroupRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    public RoomGroup addRoomGroup(RoomGroupCreation data) {
        return roomGroupRepository.save(new RoomGroup(
                data.getName(),
                data.getColor(),
                List.of()
        ));
    }

    @Override
    public Iterable<RoomGroup> getAllRoomGroups() {
        return roomGroupRepository.findAll();
    }

    @Override
    public Optional<RoomGroup> findRoomGroupById(Long id) {
        return roomGroupRepository.findById(id);
    }

    @Override
    public void deleteRoomGroup(Long id) {
        roomGroupRepository.deleteById(id);
    }

    @Override
    public void updateRoomGroup(Long roomId, Long groupId) {
        final var room = roomRepository.findById(roomId).orElseThrow(NotFoundException::new);
        final var group = roomGroupRepository.findById(groupId).orElseThrow(NotFoundException::new);

        room.getRoomGroup().getRooms().remove(room);
        // oldGroup.getRooms().remove(room);
        room.setRoomGroup(group);

        group.getRooms().add(room);
        roomRepository.save(room);
        // roomGroupRepository.save(oldGroup);
        roomGroupRepository.save(group);
    }
}
