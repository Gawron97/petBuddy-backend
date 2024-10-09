package com.example.petbuddybackend.service.user;

import com.example.petbuddybackend.dto.criteriaSearch.CaretakerSearchCriteria;
import com.example.petbuddybackend.dto.offer.OfferFilterDTO;
import com.example.petbuddybackend.dto.rating.RatingResponse;
import com.example.petbuddybackend.dto.user.CaretakerComplexInfoDTO;
import com.example.petbuddybackend.dto.user.CaretakerDTO;
import com.example.petbuddybackend.dto.user.CreateCaretakerDTO;
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
import com.example.petbuddybackend.service.mapper.RatingMapper;
import com.example.petbuddybackend.service.photo.PhotoService;
import com.example.petbuddybackend.utils.exception.throweable.general.IllegalActionException;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CaretakerService {

    private final static String CARETAKER = "Caretaker";
    private final static String CLIENT = "Client";

    private final CaretakerRepository caretakerRepository;
    private final ClientRepository clientRepository;
    private final RatingRepository ratingRepository;
    private final CaretakerMapper caretakerMapper = CaretakerMapper.INSTANCE;
    private final RatingMapper ratingMapper = RatingMapper.INSTANCE;

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

    public CaretakerComplexInfoDTO getCaretaker(String caretakerEmail) {
        Caretaker caretaker = getCaretakerByEmail(caretakerEmail);
        renewCaretakerPictures(caretaker);
        return caretakerMapper.mapToCaretakerComplexInfoDTO(caretaker);
    }

    public Caretaker getCaretakerByEmail(String caretakerEmail) {
        return caretakerRepository.findById(caretakerEmail)
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

    // TODO: commit squished
    // TODO: test if throws then will the photos get removed?
    @Transactional
    public CaretakerComplexInfoDTO addCaretaker(
            CreateCaretakerDTO createCaretakerDTO,
            String email,
            List<MultipartFile> newOfferPhotos
    ) {
        assertCaretakerNotExists(email);
        AppUser appUser = userService.getAppUser(email);
        Set<PhotoLink> uploadedOfferPhotos = photoService.uploadPhotos(newOfferPhotos);
        Caretaker caretaker = caretakerMapper.mapToCaretaker(createCaretakerDTO, appUser, uploadedOfferPhotos);

        renewCaretakerPictures(caretaker);
        return caretakerMapper.mapToCaretakerComplexInfoDTO(caretakerRepository.save(caretaker));
    }

    @Transactional
    public CaretakerComplexInfoDTO editCaretaker(
            ModifyCaretakerDTO modifyCaretakerDTO,
            String email,
            List<MultipartFile> newOfferPhotos
    ) {
        AppUser appUser = userService.getAppUser(email);
        Caretaker caretaker = getCaretakerByEmail(appUser.getEmail());

        // TODO: to function
        Set<PhotoLink> currentOfferPhotos = caretaker.getOfferPhotos();
        Set<String> offerBlobsToKeep = modifyCaretakerDTO.offerBlobsToKeep();

        Set<PhotoLink> offerBlobsToRemove = currentOfferPhotos.stream()
                .filter(photoLink -> !offerBlobsToKeep.contains(photoLink.getBlob()))
                .collect(Collectors.toSet());

        photoService.deletePhotos(offerBlobsToRemove);
        Set<PhotoLink> uploadedOfferPhotos = photoService.uploadPhotos(newOfferPhotos);

        Set<PhotoLink> mergedPhotos = currentOfferPhotos.stream()
                .filter(photoLink -> offerBlobsToKeep.contains(photoLink.getBlob()))
                .collect(Collectors.toSet());

        mergedPhotos.addAll(uploadedOfferPhotos);

        caretakerMapper.updateCaretakerFromDTO(caretaker, modifyCaretakerDTO, mergedPhotos);
        renewCaretakerPictures(caretaker);
        return caretakerMapper.mapToCaretakerComplexInfoDTO(caretakerRepository.save(caretaker));
    }

    // FIXME: might throw if I replace photos
    // TODO: check sql of this method
    // TODO check if saved photo is in caretaker or is just saved in db
    private Caretaker renewCaretakerPictures(Caretaker caretaker) {
        AppUser appUser = caretaker.getAccountData();
        Set<PhotoLink> userPhotos = caretaker.getOfferPhotos();

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
        if (caretakerExists(caretakerEmail)) {
            throw new IllegalActionException("Caretaker with email " + caretakerEmail + " already exists");
        }
    }
}
