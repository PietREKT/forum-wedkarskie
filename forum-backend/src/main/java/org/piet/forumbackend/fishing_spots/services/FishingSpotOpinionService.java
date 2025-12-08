package org.piet.forumbackend.fishing_spots.services;

import org.piet.forumbackend.fishing_spots.dtos.requests.opinions.CreateFishingSpotOpinionDto;
import org.piet.forumbackend.fishing_spots.dtos.requests.opinions.EditFishingSpotOpinionDto;
import org.piet.forumbackend.fishing_spots.dtos.responses.FishingSpotOpinionDto;
import org.piet.forumbackend.fishing_spots.entities.FishingSpotOpinion;
import org.piet.forumbackend.fishing_spots.exceptions.FishingSpotNotFoundException;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.users.core.exceptions.UserNotLoggedInException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FishingSpotOpinionService {
    FishingSpotOpinion getById(Long id) throws NotFoundException;

    FishingSpotOpinion createOpinion(CreateFishingSpotOpinionDto dto) throws UserNotLoggedInException, FishingSpotNotFoundException;

    void editOpinion(Long opinionId, EditFishingSpotOpinionDto dto) throws NotFoundException;

    void deleteOpinion(Long opinionId) throws UserNotLoggedInException;

    Page<FishingSpotOpinionDto> getOpinionsBySpotId(Long spotId, Pageable pageable);
}
