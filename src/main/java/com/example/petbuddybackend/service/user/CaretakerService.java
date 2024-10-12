package com.example.petbuddybackend.service.user;

import com.example.petbuddybackend.dto.criteriaSearch.CaretakerSearchCriteria;
import com.example.petbuddybackend.dto.offer.OfferFilterDTO;
import com.example.petbuddybackend.dto.photo.PhotoLinkDTO;
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
import com.example.petbuddybackend.service.mapper.PhotoMapper;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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


    @Transactional
    public CaretakerComplexInfoDTO addCaretaker(
            CreateCaretakerDTO createCaretakerDTO,
            String email,
            List<MultipartFile> newOfferPhotos
    ) {
        assertCaretakerNotExists(email);
        AppUser appUser = userService.getAppUser(email);
        List<PhotoLink> uploadedOfferPhotos = photoService.uploadPhotos(newOfferPhotos);
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
        Caretaker caretaker = getCaretakerByEmail(email);
        caretakerMapper.updateCaretakerFromDTO(caretaker, modifyCaretakerDTO);

        applyOfferPhotosPatch(caretaker, modifyCaretakerDTO.offerBlobsToKeep(), newOfferPhotos);
        renewCaretakerPictures(caretaker);
        return caretakerMapper.mapToCaretakerComplexInfoDTO(caretakerRepository.save(caretaker));
    }

    @Transactional
    public List<PhotoLinkDTO> patchOfferPhotos(
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

        int currentPhotosSize = currentPhotos.size();
        int photosToKeepSize = Math.min(currentPhotosSize, blobsToKeep.size() + newPhotos.size());
        int photosToRemoveSize = Math.max(0, currentPhotosSize - photosToKeepSize);

        List<PhotoLink> photosToKeep = new ArrayList<>(photosToKeepSize);
        List<PhotoLink> photosToRemove = new ArrayList<>(photosToRemoveSize);

        currentPhotos.forEach(photo -> {
            if(blobsToKeep.contains(photo.getBlob())) {
                photosToKeep.add(photo);
            } else {
                photosToRemove.add(photo);
            }
        });

        caretaker.getOfferPhotos().clear();
        caretaker.getOfferPhotos().addAll(photosToKeep);
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
        if (caretakerExists(caretakerEmail)) {
            throw new IllegalActionException("Caretaker with email " + caretakerEmail + " already exists");
        }
    }
}
