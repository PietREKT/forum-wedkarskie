package org.piet.forumbackend.fishing_spots.core.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.util.GeometricShapeFactory;
import org.piet.forumbackend.content.core.entities.enums.VerificationStatus;
import org.piet.forumbackend.fish.entities.Fish;
import org.piet.forumbackend.fish.services.FishService;
import org.piet.forumbackend.fishing_spots.core.dtos.requests.AddressDto;
import org.piet.forumbackend.fishing_spots.core.dtos.requests.CreateFishingSpotDto;
import org.piet.forumbackend.fishing_spots.core.dtos.requests.LocationDto;
import org.piet.forumbackend.fishing_spots.core.dtos.responses.FishingSpotDto;
import org.piet.forumbackend.fishing_spots.core.dtos.responses.FishingSpotListDto;
import org.piet.forumbackend.fishing_spots.core.entities.FishingSpot;
import org.piet.forumbackend.fishing_spots.core.exceptions.FishingSpotNotFoundException;
import org.piet.forumbackend.fishing_spots.core.exceptions.LocationDtoIncompleteException;
import org.piet.forumbackend.fishing_spots.core.exceptions.LocationNotFoundException;
import org.piet.forumbackend.fishing_spots.core.repositories.FishingSpotRepository;
import org.piet.forumbackend.globals.exceptions.BadRequestException;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.globals.exceptions.UnauthorizedAccessException;
import org.piet.forumbackend.globals.pagination.PaginationDto;
import org.piet.forumbackend.globals.properties.FileProperties;
import org.piet.forumbackend.users.core.entities.Role;
import org.piet.forumbackend.users.core.entities.User;
import org.piet.forumbackend.users.core.exceptions.UserNotLoggedInException;
import org.piet.forumbackend.users.core.repos.UserRepository;
import org.piet.forumbackend.users.core.services.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
public class FishingSpotServiceImpl implements FishingSpotService {
    private final UserRepository userRepository;
    private final FishingSpotRepository fishingSpotRepository;
    private final MessageSource messageSource;
    private final FileProperties fileProperties;
    private final UserService userService;
    private final FishService fishService;

    @Value("${forum.api.tomtom_key}")
    private String TOM_TOM_API_KEY;

    @Value("${forum.api.tomtom_minimum_precision}")
    private Double MINIMUM_CONFIDENCE;

    @Override
    @Transactional
    public void markFishingSpotAsVerified(FishingSpot fishingSpot) {
        fishingSpot.setVerificationStatus(VerificationStatus.ACCEPTED);
        fishingSpotRepository.save(fishingSpot);
    }

    @Override
    @Transactional
    public void markFishingSpotAsRejected(FishingSpot fishingSpot) {
        fishingSpot.setVerificationStatus(VerificationStatus.ACCEPTED);
        fishingSpotRepository.save(fishingSpot);
    }

    @Override
    @Transactional
    public void revokeFishingSpotReview(Long spotId) throws FishingSpotNotFoundException {
        FishingSpot spot = getFishingSpotById(spotId);
        spot.setVerificationStatus(VerificationStatus.IN_REVIEW);
        fishingSpotRepository.save(spot);
    }

    @Override
    @Transactional
    public void deleteFishingSpot(FishingSpot fishingSpot, User currentUser) {
        if (!currentUser.isAdmin() && !fishingSpot.getOwner().equalsUser(currentUser)) {
            throw new AccessDeniedException(
                    messageSource.getMessage("error.spots.no_perms_for_delete",
                            null,
                            LocaleContextHolder.getLocale()
                    )
            );
        }
        fishingSpotRepository.delete(fishingSpot);
    }

    @Override
    public FishingSpot getFishingSpotById(Long id) throws FishingSpotNotFoundException {
        return fishingSpotRepository.findById(id).orElseThrow(() -> new FishingSpotNotFoundException(
                messageSource.getMessage("errors.spots.id_not_found",
                        new Object[]{id},
                        LocaleContextHolder.getLocale())
        ));
    }

    @Override
    public List<FishingSpotListDto> getFishingSpotByName(String name) {
        if (name == null) return List.of();

        String q = name.trim();
        if (q.length() < 2 ) return List.of();

        return fishingSpotRepository.findTop10ByNameStartingWithIgnoreCaseOrderByNameAsc(q)
                .stream().map(FishingSpotListDto::create).toList();
    }

    @Override
    public Page<FishingSpot> getFishingSpotsByStatus(VerificationStatus status, Pageable pageable) {
        return fishingSpotRepository.findByVerificationStatus(status, pageable);
    }

    //name desc type managers fish
    @Override
    @Transactional
    public FishingSpot createFishingSpot(CreateFishingSpotDto dto) throws LocationDtoIncompleteException, IOException, LocationNotFoundException, UnauthorizedAccessException, BadRequestException, UserNotLoggedInException {
        User sentBy = userService.getCurrentUser();

        List<User> managers = dto.getManagerIds()
                .stream()
                .map(userService::getUserByIdOpt)
                .map(opt -> opt.orElseThrow(() -> new BadRequestException("All managers must have valid ids.")))
                .collect(Collectors.toList());
        List<Fish> fish = dto.getFishIds()
                .stream()
                .map(fishService::getFishByIdOptional)
                .map(opt -> opt.orElseThrow(() -> new BadRequestException("All fish must have valid ids.")))
                .collect(Collectors.toList());


        LocationDto location = dto.getLocationDto();

        FishingSpot spot = new FishingSpot();
        managers.add(sentBy);

        if (dto.getType() == FishingSpot.FISHING_SPOT_TYPE.PUBLIC && !sentBy.hasPermLevelAtLeast(Role.PZW)) {
            log.warn("User with ID: {} tried to access forbidden resource: \"createFishingSpot\" - PUBLIC type", sentBy.getId());
            throw new UnauthorizedAccessException(
                    messageSource.getMessage("error.users.unauthorized_access",
                            null,
                            LocaleContextHolder.getLocale())
            );
        }

        if (dto.getType() == FishingSpot.FISHING_SPOT_TYPE.PUBLIC && !managers.stream().allMatch(u -> u.hasPermLevelAtLeast(Role.PZW))) {
            String ids = String.join(", ", managers.stream().filter(u -> !u.hasPermLevelAtLeast(Role.PZW)).map(u -> u.getId().toString()).toList());
            throw new BadRequestException(
                    messageSource.getMessage("error.spots.not_all_have_perms",
                            new Object[]{sentBy.getId(), ids},
                            LocaleContextHolder.getLocale())
            );
        }

        if (location.getLongitude() != null && location.getLatitude() != null) {
            var geoFactory = new GeometryFactory();
            Point point = geoFactory.createPoint(
                    new Coordinate(location.getLongitude(), location.getLatitude())
            );
            spot.setLocation(point);
        } else if (location.getAddress() == null) {
            throw new LocationDtoIncompleteException(
                    messageSource.getMessage("error.spots.location_dto_incomplete",
                            null,
                            LocaleContextHolder.getLocale())
            );
        } else {
            Point point = getPointByAddress(location.getAddress());
            spot.setLocation(point);
        }

        spot.setFish(fish);
        spot.setName(dto.getName());
        spot.setDescription(dto.getDescription());
        spot.setManagers(managers);
        spot.setOwner(sentBy);

        return fishingSpotRepository.save(spot);
    }

    @Override
    @Transactional
    public FishingSpot updateStatue(FishingSpot fishingSpot, MultipartFile newStatue, User user) throws BadRequestException, IOException, UnauthorizedAccessException {
        if (fishingSpot.getManagers().stream().noneMatch(u -> u.equalsUser(user)) && !fishingSpot.getOwner().equalsUser(user)) {
            log.warn("User with id: {} tried to update statue for a fishing spot with id: {} despite lacking permissions.", user.getId(), fishingSpot.getId());
            throw new UnauthorizedAccessException(
                    messageSource.getMessage(
                            "error.users.unauthorized_access",
                            null,
                            LocaleContextHolder.getLocale()
                    )
            );
        }

        if (newStatue == null) {
            throw new BadRequestException(
                    messageSource.getMessage("error.requests.incomplete",
                            null,
                            LocaleContextHolder.getLocale())
            );
        }
        String fileExtension = FileProperties.getFileExtension(newStatue.getOriginalFilename());

        if (!fileExtension.equals(".pdf"))
            throw new BadRequestException("Fishing spot's statue must be in *.pdf format!");

        File statue = new File(fileProperties.getStatuesFolder(), fishingSpot.getId() + fileExtension);
        newStatue.transferTo(statue);

        fishingSpot.setStatuteUrl("/spots/statues/" + statue.getName());
        log.info("User with id: {} updated a statue for fishing spot with id: {}", user.getId(), fishingSpot.getId());
        return fishingSpotRepository.save(fishingSpot);
    }

    @Override
    public List<FishingSpot> getFishingSpotsInRadius(Double x, Double y, Integer radiusKm, Pageable pageable) {
        Point centre = new GeometryFactory(new PrecisionModel(), 4326).createPoint(new Coordinate(x, y));
        Geometry radius = createRadius(centre, radiusKm);
        return fishingSpotRepository.findByLocation(radius, VerificationStatus.ACCEPTED, pageable).getContent()
                .stream()
                .sorted(Comparator.comparing(spot -> spot.getLocation().distance(centre)))
                .toList();
    }

    private Point getPointByAddress(AddressDto address) throws JsonProcessingException, LocationNotFoundException {
        String tag = LocaleContextHolder.getLocale().toLanguageTag();
        if (tag.equalsIgnoreCase("en")) tag = "en-US";
        if (tag.equalsIgnoreCase("pl")) tag = "pl-PL";

        URI uri = UriComponentsBuilder
                .fromUriString("https://api.tomtom.com/search/2/structuredGeocode.json")
                .queryParam("key", TOM_TOM_API_KEY)
                .queryParam("countryCode", address.getCountryCode())
                .queryParam("streetNumber", address.getPropertyNo())
                .queryParam("streetName", address.getStreet())
                .queryParam("municipality", address.getMunicipality())
                .queryParam("language", tag)
                .build()
                .toUri();
        RestTemplate restTemplate = new RestTemplate();
        String json = restTemplate.getForObject(uri, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(json);

        Stream<JsonNode> results = StreamSupport.stream(root.path("results").spliterator(), false);

        Optional<JsonNode> best = results
                .filter(r -> r.path("matchConfidence").has("score"))
                .max(Comparator.comparingDouble(r ->
                        r.path("matchConfidence").path("score").asDouble(Double.NEGATIVE_INFINITY)));

        var node = best.orElseThrow(() -> new LocationNotFoundException(
                messageSource.getMessage("errors.location.not_found",
                        new Object[]{address.toString()},
                        LocaleContextHolder.getLocale())
        ));

        double confidence = node.path("matchConfidence").path("score").asDouble();
        double lat = node.path("position").path("lat").asDouble();
        double lon = node.path("position").path("lon").asDouble();

        if (confidence < MINIMUM_CONFIDENCE) {
            log.warn("No matches satisfying condition: 'confidence >= {}'found for address: \"{}\". Best confidence: {}",
                    MINIMUM_CONFIDENCE,
                    address.toString(),
                    confidence);
            throw new LocationNotFoundException(
                    messageSource.getMessage("errors.location.low_confidence",
                            new Object[]{address.toString()},
                            LocaleContextHolder.getLocale())
            );
        }
        var geoFactory = new GeometryFactory(new PrecisionModel(), 4326);
        return geoFactory.createPoint(new Coordinate(
                lon, lat
        ));
    }

    private Geometry createRadius(Point point, Integer radius) {
        GeometricShapeFactory shapeFactory = new GeometricShapeFactory(new GeometryFactory(new PrecisionModel(), 4326));
        shapeFactory.setNumPoints(32);
        shapeFactory.setCentre(point.getCoordinate());
        shapeFactory.setSize(radius * 2);
        return shapeFactory.createCircle();
    }

    @Override
    @Transactional
    public void transferOwnership(Long spotId, User newOwner, User currentUser) throws FishingSpotNotFoundException {
        FishingSpot spot = getFishingSpotById(spotId);

        if (!spot.getOwner().equalsUser(currentUser)
                && currentUser.isAdmin()) {
            throw new AccessDeniedException(
                    messageSource.getMessage("error.spots.no_perms_for_transfer",
                            null,
                            LocaleContextHolder.getLocale()
                    )
            );
        }

        spot.setOwner(newOwner);
        if (!spot.getManagers().contains(newOwner)){
            spot.getManagers().add(newOwner);
        }

        fishingSpotRepository.save(spot);
    }

    @Override
    public Page<FishingSpotDto> getUnverified(Pageable pageable) {
        return fishingSpotRepository.findByVerificationStatus(VerificationStatus.IN_REVIEW, pageable)
                .map(FishingSpotDto::create);
    }

    @Override
    public boolean isOwner(UUID userId, Long fishingSpotId) {
        return fishingSpotRepository.existsByIdAndOwner_Id(fishingSpotId, userId);
    }

    @Override
    public Page<FishingSpotListDto> getUserFavourites(UUID userId, PaginationDto pagination) throws UserNotLoggedInException {
        return fishingSpotRepository.findAllUserFavourites(userId, pagination.toPageable())
                .map(FishingSpotListDto::create);
    }

    @Override
    public Page<FishingSpotListDto> getUserCurrentFavourites(PaginationDto pagination) throws UserNotLoggedInException {
        return fishingSpotRepository.findAllUserFavourites(userService.getCurrentUser().getId(), pagination.toPageable())
                .map(FishingSpotListDto::create);
    }

    @Override
    @Transactional
    public void addFishingSpotToFavourites(Long spotId) throws NotFoundException, UserNotLoggedInException {
        FishingSpot spot = getFishingSpotById(spotId);
        User currentUser = userService.getCurrentUser();

        currentUser.addFavouriteFishingSpot(spot);
        userRepository.save(currentUser);
    }

    @Override
    @Transactional
    public void removeFishingSpotFromFavourites(Long spotId) throws NotFoundException, UserNotLoggedInException {
        FishingSpot spot = getFishingSpotById(spotId);
        User currentUser = userService.getCurrentUser();

        currentUser.removeFavouriteFishingSpot(spot);
        userRepository.save(currentUser);
    }

    @Override
    @Transactional
    public void setFishingSpotPicture(Long spotId, MultipartFile file) throws UserNotLoggedInException, IOException {
        FishingSpot spot = getFishingSpotById(spotId);
        User user = userService.getCurrentUser();

        if (spot.getManagers().stream().noneMatch(u -> u.equalsUser(user)) && !spot.getOwner().equalsUser(user)) {
            log.warn("User with id: {} tried to update picture of a fishing spot with id: {} despite lacking permissions.", user.getId(), spot.getId());
            throw new AccessDeniedException(
                    messageSource.getMessage(
                            "error.users.unauthorized_access",
                            null,
                            LocaleContextHolder.getLocale()
                    )
            );
        }
        String ext = FileProperties.getFileExtension(file.getOriginalFilename());
        File pic = new File(fileProperties.getSpotsPicsFolder(), spot.getId() + ext);
        file.transferTo(pic);

        spot.setPhotoUrl("/spots/pics/" + pic.getName());

        fishingSpotRepository.save(spot);
    }
}
