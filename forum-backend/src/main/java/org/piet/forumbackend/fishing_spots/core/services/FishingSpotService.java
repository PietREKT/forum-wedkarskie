package org.piet.forumbackend.fishing_spots.core.services;

import org.piet.forumbackend.content.core.entities.enums.VerificationStatus;
import org.piet.forumbackend.fishing_spots.core.dtos.requests.CreateFishingSpotDto;
import org.piet.forumbackend.fishing_spots.core.dtos.responses.FishingSpotDto;
import org.piet.forumbackend.fishing_spots.core.dtos.responses.FishingSpotListDto;
import org.piet.forumbackend.fishing_spots.core.entities.FishingSpot;
import org.piet.forumbackend.fishing_spots.core.exceptions.FishingSpotNotFoundException;
import org.piet.forumbackend.fishing_spots.core.exceptions.LocationDtoIncompleteException;
import org.piet.forumbackend.fishing_spots.core.exceptions.LocationNotFoundException;
import org.piet.forumbackend.globals.exceptions.BadRequestException;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.globals.exceptions.UnauthorizedAccessException;
import org.piet.forumbackend.globals.pagination.PaginationDto;
import org.piet.forumbackend.users.core.entities.User;
import org.piet.forumbackend.users.core.exceptions.UserNotLoggedInException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface FishingSpotService {
    void markFishingSpotAsVerified(FishingSpot fishingSpot);
    void markFishingSpotAsRejected(FishingSpot fishingSpot);
    void revokeFishingSpotReview(Long spotId) throws FishingSpotNotFoundException;

    void deleteFishingSpot(FishingSpot fishingSpot, User currentUser);

    FishingSpot getFishingSpotById(Long id) throws FishingSpotNotFoundException;

    List<FishingSpotListDto> getFishingSpotByName(String name);

    FishingSpot createFishingSpot(CreateFishingSpotDto dto)
            throws LocationDtoIncompleteException,
            IOException,
            LocationNotFoundException,
            UnauthorizedAccessException,
            BadRequestException, UserNotLoggedInException;

    FishingSpot updateStatue(FishingSpot fishingSpot, MultipartFile newStatue, User user) throws BadRequestException, IOException, UnauthorizedAccessException;

    List<FishingSpot> getFishingSpotsInRadius(Double x, Double y, Integer radiusKm, Pageable pageable);

    Page<FishingSpot> getFishingSpotsByStatus(VerificationStatus status, Pageable pageable);
    default Page<FishingSpotListDto> getFishingSpotDtosByStatus(VerificationStatus status, Pageable pageable){
        return getFishingSpotsByStatus(status, pageable).map(FishingSpotListDto::create);
    }

    default Page<FishingSpot> getFishingSpotsByStatus(VerificationStatus status, PaginationDto dto){
        return getFishingSpotsByStatus(status, dto.toPageable());
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

    boolean isOwner(UUID userId, Long fishingSpotId);

    Page<FishingSpotListDto> getUserFavourites(UUID userId, PaginationDto pagination) throws UserNotLoggedInException;
    Page<FishingSpotListDto> getUserCurrentFavourites(PaginationDto pagination) throws UserNotLoggedInException;

    void addFishingSpotToFavourites(Long spotId) throws NotFoundException, UserNotLoggedInException;
    void removeFishingSpotFromFavourites(Long spotId) throws NotFoundException, UserNotLoggedInException;

    void setFishingSpotPicture(Long spotId, MultipartFile file) throws UserNotLoggedInException, UnauthorizedAccessException, IOException;
}


