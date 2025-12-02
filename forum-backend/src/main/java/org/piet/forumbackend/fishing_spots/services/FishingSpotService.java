package org.piet.forumbackend.fishing_spots.services;

import org.piet.forumbackend.fish.entities.Fish;
import org.piet.forumbackend.fishing_spots.dtos.FishingSpotDto;
import org.piet.forumbackend.fishing_spots.dtos.LocationDto;
import org.piet.forumbackend.fishing_spots.entities.FishingSpot;
import org.piet.forumbackend.fishing_spots.exceptions.FishingSpotNotFoundException;
import org.piet.forumbackend.fishing_spots.exceptions.LocationDtoIncompleteException;
import org.piet.forumbackend.fishing_spots.exceptions.LocationNotFoundException;
import org.piet.forumbackend.globals.exceptions.BadRequestException;
import org.piet.forumbackend.globals.exceptions.UnauthorizedAccessException;
import org.piet.forumbackend.globals.pagination.PaginationDto;
import org.piet.forumbackend.users.core.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FishingSpotService {
    void markFishingSpotAsVerified(FishingSpot fishingSpot);
    void markFishingSpotAsRejected(FishingSpot fishingSpot);

    void deleteFishingSpot(FishingSpot fishingSpot, User currentUser);

    FishingSpot getFishingSpotById(Long id) throws FishingSpotNotFoundException;

    FishingSpot getFishingSpotByName(String name) throws FishingSpotNotFoundException;

    FishingSpot createFishingSpot(String name,
                                         String desc,
                                         FishingSpot.FISHING_SPOT_TYPE type,
                                         List<User> managers,
                                         List<Fish> fish,
                                         LocationDto location,
                                         User sentBy)
            throws LocationDtoIncompleteException,
            IOException,
            LocationNotFoundException,
            UnauthorizedAccessException,
            BadRequestException;

    FishingSpot updateStatue(FishingSpot fishingSpot, MultipartFile newStatue, User user) throws BadRequestException, IOException, UnauthorizedAccessException;

    List<FishingSpot> getFishingSpotsInRadius(Double x, Double y, Integer radiusKm, Pageable pageable);

    Page<FishingSpot> getFishingSpots(Pageable pageable);
    default Page<FishingSpot> getFishingSpots(PaginationDto dto){
        return getFishingSpots(dto.toPageable());
    };

    default void deleteFishingSpot(Long id, User currentUser) throws FishingSpotNotFoundException {
        var fs = getFishingSpotById(id);
        deleteFishingSpot(fs, currentUser);
    }

    default void markFishingSpotAsVerified(Long id) throws FishingSpotNotFoundException {
        FishingSpot fs = getFishingSpotById(id);
        markFishingSpotAsVerified(fs);
    }
    default void markFishingSpotAsRejected(Long id) throws FishingSpotNotFoundException {
        FishingSpot fs = getFishingSpotById(id);
        markFishingSpotAsRejected(fs);
    }

    void transferOwnership(Long spotId, User newOwner, User currentOwner) throws FishingSpotNotFoundException;

    Page<FishingSpotDto> getUnverified(Pageable pageable);
    default Page<FishingSpotDto> getUnverified(PaginationDto pagination){
        return getUnverified(pagination.toPageable(Sort.by(Sort.Direction.ASC, "createdAt")));
    }
}


