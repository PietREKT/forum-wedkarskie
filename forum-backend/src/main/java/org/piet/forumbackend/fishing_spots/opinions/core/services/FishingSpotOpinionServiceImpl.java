package org.piet.forumbackend.fishing_spots.opinions.core.services;

import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.fishing_spots.core.entities.FishingSpot;
import org.piet.forumbackend.fishing_spots.core.exceptions.FishingSpotNotFoundException;
import org.piet.forumbackend.fishing_spots.core.services.FishingSpotService;
import org.piet.forumbackend.fishing_spots.opinions.core.dtos.FishingSpotOpinionDtoMapper;
import org.piet.forumbackend.fishing_spots.opinions.core.dtos.requests.CreateFishingSpotOpinionDto;
import org.piet.forumbackend.fishing_spots.opinions.core.dtos.requests.EditFishingSpotOpinionDto;
import org.piet.forumbackend.fishing_spots.opinions.core.dtos.responses.FishingSpotOpinionDto;
import org.piet.forumbackend.fishing_spots.opinions.core.entities.FishingSpotOpinion;
import org.piet.forumbackend.fishing_spots.opinions.core.repositories.FishingSpotOpinionRepository;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.users.core.entities.User;
import org.piet.forumbackend.users.core.exceptions.UserNotLoggedInException;
import org.piet.forumbackend.users.core.services.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class FishingSpotOpinionServiceImpl implements FishingSpotOpinionService {
    private final UserService userService;
    private final FishingSpotService fishingSpotService;
    private final FishingSpotOpinionRepository fishingSpotOpinionRepository;

    @Override
    public FishingSpotOpinion getById(Long id) throws NotFoundException {
        return fishingSpotOpinionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Fishing spot opinion with id: " + id + " was not found"));
    }

    @Override
    public FishingSpotOpinion createOpinion(CreateFishingSpotOpinionDto dto) throws UserNotLoggedInException, FishingSpotNotFoundException {
        User user = userService.getCurrentUser();
        FishingSpotOpinion opinion = new FishingSpotOpinion();
        FishingSpot spot = fishingSpotService.getFishingSpotById(dto.getSpotId());
        opinion.setCreatedAt(Instant.now());
        opinion.setAuthor(user);
        opinion.setComment(dto.getComment());
        opinion.setRating(dto.getRating());
        opinion.setSpot(spot);
        spot.addOpinion(opinion);
        return fishingSpotOpinionRepository.save(opinion);
    }

    @Override
    public void editOpinion(Long opinionId, EditFishingSpotOpinionDto dto) throws NotFoundException {
        var opinion = getById(opinionId);
        if (dto.getComment() != null){
            opinion.setComment(dto.getComment());
        }
        if (dto.getRating() != null){
            opinion.setRating(dto.getRating());
        }
        fishingSpotOpinionRepository.save(opinion);
    }

    @Override
    public void deleteOpinion(Long opinionId) throws UserNotLoggedInException, AccessDeniedException {
            try {
                User user = userService.getCurrentUser();
                var opinion = getById(opinionId);
                if (!opinion.getAuthor().equalsUser(user) && !user.isMod()){
                    throw new AccessDeniedException("You can't delete other people's opinions!");
                }
                fishingSpotOpinionRepository.delete(opinion);
            } catch (NotFoundException ignored){}
    }

    @Override
    public Page<FishingSpotOpinionDto> getOpinionsBySpotId(Long spotId, Pageable pageable) {
        return fishingSpotOpinionRepository.findAllBySpot_Id(spotId, pageable)
                .map(FishingSpotOpinionDtoMapper::toFishingSpotOpinionDto);
    }
}
