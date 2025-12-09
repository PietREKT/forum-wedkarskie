package org.piet.forumbackend.fishing_spots.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.fishing_spots.dtos.FishingSpotOpinionDtoMapper;
import org.piet.forumbackend.fishing_spots.dtos.requests.opinions.CreateFishingSpotOpinionDto;
import org.piet.forumbackend.fishing_spots.dtos.requests.opinions.EditFishingSpotOpinionDto;
import org.piet.forumbackend.fishing_spots.dtos.responses.FishingSpotOpinionDto;
import org.piet.forumbackend.fishing_spots.exceptions.FishingSpotNotFoundException;
import org.piet.forumbackend.fishing_spots.services.FishingSpotOpinionService;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.globals.pagination.PageDto;
import org.piet.forumbackend.globals.pagination.PaginationDto;
import org.piet.forumbackend.users.core.exceptions.UserNotLoggedInException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${forum.api.prefix}/spots/opinions")
@RequiredArgsConstructor
@Tag(name = "Opinion Controller", description = "Endpoints for fishing spot opinion crud")
public class FishingSpotOpinionController {
    private final FishingSpotOpinionService fishingSpotOpinionService;

    @GetMapping("/{spotId}")
    public ResponseEntity<PageDto<FishingSpotOpinionDto>> getOpinionsOnPost(@PathVariable Long spotId, PaginationDto pagination){
        var page = fishingSpotOpinionService.getOpinionsBySpotId(spotId,
                pagination.toPageable(Sort.by(Sort.Direction.DESC,"createdAt")));

        return ResponseEntity.ok(PageDto.of(page));
    }

    @PostMapping
    public ResponseEntity<FishingSpotOpinionDto> createOpinion(@Valid @RequestBody CreateFishingSpotOpinionDto dto) throws UserNotLoggedInException, FishingSpotNotFoundException {
        FishingSpotOpinionDto returnDto = FishingSpotOpinionDtoMapper.toFishingSpotOpinionDto(
                fishingSpotOpinionService.createOpinion(dto)
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(returnDto);
    }

    @DeleteMapping("/{opinionId}")
    public ResponseEntity<?> delete(@PathVariable Long opinionId) throws UserNotLoggedInException {
        fishingSpotOpinionService.deleteOpinion(opinionId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{opinionId}")
    public ResponseEntity<?> edit(@PathVariable Long opinionId, @Valid @RequestBody EditFishingSpotOpinionDto dto) throws NotFoundException {
        fishingSpotOpinionService.editOpinion(opinionId, dto);
        return ResponseEntity.noContent().build();
    }
}
