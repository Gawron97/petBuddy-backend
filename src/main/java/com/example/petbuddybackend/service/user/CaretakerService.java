package com.example.petbuddybackend.service.user;

import com.example.petbuddybackend.dto.criteriaSearch.CaretakerSearchCriteria;
import com.example.petbuddybackend.dto.offer.OfferFilterDTO;
import com.example.petbuddybackend.dto.photo.PhotoLinkDTO;
import com.example.petbuddybackend.dto.user.*;
import com.example.petbuddybackend.entity.address.Address;
import com.example.petbuddybackend.entity.photo.PhotoLink;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.service.mapper.CaretakerMapper;
import com.example.petbuddybackend.service.mapper.PhotoMapper;
import com.example.petbuddybackend.service.photo.PhotoService;
import com.example.petbuddybackend.utils.exception.throweable.general.IllegalActionException;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import com.example.petbuddybackend.utils.exception.throweable.photo.PhotoLimitException;
import com.example.petbuddybackend.utils.provider.geolocation.GeolocationProvider;
import com.example.petbuddybackend.utils.provider.geolocation.dto.Coordinates;
import com.example.petbuddybackend.utils.specification.CaretakerSpecificationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CaretakerService {

    private static final String CARETAKER = "Caretaker";
    private static final String PHOTO_LIMIT_EXCEEDED_MESSAGE = "Photo limit exceeded for Caretaker. Provided: %d, expected %s";
    public static final String CARETAKER_EXISTS_MESSAGE = "Caretaker with email %s already exists";

    private final CaretakerRepository caretakerRepository;
    private final CaretakerMapper caretakerMapper = CaretakerMapper.INSTANCE;
    private final PhotoMapper photoMapper = PhotoMapper.INSTANCE;

    private final UserService userService;
    private final PhotoService photoService;
    private final GeolocationProvider geolocationProvider;

    @Transactional(readOnly = true)
    public SearchCaretakersResponseDTO getCaretakers(Pageable pageable,
                                                           CaretakerSearchCriteria filters,
                                                           Set<OfferFilterDTO> offerFilters) {
        Specification<Caretaker> spec = CaretakerSpecificationUtils.toSpecification(filters, offerFilters);

        Page<CaretakerDTO> caretakers = caretakerRepository.findAll(spec, pageable)
                .map(this::renewCaretakerPictures)
                .map(caretakerMapper::mapToCaretakerDTO);

        Coordinates coordinates;
        try{
            coordinates = geolocationProvider.getCoordinatesOfAddress("Poland", filters.cityLike());
        } catch (NotFoundException e) {
            coordinates = new Coordinates(null, null);
        }

        return SearchCaretakersResponseDTO.builder()
                .caretakers(caretakers)
                .cityLatitude(coordinates.latitude())
                .cityLongitude(coordinates.longitude())
                .build();

    }

    public CaretakerComplexPublicDTO getOtherCaretaker(String caretakerEmail) {
        Caretaker caretaker = getCaretakerByEmail(caretakerEmail);
        this.renewCaretakerPictures(caretaker);
        return caretakerMapper.mapToCaretakerComplexPublicDTO(caretaker);
    }

    public CaretakerComplexDTO getMyCaretakerProfile(String caretakerEmail) {
        Caretaker caretaker = getCaretakerByEmail(caretakerEmail);
        this.renewCaretakerPictures(caretaker);
        return caretakerMapper.mapToCaretakerComplexDTO(caretaker);
    }

    public Caretaker getCaretakerByEmail(String caretakerEmail) {
        return caretakerRepository.findById(caretakerEmail)
                .orElseThrow(() -> NotFoundException.withFormattedMessage(CARETAKER, caretakerEmail));
    }

    public boolean caretakerExists(String caretakerEmail) {
        return caretakerRepository.existsById(caretakerEmail);
    }

    @Transactional
    public CaretakerComplexDTO addCaretaker(
            ModifyCaretakerDTO createCaretakerDTO,
            String email,
            List<MultipartFile> newOfferPhotos
    ) {
        assertCaretakerNotExists(email);
        assertOfferPhotoCountWithinLimit(newOfferPhotos);
        AppUser appUser = userService.getAppUser(email);
        List<PhotoLink> uploadedOfferPhotos = photoService.uploadPhotos(newOfferPhotos);
        Caretaker caretaker = caretakerMapper.mapToCaretaker(createCaretakerDTO, appUser, uploadedOfferPhotos);
        updateCaretakerGeolocation(caretaker.getAddress());

        renewCaretakerPictures(caretaker);
        return caretakerMapper.mapToCaretakerComplexDTO(caretakerRepository.save(caretaker));
    }

    @Transactional
    public CaretakerComplexDTO editCaretaker(
            ModifyCaretakerDTO modifyCaretakerDTO,
            String email,
            Set<String> offerBlobsToKeep,
            List<MultipartFile> newOfferPhotos
    ) {
        Caretaker caretaker = getCaretakerByEmail(email);
        caretakerMapper.updateCaretakerFromDTO(caretaker, modifyCaretakerDTO);
        updateCaretakerGeolocation(caretaker.getAddress());

        applyOfferPhotosPatch(caretaker, offerBlobsToKeep, newOfferPhotos);
        renewCaretakerPictures(caretaker);
        return caretakerMapper.mapToCaretakerComplexDTO(caretakerRepository.save(caretaker));
    }

    @Transactional
    public List<PhotoLinkDTO> putOfferPhotos(
            String email,
            Set<String> offerBlobsToKeep,
            List<MultipartFile> newOfferPhotos
    ) {
        Caretaker caretaker = getCaretakerByEmail(email);
        applyOfferPhotosPatch(caretaker, offerBlobsToKeep, newOfferPhotos);
        renewCaretakerPictures(caretaker);

        return caretakerRepository.save(caretaker).getOfferPhotos().stream()
                .map(photoMapper::mapToPhotoLinkDTO)
                .toList();
    }

    private void updateCaretakerGeolocation(Address address) {
        Coordinates coordinates = geolocationProvider.getCoordinatesOfAddress(
                "Poland",
                address.getCity(),
                address.getStreet()
        );
        address.setLatitude(coordinates.latitude());
        address.setLongitude(coordinates.longitude());
    }

    private void applyOfferPhotosPatch(Caretaker caretaker, Set<String> blobsToKeep, List<MultipartFile> newPhotos) {
        List<PhotoLink> currentPhotos = caretaker.getOfferPhotos();

        List<PhotoLink> photosToRemove = currentPhotos.stream()
                .filter(photo -> !blobsToKeep.contains(photo.getBlob()))
                .toList();

        caretaker.getOfferPhotos().removeAll(photosToRemove);
        assertOfferPhotoCountWithinLimit(caretaker.getOfferPhotos().size() + newPhotos.size());
        caretaker.getOfferPhotos().addAll(photoService.uploadPhotos(newPhotos));
        photoService.schedulePhotoDeletions(photosToRemove);
    }

    private Caretaker renewCaretakerPictures(Caretaker caretaker) {
        AppUser appUser = caretaker.getAccountData();
        List<PhotoLink> userPhotos = caretaker.getOfferPhotos();

        userService.renewProfilePicture(appUser);
        photoService.updatePhotoExpirations(userPhotos);
        return caretakerRepository.save(caretaker);
    }

    private void assertCaretakerNotExists(String caretakerEmail) {
        if(caretakerExists(caretakerEmail)) {
            throw new IllegalActionException(String.format(CARETAKER_EXISTS_MESSAGE, caretakerEmail));
        }
    }

    private void assertOfferPhotoCountWithinLimit(int photoSize) {
        if(photoSize > Caretaker.MAX_OFFER_PHOTO_LIMIT) {
            throw new PhotoLimitException(String.format(
                    PHOTO_LIMIT_EXCEEDED_MESSAGE,
                    photoSize,
                    Caretaker.MAX_OFFER_PHOTO_LIMIT)
            );
        }
    }

    private void assertOfferPhotoCountWithinLimit(List<MultipartFile> newPhotos) {
        assertOfferPhotoCountWithinLimit(newPhotos.size());
    }
}
