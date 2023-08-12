package com.gpse.basis.domain.service;

import com.gpse.basis.domain.Image;
import com.gpse.basis.domain.repository.ImageRepository;
import com.gpse.basis.web.exceptions.InvalidMimeTypeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.sql.Blob;
import java.util.Optional;

@Service
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;

    @Autowired
    public ImageServiceImpl(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Override
    public Image addImage(Blob data, String type) {
        if (!MimeTypeUtils.parseMimeType(type).getType().equals("image")) {
            throw new InvalidMimeTypeException();
        }
        Image entity = new Image(data, type);
        imageRepository.save(entity);
        return entity;
    }

    @Override
    public void removeImage(long id) {
        imageRepository.deleteById(id);
    }

    @Override
    public Iterable<Image> getAllImages() {
        return imageRepository.findAll();
    }

    @Override
    public Optional<Image> getImageById(long id) {
        return imageRepository.findById(id);
    }
}
