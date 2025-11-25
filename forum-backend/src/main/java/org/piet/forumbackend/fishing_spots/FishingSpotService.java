package org.piet.forumbackend.fishing_spots;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.util.GeometricShapeFactory;
import org.piet.forumbackend.content.entities.enums.VerificationStatus;
import org.piet.forumbackend.exceptions.BadRequestException;
import org.piet.forumbackend.exceptions.UnauthorizedAccessException;
import org.piet.forumbackend.fish.entities.Fish;
import org.piet.forumbackend.fishing_spots.dtos.AddressDto;
import org.piet.forumbackend.fishing_spots.dtos.LocationDto;
import org.piet.forumbackend.fishing_spots.exceptions.FishingSpotNotFoundException;
import org.piet.forumbackend.fishing_spots.exceptions.LocationDtoIncompleteException;
import org.piet.forumbackend.fishing_spots.exceptions.LocationNotFoundException;
import org.piet.forumbackend.properties.FileProperties;
import org.piet.forumbackend.users.entities.Role;
import org.piet.forumbackend.users.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Log4j2
public class FishingSpotService {
    private final FishingSpotRepository fishingSpotRepository;
    private final MessageSource messageSource;
    private final FileProperties fileProperties;

    @Value("${forum.api.tomtom_key}")
    private String TOM_TOM_API_KEY;

    @Value("${forum.api.tomtom_minimum_precision}")
    private Double MINIMUM_CONFIDENCE;

    private FishingSpot markFishingSpotAsVerified(FishingSpot fishingSpot) {
        fishingSpot.setVerificationStatus(VerificationStatus.ACCEPTED);
        return fishingSpotRepository.save(fishingSpot);
    }

    private void markFishingSpotForDeletion(FishingSpot fishingSpot) {
        fishingSpot.setVerificationStatus(VerificationStatus.MARKED_FOR_DELETION);
        fishingSpotRepository.save(fishingSpot);
    }

    private void deleteFishingSpot(FishingSpot fishingSpot) {
        fishingSpotRepository.delete(fishingSpot);
    }

    public FishingSpot getFishingSpotById(Long id) throws FishingSpotNotFoundException {
        return fishingSpotRepository.findById(id).orElseThrow(() -> new FishingSpotNotFoundException(
                messageSource.getMessage("errors.spots.id_not_found",
                        new Object[]{id},
                        LocaleContextHolder.getLocale())
        ));
    }

    public FishingSpot getFishingSpotByName(String name) throws FishingSpotNotFoundException {
        return fishingSpotRepository.findByNameIgnoreCase(name.trim()).orElseThrow(() -> new FishingSpotNotFoundException(
                messageSource.getMessage("errors.spots.name_not_found",
                        new Object[]{name},
                        LocaleContextHolder.getLocale())
        ));
    }

    private Point getPointByAddress(AddressDto address) throws JsonProcessingException, LocationNotFoundException {
        var geoFactory = new GeometryFactory();

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

        // Stream over "results" array
        Stream<JsonNode> results = StreamSupport.stream(root.path("results").spliterator(), false);

        // Find entry with max confidence
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
            log.warn("No matches satisfying condition: 'confidence >= {}'found for address: \"{}\". Best confidence: {}", MINIMUM_CONFIDENCE, address.toString(), confidence);
            throw new LocationNotFoundException(
                    messageSource.getMessage("errors.location.low_confidence",
                            new Object[]{address.toString()},
                            LocaleContextHolder.getLocale())
            );
        }
        log.info("Best match for address \"{}\": Lat: '{}' Lon: '{}'", address.toString(), lat, lon);
        return geoFactory.createPoint(new Coordinate(
                lon, lat
        ));
    }

    //name desc type managers fish
    public FishingSpot createFishingSpot(String name, String desc, FishingSpot.FISHING_SPOT_TYPE type, List<User> managers, List<Fish> fish, LocationDto location, User sentBy) throws LocationDtoIncompleteException, IOException, LocationNotFoundException, UnauthorizedAccessException, BadRequestException {
        FishingSpot spot = new FishingSpot();
        if (managers == null){
            managers = new ArrayList<>();
        }
        managers.add(sentBy);

        if (type == FishingSpot.FISHING_SPOT_TYPE.PUBLIC && !sentBy.hasPermLevelAtLeast(Role.PZW)) {
            log.warn("User with ID: {} tried to access forbidden resource: \"createFishingSpot\" - PUBLIC type", sentBy.getId());
            throw new UnauthorizedAccessException(
                    messageSource.getMessage("error.users.unauthorized_access",
                            null,
                            LocaleContextHolder.getLocale())
            );
        }

        if (type == FishingSpot.FISHING_SPOT_TYPE.PUBLIC && !managers.stream().allMatch(u -> u.hasPermLevelAtLeast(Role.PZW))) {
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
        spot.setName(name);
        spot.setDescription(desc);
        spot.setManagers(managers);

//        if (statue != null && !statue.isEmpty()) {
//            return updateStatue(spot, statue, sentBy);
//        }
        return fishingSpotRepository.save(spot);
    }

    public void markFishingSpotForDeletion(String name) throws FishingSpotNotFoundException {
        var fs = getFishingSpotByName(name);
        markFishingSpotForDeletion(fs);
    }

    public void markFishingSpotForDeletion(Long id) throws FishingSpotNotFoundException {
        var fs = getFishingSpotById(id);
        markFishingSpotForDeletion(fs);
    }

    private void deleteFishingSpot(String name) throws FishingSpotNotFoundException {
        var fs = getFishingSpotByName(name);
        deleteFishingSpot(fs);
    }

    public void deleteFishingSpot(Long id) throws FishingSpotNotFoundException {
        var fs = getFishingSpotById(id);
        deleteFishingSpot(fs);
    }

    public FishingSpot markFishingSpotAsVerified(String name) throws FishingSpotNotFoundException {
        FishingSpot fs = getFishingSpotByName(name);
        return markFishingSpotAsVerified(fs);
    }

    public FishingSpot markFishingSpotAsVerified(Long id) throws FishingSpotNotFoundException {
        FishingSpot fs = getFishingSpotById(id);
        return markFishingSpotAsVerified(fs);
    }

    public FishingSpot updateStatue(FishingSpot fishingSpot, MultipartFile newStatue, User user) throws BadRequestException, IOException, UnauthorizedAccessException {
        if (fishingSpot.getManagers().stream().noneMatch(u -> u.equals(user))) {
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
        File fishingSpotFolder = new File(fileProperties.getSpotsFolder(), "spot-" + fishingSpot.getId());
        String fileExtension = FileProperties.getFileExtension(newStatue.getOriginalFilename());
        File statue = new File(fishingSpotFolder, "statue" + fileExtension);
        newStatue.transferTo(statue);

        fishingSpot.setStatuteUrl("/spots/spot-" + fishingSpot.getId() + File.separator + statue.getName() + fileExtension);
        log.info("User with id: {} updated a statue for fishing spot with id: {}", user.getId(), fishingSpot.getId());
        return fishingSpotRepository.save(fishingSpot);
    }

    private Geometry createRadius(Point point, Integer radius){
        GeometricShapeFactory shapeFactory = new GeometricShapeFactory(new GeometryFactory(new PrecisionModel(), 4326));
        shapeFactory.setNumPoints(32);
        shapeFactory.setCentre(point.getCoordinate());
        shapeFactory.setSize(radius*2);
        return shapeFactory.createCircle();
    }

    public List<FishingSpot> getFishingSpotsInRadius(Double x, Double y, Integer radiusKm, Pageable pageable){
        Point centre = new GeometryFactory(new PrecisionModel(), 4326).createPoint(new Coordinate(x, y));
        Geometry radius = createRadius(centre, radiusKm);
        return fishingSpotRepository.findByLocation(radius, pageable).getContent()
                .stream()
                .sorted(Comparator.comparing(spot -> spot.getLocation().distance(centre)))
                .toList();
    }
}
