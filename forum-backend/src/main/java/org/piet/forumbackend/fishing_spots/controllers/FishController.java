package org.piet.forumbackend.fishing_spots.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.exceptions.BadRequestException;
import org.piet.forumbackend.exceptions.NotFoundException;
import org.piet.forumbackend.fishing_spots.dtos.FishDto;
import org.piet.forumbackend.fishing_spots.dtos.FishingSpotsDtoMapper;
import org.piet.forumbackend.fishing_spots.dtos.GetFishDto;
import org.piet.forumbackend.fishing_spots.services.FishService;
import org.piet.forumbackend.pagination.PaginationDto;
import org.piet.forumbackend.users.UserService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${forum.api.prefix}/fish")
@Tag(name = "Fish", description = "Endpoints for fish management.")
@RequiredArgsConstructor
public class FishController {
    private final UserService userService;
    private final FishService fishService;
    private final MessageSource messageSource;

    @GetMapping
    public ResponseEntity<List<FishDto>> getFish(@ParameterObject GetFishDto fishDto, @ParameterObject PaginationDto paginationDto) throws NotFoundException, BadRequestException {
        Pageable pageable = PageRequest.of(paginationDto.getPage(), paginationDto.getSize());
        if (fishDto.getId() != null) {
            return ResponseEntity.ok(List.of(
                    FishingSpotsDtoMapper.toFishDto(fishService.getFishById(fishDto.getId()))
            ));
        } else if (fishDto.getName() != null){
            return ResponseEntity.ok(List.of(
                    FishingSpotsDtoMapper.toFishDto(fishService.getFishByName(fishDto.getName()))
            ));
        } else if (fishDto.getWaterType() != null){
            return ResponseEntity.ok(
                    fishService.getFishByWaterType(fishDto.getWaterType(), pageable)
                            .stream()
                            .map(FishingSpotsDtoMapper::toFishDto)
                            .toList()
            );
        } else if (fishDto.getMethods() != null && !fishDto.getMethods().isEmpty()){
            return ResponseEntity.ok(
                fishService.getFishByFishingMethods(fishDto.getMethods(), pageable)
                        .stream()
                        .map(FishingSpotsDtoMapper::toFishDto)
                        .toList()
            );
        }

        throw new BadRequestException(
                messageSource.getMessage("error.fish.bad_get_params",
                        new Object[]{fishDto.toString()},
                        LocaleContextHolder.getLocale())
        );
    }
}
