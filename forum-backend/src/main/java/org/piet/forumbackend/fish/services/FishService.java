package org.piet.forumbackend.fish.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.piet.forumbackend.fish.dtos.FishDto;
import org.piet.forumbackend.fish.dtos.FishDtoMapper;
import org.piet.forumbackend.fish.dtos.FishListDto;
import org.piet.forumbackend.fish.entities.Fish;
import org.piet.forumbackend.fish.entities.enums.FishingMethod;
import org.piet.forumbackend.fish.entities.enums.WaterType;
import org.piet.forumbackend.fish.repositories.FishRepository;
import org.piet.forumbackend.fishing_spots.exceptions.FishNotFoundException;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.globals.exceptions.UnauthorizedAccessException;
import org.piet.forumbackend.globals.pagination.PageDto;
import org.piet.forumbackend.globals.properties.FileProperties;
import org.piet.forumbackend.users.core.entities.Role;
import org.piet.forumbackend.users.core.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
public class FishService {

    private final FishRepository fishRepository;
    private final MessageSource messageSource;
    private final FileProperties fileProperties;

    @Value("${forum.files.fish.defaultPhoto}")
    String defaultFishPhotoPath;

    public boolean fishExistsByName(String name){
        return fishRepository.existsByName(name);
    }

    public List<FishListDto> getFishByName(String query) {
        if (query == null) return List.of();

        String q = query.trim();

        if (q.length() < 2) return List.of();

        return fishRepository.findTop10ByNameStartingWithIgnoreCaseOrderByNameAsc(q)
                .stream().map(FishDtoMapper::toFishListDto).toList();
    }

    public Optional<Fish> getFishByIdOptional(Long id){
        return fishRepository.findById(id);
    }

    public Fish getFishById(Long id) throws FishNotFoundException {
        return fishRepository.findById(id).orElseThrow(() -> new FishNotFoundException(
                messageSource.getMessage("error.fish.id_not_found",
                        new Object[]{id},
                        LocaleContextHolder.getLocale())
        ));
    }
    public FishDto getFishDtoById(Long id) throws FishNotFoundException {
        Fish fish = fishRepository.findById(id).orElseThrow(() -> new FishNotFoundException(
                messageSource.getMessage("error.fish.id_not_found",
                        new Object[]{id},
                        LocaleContextHolder.getLocale())
        ));

        return FishDtoMapper.toFishDto(fish);
    }

    public PageDto<FishListDto> getFishByWaterType(WaterType waterType, Pageable pageable) throws NotFoundException {
        Page<Fish> fish = fishRepository.findByWaterType(waterType, pageable);

        if (fish.isEmpty()) throw new NotFoundException(
                messageSource.getMessage("error.fish.water_type_not_found",
                        new Object[]{waterType.name()},
                        LocaleContextHolder.getLocale())
        );

        return PageDto.of(fish.map(FishListDto::create));
    }

    public PageDto<FishListDto> getFishByFishingMethods(List<FishingMethod> methods, Pageable pageable) throws NotFoundException {
        Page<Fish> fish = fishRepository.findByMethods(methods, pageable);

        if (fish.isEmpty()) throw new NotFoundException(
                messageSource.getMessage("error.fish.methods_not_found",
                        new Object[]{String.join(", ", methods.stream().map(FishingMethod::name).toList())},
                        LocaleContextHolder.getLocale())
        );

        return PageDto.of(fish.map(FishListDto::create));
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

    public Optional<Fish> getFishByNameOptional(String name) {
        return fishRepository.findByName(name);
    }
}
