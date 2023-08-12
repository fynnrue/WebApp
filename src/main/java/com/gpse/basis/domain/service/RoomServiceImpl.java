package com.gpse.basis.domain.service;

import com.gpse.basis.domain.Room;
import com.gpse.basis.domain.Utils;
import com.gpse.basis.domain.credentials.AttributeRelation;
import com.gpse.basis.domain.credentials.PredicateRequirement;
import com.gpse.basis.domain.repository.RoomGroupRepository;
import com.gpse.basis.domain.repository.RoomRepository;
import com.gpse.basis.web.cmd.RoomCreationData;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class RoomServiceImpl implements RoomService {

    private final Environment environment;

    private final RoomRepository roomRepository;
    private final RoomGroupRepository roomGroupRepository;

    @Autowired
    public RoomServiceImpl(final RoomRepository roomRepository,
                           final RoomGroupRepository roomGroupRepository, Environment environment) {
        this.roomRepository = roomRepository;
        this.roomGroupRepository = roomGroupRepository;
        this.environment = environment;
    }

    @Override
    public Room addRoom(RoomCreationData creationData) {
        return roomRepository.save(
                new Room(
                        creationData.getName(),
                        creationData.getDescription(),
                        creationData.getVertices(),
                        List.of(),
                        List.of()
                ));
    }

    @Override
    public void removeRoom(Long id) {
        // Delete room from all the roomGroups first
        final var room = roomRepository.findById(id).orElseThrow();
        for (var roomGroup : roomGroupRepository.findAll()) {
            roomGroup.getRooms().remove(room);
            roomGroupRepository.save(roomGroup);
        }
        roomRepository.deleteById(id);
    }

    @Override
    public Iterable<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @Override
    public void applyCredentialConfig(Room room) {
        final var restTemplate = new RestTemplate();
        final var jsonObject = new JSONObject();
        jsonObject.put("description", room.getDescription());
        final var predicates = new JSONObject();
        final var attributes = new JSONObject();

        var i = 0;
        for (var or : room.getOrConnections()) {
            if (or.getRequirements() == null || or.getRequirements().isEmpty()) {
                // OR connection specifies requested attributes
                final var proofAttributeInfo = new JSONObject();
                final var restrictions = new JSONArray();

                final var attributeRequirements = or.getAttributeRequirements();

                if (attributeRequirements == null) continue;

                for (var value : attributeRequirements.getValues()) {
                    final var restriction = new JSONObject();
                    final var attributeValue = new JSONObject();
                    attributeValue.put("name", attributeRequirements.getAttributeName());
                    attributeValue.put("value", value.getValue());

                    restriction.put("attributeValue", attributeValue);

                    restrictions.put(restriction);
                }
                proofAttributeInfo.put("name", attributeRequirements.getAttributeName());
                proofAttributeInfo.put("restrictions", restrictions);
                attributes.put(String.format("attribute_%s", i), proofAttributeInfo);
            } else {
                // OR connection specifies requested predicates

                final var restrictions = new JSONArray();
                for (var c : or.getAllCredentials().stream().map(
                        (c) -> new JSONObject().put("credentialDefinitionId", c.getCredentialDid())
                ).toList()) {
                    restrictions.put(c);
                }

                // Replace occurrences of EQUAL with AND connection of GREATER_EQUAL and LOWER_EQUAL
                final var requirements = or.getRequirements().stream().flatMap((r) -> {
                    if (r.getRelation() == AttributeRelation.EQUAL) {
                        return Stream.of(
                                new PredicateRequirement(
                                        r.getAttributeName(),
                                        AttributeRelation.GREATER_EQUAL,
                                        r.getAttributeValue(),
                                        r.getValueType()
                                ),
                                new PredicateRequirement(
                                        r.getAttributeName(),
                                        AttributeRelation.LOWER_EQUAL,
                                        r.getAttributeValue(),
                                        r.getValueType()
                                )
                        );
                    } else {
                        return Stream.of(r);
                    }
                }).toList();

                for (var requirement : requirements) {
                    final var predicate = new JSONObject();
                    final var value = switch (requirement.getValueType()) {
                        case DATE -> Integer.parseInt(requirement.getAttributeValue().replace("-", ""));
                        case NUMBER -> Integer.parseInt(requirement.getAttributeValue());
                        case TODAY -> "$TODAY-YYYYMMDD";
                    };
                    predicate.put("name", requirement.getAttributeName());
                    predicate.put("predicateValue", value);
                    predicate.put("predicateType", requirement.getRelation().symbol);
                    predicate.put("restrictions", restrictions);
                    predicates.put(String.format("predicate_%s", i), predicate);
                    i++;
                }
            }
            i++;
        }


        jsonObject.put("requestedPredicates", predicates);
        jsonObject.put("requestedAttributes", attributes);

        final var uri = environment.getProperty("issue-API.uri");
        final var username = environment.getProperty("issue-API.username");
        final var password = environment.getProperty("issue-API.password");

        final var headers = Utils.issueAPIHeaders(username, password);
        headers.setContentType(MediaType.APPLICATION_JSON);

        final var requestEntity = new HttpEntity<>(jsonObject.toString(), headers);

        for (var door : room.getDoors()) {
            restTemplate.exchange(uri + "/api/proof/config/" + door, HttpMethod.POST, requestEntity, String.class);
        }

    }

    @Override
    public Optional<Room> getRoomById(Long id) {
        return roomRepository.findById(id);
    }
}
