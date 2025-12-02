package org.piet.forumbackend.content.services;

import org.piet.forumbackend.content.dtos.responses.tutorials.TutorialDto;
import org.piet.forumbackend.content.entities.Tutorial;
import org.piet.forumbackend.fish.entities.Fish;
import org.piet.forumbackend.fish.entities.enums.FishingMethod;
import org.piet.forumbackend.globals.exceptions.BadRequestException;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.globals.exceptions.UnauthorizedAccessException;
import org.piet.forumbackend.globals.pagination.PaginationDto;
import org.piet.forumbackend.users.core.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface TutorialService {
    Tutorial getById(Long id, User currentUser) throws NotFoundException;

    TutorialDto getDtoById(Long id, User currentUser) throws NotFoundException;

    TutorialDto createTutorial(User author, String title, String content, List<MultipartFile> photos) throws UnauthorizedAccessException, IOException;

    void acceptTutorial(Long tutorialId, User currentUser) throws AccessDeniedException, NotFoundException, BadRequestException;


    void rejectTutorial(Long tutorialId, String rejectionReason, User currentUser) throws AccessDeniedException, NotFoundException, BadRequestException;


    Page<TutorialDto> getTutorialsByMethod(FishingMethod method, Pageable pageable);
    default Page<TutorialDto> getTutorialsByMethod(FishingMethod method, PaginationDto pagination){
        return getTutorialsByMethod(method, pagination.toPageable());
    }

    Page<TutorialDto> getTutorialsByFish(Fish fish, Pageable pageable);
    default Page<TutorialDto> getTutorialsByFish(Fish fish, PaginationDto pagination){
        return getTutorialsByFish(fish, pagination.toPageable());
    }

    Page<TutorialDto> getTutorialsUnverified(Pageable pageable);
    default Page<TutorialDto> getTutorialsUnverified(PaginationDto pagination){
        return getTutorialsUnverified(pagination.toPageable(Sort.by(Sort.Direction.ASC,
                "createdAt")));
    }

    void deleteTutorial(Long tutorialId, User currentUser) throws org.springframework.security.access.AccessDeniedException;
}
