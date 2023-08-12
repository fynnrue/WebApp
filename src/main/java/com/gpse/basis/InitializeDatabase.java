package com.gpse.basis;

import com.gpse.basis.domain.*;
import com.gpse.basis.domain.credentials.Credential;
import com.gpse.basis.domain.repository.*;
import com.gpse.basis.domain.service.*;
import com.gpse.basis.web.cmd.RoomCreationData;
import com.gpse.basis.web.exceptions.NotFoundException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.util.Streamable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class InitializeDatabase implements InitializingBean {
    private final BuildingService buildingService;
    private final UserService userService;
    private final ImageService imageService;
    private final FloorService floorService;

    private final CredentialService credentialService;
    private final CredentialGroupService credentialGroupService;

    private final RoomService roomService;

    private final RoomGroupService roomGroupService;

    private final WebsiteConfigurationService websiteConfigurationService;

    private final RoomGroupRepository roomGroupRepository;
    private final FloorRepository floorRepository;
    private final RoomRepository roomRepository;
    private final BuildingRepository buildingRepository;

    @Autowired
    private ResourceLoader resourceLoader;

    private final WebsiteConfigurationRepository websiteConfigurationRepository;

    @Autowired
    public InitializeDatabase(
            final BuildingService buildingService,
            final UserService userService,
            final ImageService imageService,
            final CredentialService credentialService,
            final FloorService floorService,
            final RoomService roomService,
            final RoomGroupService roomGroupService,
            final WebsiteConfigurationService websiteConfigurationService,
            final RoomGroupRepository roomGroupRepository,
            final FloorRepository floorRepository,
            final RoomRepository roomRepository,
            final WebsiteConfigurationRepository websiteConfigurationRepository,
            final BuildingRepository buildingRepository,
            final CredentialGroupService credentialGroupService) {
        this.roomRepository = roomRepository;
        this.buildingService = buildingService;
        this.userService = userService;
        this.imageService = imageService;
        this.floorService = floorService;
        this.credentialService = credentialService;
        this.roomService = roomService;
        this.roomGroupService = roomGroupService;
        this.websiteConfigurationService = websiteConfigurationService;
        this.roomGroupRepository = roomGroupRepository;
        this.floorRepository = floorRepository;
        this.websiteConfigurationRepository = websiteConfigurationRepository;
        this.buildingRepository = buildingRepository;
        this.credentialGroupService = credentialGroupService;
    }

    private void initUsers() {
        try {
            userService.loadUserByUsername("admin@mail.com");
            userService.loadUserByUsername("jana@mail.com");
        } catch (UsernameNotFoundException exc) {
            final String adminEmail = "admin@mail.com";
            final String adminPassword = "{bcrypt}$2a$10$WoG5Z4YN9Z37EWyNCkltyeFr6PtrSXSLMeFWOeDUwcanht5CIJgPa";
            final String adminFirstName = "Bob";
            final String adminLastName = "Marley";
            userService.createUser(adminEmail, adminPassword, adminFirstName, adminLastName, SesamUser.ROLE_ADMIN, SesamUser.ROLE_ISSUER, SesamUser.ROLE_EDITOR);

            final String userEmail = "jana@mail.com";
            final String userPassword = "{bcrypt}$2a$12$8WYq0/mFBJOY31GWx7BptemS5OTmt4dAV6AsvReXnaGawm6w6aLZq";
            final String userFirstName = "Jana";
            final String userLastname = "Meier";
            userService.createUser(userEmail, userPassword, userFirstName, userLastname, SesamUser.ROLE_ISSUER, SesamUser.ROLE_EDITOR);

        }
    }

    public void initWebsiteConfig() {
        if (websiteConfigurationService.getConfig().isEmpty()) {
            ColorScheme darkMode = new ColorScheme("#313338", "#e20075", "#2b2d31", "#4d4d4d");
            ColorScheme lightMode = new ColorScheme("#FFFFFF", "#e20075", "#e8e8e8", "#4d4d4d");
            try (InputStream iSLogo = getClass().getResourceAsStream("/logo.png")) {
                if (iSLogo == null) {
                    throw new IllegalStateException("Could not find logo.png in resources");
                }
                final var logoImage = imageService.addImage(iSLogo.readAllBytes(), "image/png");
                final var favIcon = imageService.addImage(iSLogo.readAllBytes(), "image/png");
                WebsiteConfiguration websiteConfiguration = new WebsiteConfiguration(
                        "Sesam",
                        darkMode,
                        lightMode,
                        logoImage.getId(),
                        favIcon.getId(),
                        "No Imprint available");
                websiteConfigurationService.addWebsiteConfiguration(websiteConfiguration);

            } catch (IOException | SQLException e) {
                System.out.println("Initial website designconfig could not be initialized");
            }
        }
    }

    public void initBuildings() {
        final List<Building> buildings = buildingService.getBuildings();
        if (buildings.isEmpty()) {
            try (InputStream isbuilding = getClass().getResourceAsStream("/CITEC.png");
                 InputStream isfloor = getClass().getResourceAsStream("/erdgeschoss.png")) {
                if (isbuilding == null) {
                    throw new IllegalStateException("Could not find CITEC.png in resources");
                }
                if (isfloor == null) {
                    throw new IllegalStateException("Could not find erdgeschoss.png in resources");
                }
                final var imageBuilding = imageService.addImage(isbuilding.readAllBytes(), "image/png");
                final var imageFloor = imageService.addImage(isfloor.readAllBytes(), "image/png");
                Building building = buildingService.addBuilding(
                    new Building("CITEC", imageBuilding.getId())
                );
                final var floor = floorService.addFloor("First Floor", imageFloor.getId());
                floor.setBuilding(building);
                building.setFloors(List.of(floor));
                buildingRepository.save(building);
                List<Position> positions = new ArrayList<>();
                positions.add(new Position(102.5, 76.3));
                positions.add(new Position(102.5, 336.3));
                positions.add(new Position(289.5, 336.3));
                positions.add(new Position(289.5, 76.3));
                positions.add(new Position(102.5, 76.3));

                final var room = roomService.addRoom(new RoomCreationData(
                        "0.007", "Hörsaal 0.007 im CITEC", positions
                ));
                room.setDoors(List.of("T.100"));
                roomRepository.save(room);

                final var roomGroup = floor.getDefaultRoomGroup();

                roomGroup.setRooms(List.of(room));
                room.setRoomGroup(roomGroup);
                roomRepository.save(room);

                roomGroup.setFloor(floor);
                roomGroupRepository.save(roomGroup);

                floor.setRoomGroups(List.of(roomGroup));
                floorRepository.save(floor);

            } catch (IOException | SQLException e) {
                throw new NotFoundException("Error while initializing database", e);
            }
        }
    }

    private void initCredentials() {
        final List<Credential> credentials = Streamable.of(credentialService.getCredentials()).toList();
        if (credentials.isEmpty()) {
            //Load credentials from JSON file:
            Collection<Credential> credentialList = new ArrayList<>();
            try (final var stream = getClass().getResourceAsStream("/credentials.json")) {
                String content = new String(stream.readAllBytes());

                JSONObject obj = new JSONObject(content);
                JSONArray arr = obj.getJSONArray("credentials");

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject credentialJSON = arr.getJSONObject(i);

                    Collection<String> checklist = new ArrayList<>();
                    Collection<String> attributes = new ArrayList<>();

                    JSONArray checklistJSON = credentialJSON.getJSONArray("checkboxes");
                    JSONArray attributesJSON = credentialJSON.getJSONArray("attributes");

                    for (int j = 0; j < checklistJSON.length(); j++) {
                        checklist.add(checklistJSON.getString(j));
                    }
                    for (int j = 0; j < attributesJSON.length(); j++) {
                        attributes.add(attributesJSON.getString(j));
                    }

                    Credential credential = new Credential(
                            credentialJSON.getString("did"),
                            checklist,
                            attributes,
                            credentialJSON.getString("fancyName")
                            );

                    credentialService.createCredential(credential);
                    credentialList.add(credential);
                }

                if(userService.getUserByEmail("admin@mail.com").isPresent()){
                    SesamUser admin = userService.getUserByEmail("admin@mail.com").get();
                    admin.setIssuableCredentials(credentialList);
                    userService.updateUser(admin);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void afterPropertiesSet() {
        initUsers();
        initBuildings();
        initCredentials();
        initWebsiteConfig();
    }

}
