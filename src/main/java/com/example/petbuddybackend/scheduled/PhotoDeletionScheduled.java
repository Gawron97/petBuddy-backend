package com.example.petbuddybackend.scheduled;

import com.example.petbuddybackend.entity.photo.PhotoLink;
import com.example.petbuddybackend.repository.photo.PhotoLinkRepository;
import com.example.petbuddybackend.service.photo.PhotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class PhotoDeletionScheduled {

    private final PhotoService photoService;
    private final PhotoLinkRepository photoRepository;

    @Scheduled(cron = "0 1 0 * * *")
    public void terminatePhotos() {
        List<PhotoLink> photosToDelete = photoRepository.getAllByMarkedForDeletionAtNotNull();
        log.info("Deleting {} photos", photosToDelete.size());
        photoService.schedulePhotoDeletions(photosToDelete);
    }
}
