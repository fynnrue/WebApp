package com.gpse.basis;

import com.gpse.basis.domain.Building;
import com.gpse.basis.domain.service.BuildingService;
import com.gpse.basis.domain.service.ImageService;
import com.gpse.basis.web.BuildingController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BuildingControllerTest {

    @Value("${app.max-image-size}")
    private long MAX_IMAGE_SIZE; // 10 MiB by default
    private MockMvc mockMvc;
    private final BuildingService buildingService = Mockito.mock(BuildingService.class);
    private final ImageService imageService = Mockito.mock(ImageService.class);

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new BuildingController(buildingService, imageService)).build();
    }

    @Test
    public void getBuildings() throws Exception {
        given(buildingService.getBuildings()).willReturn(List.of(new Building("Building1", 1L)));

        mockMvc.perform(get("/api/buildings"))
                .andExpect(status().isOk());
    }

    @Test
    public void getBuildingById() throws Exception {
        given(buildingService.getBuildingById(1L)).willReturn(Optional.of(new Building("Building1", 1L)));

        mockMvc.perform(get("/api/buildings/1"))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteBuilding() throws Exception {
        Building building = new Building("Building1", 1L);
        given(buildingService.getBuildingById(1L)).willReturn(Optional.of(building));
        given(buildingService.deleteBuilding(building)).willReturn(true);

        mockMvc.perform(delete("/api/deleteBuildings/1"))
                .andExpect(status().isOk());
    }

    @Test
    public void getBuildingByIdNotFound() throws Exception {
        given(buildingService.getBuildingById(1L)).willReturn(Optional.empty());

        mockMvc.perform(get("/api/buildings/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void storeBuildingExceedingSize() throws Exception {
        byte[] oversizedImage = new byte[(int) MAX_IMAGE_SIZE + 1000]; // Exceeding image size limit
        MockMultipartFile imageFile = new MockMultipartFile("image", "hello.png", MediaType.IMAGE_PNG_VALUE, oversizedImage);

        mockMvc.perform(multipart("/api/buildings")
                        .file("image", imageFile.getBytes())
                        .param("name", "Test Building")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }
}