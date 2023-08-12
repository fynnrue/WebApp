package com.gpse.basis.web;

import com.gpse.basis.domain.Image;
import com.gpse.basis.domain.service.ImageService;
import com.gpse.basis.web.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

/**
 * The ImageController class handles requests related to images.
 */

@RestController
@CrossOrigin
@RequestMapping("/api")
public class ImageController {

    private final ImageService imageService;

    @Autowired
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    /**
     * Returns the url an image can be reached with.
     *
     * @param id The id of the image
     * @return The url the image can be reached with.
     */
    public static String urlFor(Long id) {
        return String.format("/api/images/%s", id);
    }

    /**
     * Retrieves an image with the specified id.
     *
     * @param id The id of the image to retrieve.
     * @return ResponseEntity containing the image data.
     * @throws SQLException If an error occurs while interacting with the database.
     * @throws IOException If an error occurs while reading the image data.
     * @throws NotFoundException If the image with the specified id does not exist.
     */
    @GetMapping("/images/{id:\\d+}")
    public ResponseEntity<byte[]> getImage(@PathVariable("id") final Long id) throws SQLException, IOException {
        final Optional<Image> image = imageService.getImageById(id);
        if (image.isEmpty()) {
            throw new NotFoundException();
        }
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.valueOf(image.get().getType()))
                .body(image.get().getData().getBinaryStream().readAllBytes());
    }
}
