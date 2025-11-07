package org.piet.forumbackend.fish;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.piet.forumbackend.exceptions.NotFoundException;
import org.piet.forumbackend.exceptions.UnauthorizedAccessException;
import org.piet.forumbackend.fish.entities.Fish;
import org.piet.forumbackend.fish.entities.FishingMethod;
import org.piet.forumbackend.fish.entities.WaterType;
import org.piet.forumbackend.fishing_spots.exceptions.FishNotFoundException;
import org.piet.forumbackend.properties.FileProperties;
import org.piet.forumbackend.users.entities.Role;
import org.piet.forumbackend.users.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class FishService {

    private final FishRepository fishRepository;
    private final MessageSource messageSource;
    private final FileProperties fileProperties;

    @Value("${forum.files.fish.defaultPhoto}")
    String defaultFishPhotoPath;

    public Fish getFishByName(String name) throws FishNotFoundException {
        return fishRepository.findByName(name.trim().toLowerCase()).orElseThrow(() -> new FishNotFoundException(
                messageSource.getMessage("error.fish.name_not_found",
                        new Object[]{name},
                        LocaleContextHolder.getLocale())
        ));
    }

    public Fish getFishById(Long id) throws FishNotFoundException {
        return fishRepository.findById(id).orElseThrow(() -> new FishNotFoundException(
                messageSource.getMessage("error.fish.id_not_found",
                        new Object[]{id},
                        LocaleContextHolder.getLocale())
        ));
    }

    public List<Fish> getFishByWaterType(WaterType waterType, Pageable pageable) throws NotFoundException {
        List<Fish> fish = fishRepository.findByWaterType(waterType, pageable).getContent();

        if (fish.isEmpty()) throw new NotFoundException(
                messageSource.getMessage("error.fish.water_type_not_found",
                        new Object[]{waterType.name()},
                        LocaleContextHolder.getLocale())
        );

        return fish;
    }

    public List<Fish> getFishByFishingMethods(List<FishingMethod> methods, Pageable pageable) throws NotFoundException {
        List<Fish> fish = fishRepository.findByMethods(methods, pageable).getContent();

        if (fish.isEmpty()) throw new NotFoundException(
                messageSource.getMessage("error.fish.methods_not_found",
                        new Object[]{String.join(", ", methods.stream().map(FishingMethod::name).toList())},
                        LocaleContextHolder.getLocale())
        );

        return fish;
    }

    public void deleteFishByName(String name, User user) throws UnauthorizedAccessException {
        if (!user.hasPermLevelAtLeast(Role.MOD)){
            log.warn("User with id: {} tried to access forbidden resource: \"deleteFishByName\".", user.getId());
            throw new UnauthorizedAccessException(
                    messageSource.getMessage("error.users.unauthorized_access",
                            null,
                            LocaleContextHolder.getLocale())
            );
        }
        log.info("User with id: {} is deleting fish with name: {}", user.getId(), name);
        fishRepository.deleteByName(name);
    }

    public Fish createFish(String name, String description, Float avgLength, Float avgMass, boolean isPredatory, MultipartFile photo, List<FishingMethod> methods, WaterType waterType, User creator) throws IOException {
        if (!creator.hasPermLevelAtLeast(Role.MOD)){
            log.warn("User with ID: {} tried to access forbidden resource: \"createFish\"", creator.getId());
            throw new IllegalAccessError(
                    messageSource.getMessage("error.users.unauthorized_access",
                            null,
                            LocaleContextHolder.getLocale())
            );
        }

        Fish fish = new Fish();
        fish.setName(name.trim().toLowerCase());
        fish.setDescription(description);
        fish.setAvgLength(avgLength);
        fish.setAvgMass(avgMass);
        fish.setMethods(methods);
        fish.setWaterType(waterType);
        fish.setPredatory(isPredatory);
        if (photo == null || photo.getOriginalFilename() == null || !photo.getOriginalFilename().contains(".")) {
            fish.setPhotoUrl("/fish/" + defaultFishPhotoPath);
            log.info("Set default photo url for fish: '{}'", StringUtils.capitalize(fish.getName()));
        } else {
            String ext = photo.getOriginalFilename().substring(photo.getOriginalFilename().lastIndexOf('.'));
            File fishPhoto = new File(fileProperties.getFishFolder(), String.join("_", name.split("\\s+")).trim().toLowerCase() + ext);
            photo.transferTo(fishPhoto);
            fish.setPhotoUrl("/fish/" + fishPhoto.getName());
            log.info("Set custom photo url: \"{}\" for fish '{}'", fish.getPhotoUrl(), StringUtils.capitalize(fish.getName()));
        }
        return fishRepository.save(fish);
    }

    public List<Fish> getFish(Pageable pageable) {
        return fishRepository.findAll(pageable).getContent();
    }

    public List<FishingMethod> getMethods() {
        return Arrays.stream(FishingMethod.values()).toList();
    }

    public List<WaterType> getWaterTypes(){
        return Arrays.stream(WaterType.values()).toList();
    }
}
