package com.example.petbuddybackend.service.user;

import com.example.petbuddybackend.dto.criteriaSearch.CaretakerSearchCriteria;
import com.example.petbuddybackend.dto.offer.OfferFilterDTO;
import com.example.petbuddybackend.dto.photo.PhotoLinkDTO;
import com.example.petbuddybackend.dto.rating.RatingResponse;
import com.example.petbuddybackend.dto.user.CaretakerComplexDTO;
import com.example.petbuddybackend.dto.user.CaretakerComplexPublicDTO;
import com.example.petbuddybackend.dto.user.CaretakerDTO;
import com.example.petbuddybackend.dto.user.ModifyCaretakerDTO;
import com.example.petbuddybackend.entity.photo.PhotoLink;
import com.example.petbuddybackend.entity.rating.Rating;
import com.example.petbuddybackend.entity.rating.RatingKey;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.repository.rating.RatingRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.repository.user.ClientRepository;
import com.example.petbuddybackend.service.mapper.CaretakerMapper;
import com.example.petbuddybackend.service.mapper.PhotoMapper;
import com.example.petbuddybackend.service.mapper.RatingMapper;
import com.example.petbuddybackend.service.photo.PhotoService;
import com.example.petbuddybackend.utils.exception.throweable.general.IllegalActionException;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import com.example.petbuddybackend.utils.exception.throweable.photo.PhotoLimitException;
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
    private static final String CLIENT = "Client";
    private static final String PHOTO_LIMIT_EXCEEDED_MESSAGE = "Photo limit exceeded for Caretaker. Provided: %d, expected %s";
    public static final String CARETAKER_EXISTS_MESSAGE = "Caretaker with email %s already exists";

    private final CaretakerRepository caretakerRepository;
    private final ClientRepository clientRepository;
    private final RatingRepository ratingRepository;
    private final CaretakerMapper caretakerMapper = CaretakerMapper.INSTANCE;
    private final RatingMapper ratingMapper = RatingMapper.INSTANCE;
    private final PhotoMapper photoMapper = PhotoMapper.INSTANCE;

    private final UserService userService;
    private final PhotoService photoService;

    @Transactional(readOnly = true)
    public Page<CaretakerDTO> getCaretakers(Pageable pageable,
                                            CaretakerSearchCriteria filters,
                                            Set<OfferFilterDTO> offerFilters) {
        Specification<Caretaker> spec = CaretakerSpecificationUtils.toSpecification(filters, offerFilters);

        return caretakerRepository.findAll(spec, pageable)
                .map(this::renewCaretakerPictures)
                .map(caretakerMapper::mapToCaretakerDTO);
    }

    public CaretakerComplexPublicDTO getOtherCaretaker(String caretakerEmail) {
        Caretaker caretaker = getCaretakerByEmail(caretakerEmail);
        return caretakerMapper.mapToCaretakerComplexPublicDTO(caretaker);
    }

    public CaretakerComplexDTO getMyCaretakerProfile(String caretakerEmail) {
        Caretaker caretaker = getCaretakerByEmail(caretakerEmail);
        return caretakerMapper.mapToCaretakerComplexDTO(caretaker);
    }

    public Caretaker getCaretakerByEmail(String caretakerEmail) {
        return caretakerRepository.findById(caretakerEmail)
                .map(this::renewCaretakerPictures)
                .orElseThrow(() -> NotFoundException.withFormattedMessage(CARETAKER, caretakerEmail));
    }

    public Page<RatingResponse> getRatings(Pageable pageable, String caretakerEmail) {
        return ratingRepository.findAllByCaretakerEmail(caretakerEmail, pageable)
                .map(ratingMapper::mapToRatingResponse);
    }

    public Rating getRating(String caretakerEmail, String clientEmail) {
        return getRating(new RatingKey(caretakerEmail, clientEmail));
    }

    public boolean caretakerExists(String caretakerEmail) {
        return caretakerRepository.existsById(caretakerEmail);
    }

    public RatingResponse rateCaretaker(String caretakerEmail, String clientEmail, int rating, String comment) {
        assertCaretakerAndClientExist(caretakerEmail, clientEmail);

        if(caretakerEmail.equals(clientEmail)) {
            throw new IllegalActionException("User cannot rate himself");
        }

        return ratingMapper.mapToRatingResponse(createOrUpdateRating(caretakerEmail, clientEmail, rating, comment));
    }

    public RatingResponse deleteRating(String caretakerEmail, String clientEmail) {
        assertCaretakerAndClientExist(caretakerEmail, clientEmail);

        RatingKey ratingKey = new RatingKey(caretakerEmail, clientEmail);
        Rating rating = getRating(ratingKey);
        ratingRepository.deleteById(ratingKey);

        return ratingMapper.mapToRatingResponse(rating);
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

    private void applyOfferPhotosPatch(Caretaker caretaker, Set<String> blobsToKeep, List<MultipartFile> newPhotos) {
        List<PhotoLink> currentPhotos = caretaker.getOfferPhotos();

        List<PhotoLink> photosToRemove = currentPhotos.stream()
                .filter(photo -> !blobsToKeep.contains(photo.getBlob()))
                .toList();

        caretaker.getOfferPhotos().removeAll(photosToRemove);
        assertOfferPhotoCountWithinLimit(caretaker.getOfferPhotos().size() + newPhotos.size());
        caretaker.getOfferPhotos().addAll(photoService.uploadPhotos(newPhotos));
        photoService.deletePhotos(photosToRemove);
    }

    private Caretaker renewCaretakerPictures(Caretaker caretaker) {
        AppUser appUser = caretaker.getAccountData();
        List<PhotoLink> userPhotos = caretaker.getOfferPhotos();

        userService.renewProfilePicture(appUser);
        photoService.updatePhotoExpirations(userPhotos);
        return caretakerRepository.save(caretaker);
    }

    private Rating createOrUpdateRating(String caretakerEmail, String clientEmail, int rating, String comment) {
        Rating ratingEntity = getOrCreateRating(caretakerEmail, clientEmail);

        ratingEntity.setRating(rating);
        ratingEntity.setComment(comment);

        return ratingRepository.save(ratingEntity);
    }

    private Rating getOrCreateRating(String caretakerEmail, String clientEmail) {
        return ratingRepository.findById(new RatingKey(caretakerEmail, clientEmail))
                .orElse(
                        Rating.builder()
                                .clientEmail(clientEmail)
                                .caretakerEmail(caretakerEmail)
                                .build()
                );
    }

    private void assertCaretakerAndClientExist(String caretakerEmail, String clientEmail) {
        if (!caretakerExists(caretakerEmail)) {
            throw NotFoundException.withFormattedMessage(CARETAKER, caretakerEmail);
        }

        if (!clientRepository.existsById(clientEmail)) {
            throw NotFoundException.withFormattedMessage(CLIENT, clientEmail);
        }
    }

    private Rating getRating(RatingKey ratingKey) {
        return ratingRepository.findById(ratingKey).orElseThrow(
                () -> new NotFoundException("Rating does not exist")
        );
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
