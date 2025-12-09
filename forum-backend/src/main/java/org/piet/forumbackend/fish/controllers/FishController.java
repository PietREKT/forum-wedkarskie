package org.piet.forumbackend.fish.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.fish.dtos.FishDto;
import org.piet.forumbackend.fish.dtos.FishListDto;
import org.piet.forumbackend.fish.entities.enums.FishingMethod;
import org.piet.forumbackend.fish.entities.enums.WaterType;
import org.piet.forumbackend.fish.services.FishService;
import org.piet.forumbackend.fishing_spots.exceptions.FishNotFoundException;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.globals.pagination.PageDto;
import org.piet.forumbackend.globals.pagination.PaginationDto;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${forum.api.prefix}/fish")
@Tag(name = "Fish", description = "Endpoints for fish management.")
@RequiredArgsConstructor
public class FishController {
    private final FishService fishService;
    private final MessageSource messageSource;

    @GetMapping("/search")
    public ResponseEntity<List<FishListDto>> getFishByName(@RequestParam(name = "name") String query){
        var dtos = fishService.getFishByName(query);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FishDto> getFishById(@PathVariable Long id) throws FishNotFoundException {
        var dto = fishService.getFishDtoById(id);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/methods")
    public ResponseEntity<PageDto<FishListDto>> getFishByMethods(@RequestParam(name = "method") List<FishingMethod> methods, PaginationDto pagination) throws NotFoundException {
        var dtos = fishService.getFishByFishingMethods(methods, pagination.toPageable());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/water-type")
    public ResponseEntity<PageDto<FishListDto>> getFishByWaterTypes(@RequestParam(name = "type") WaterType type, PaginationDto pagination) throws NotFoundException {
        var dtos = fishService.getFishByWaterType(type, pagination.toPageable());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/enums/methods")
    public ResponseEntity<List<FishingMethod>> getFishingMethods(){
        return ResponseEntity.ok(fishService.getMethods());
    }

    @GetMapping("/enums/water-types")
    public ResponseEntity<List<WaterType>> getWaterTypes(){
        return ResponseEntity.ok(fishService.getWaterTypes());
    }
}
