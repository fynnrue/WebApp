package com.gpse.basis.domain.service;

import com.gpse.basis.domain.Image;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Interface for Services for managing images.
 */
public interface ImageService {
    /**
     * Add an image to the Repository.
     *
     * @param data The raw bytes of the image.
     * @param type The image type (e.g. image/png, image/jpg).
     * @return The Image object stored in the repository.
     */
    Image addImage(Blob data, String type);

    /**
     * Add an image to the repository
     *
     * @param data The raw bytes of the image.
     * @param type The image type (e.g. image/png, image/jpg).
     * @return The Image object stored in the repository.
     */
    default Image addImage(byte[] data, String type) throws SQLException {
        return addImage(new SerialBlob(data), type);
    }

    /**
     * Add an image to the repository
     *
     * @param file The image file to add to the repository
     * @return The Image object stored in the repository.
     */
    default Image addImage(MultipartFile file) throws IOException, SQLException {
        return addImage(file.getBytes(), file.getContentType());
    }

    /**
     * Removes an image from the repository.
     *
     * @param id The id of the image to remove.
     */
    void removeImage(long id);

    /**
     * @return An Iterable of all images stored in the repository.
     */
    Iterable<Image> getAllImages();

    /**
     * Searches for an image by id.
     *
     * @param id The id of the image to search for.
     * @return The Image if it exists.
     */
    Optional<Image> getImageById(long id);
}
