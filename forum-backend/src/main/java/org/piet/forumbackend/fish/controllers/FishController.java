package org.piet.forumbackend.fish.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.exceptions.BadRequestException;
import org.piet.forumbackend.exceptions.NotFoundException;
import org.piet.forumbackend.fish.FishService;
import org.piet.forumbackend.fish.dtos.FishDto;
import org.piet.forumbackend.fish.dtos.FishDtoMapper;
import org.piet.forumbackend.fish.dtos.GetFishDto;
import org.piet.forumbackend.fish.entities.FishingMethod;
import org.piet.forumbackend.fish.entities.WaterType;
import org.piet.forumbackend.pagination.PaginationDto;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.context.MessageSource;
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
    private final FishService fishService;
    private final MessageSource messageSource;

    @GetMapping
    public ResponseEntity<List<FishDto>> getFish(@ParameterObject GetFishDto fishDto, @ParameterObject PaginationDto paginationDto) throws NotFoundException, BadRequestException {
        Pageable pageable = PageRequest.of(paginationDto.getPage(), paginationDto.getSize());
        if (fishDto.getId() != null) {
            return ResponseEntity.ok(List.of(
                    FishDtoMapper.toFishDto(fishService.getFishById(fishDto.getId()))
            ));
        } else if (fishDto.getName() != null) {
            return ResponseEntity.ok(List.of(
                    FishDtoMapper.toFishDto(fishService.getFishByName(fishDto.getName()))
            ));
        } else if (fishDto.getWaterType() != null) {
            return ResponseEntity.ok(
                    fishService.getFishByWaterType(fishDto.getWaterType(), pageable)
                            .stream()
                            .map(FishDtoMapper::toFishDto)
                            .toList()
            );
        } else if (fishDto.getMethods() != null && !fishDto.getMethods().isEmpty()) {
            return ResponseEntity.ok(
                    fishService.getFishByFishingMethods(fishDto.getMethods(), pageable)
                            .stream()
                            .map(FishDtoMapper::toFishDto)
                            .toList()
            );
        } else {
            return ResponseEntity.ok(fishService.getFish(pageable)
                    .stream()
                    .map(FishDtoMapper::toFishDto)
                    .toList()
            );
        }
    }

    @GetMapping("/methods")
    public ResponseEntity<List<FishingMethod>> getFishingMethods(){
        return ResponseEntity.ok(fishService.getMethods());
    }

    @GetMapping("/water-types")
    public ResponseEntity<List<WaterType>> getWaterTypes(){
        return ResponseEntity.ok(fishService.getWaterTypes());
    }
}
