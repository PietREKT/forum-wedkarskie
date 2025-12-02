package org.piet.forumbackend.content.services;

import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.content.dtos.TutorialDtoMapper;
import org.piet.forumbackend.content.dtos.responses.tutorials.TutorialDto;
import org.piet.forumbackend.content.entities.Tutorial;
import org.piet.forumbackend.content.entities.enums.VerificationStatus;
import org.piet.forumbackend.content.repositories.TutorialRepository;
import org.piet.forumbackend.fish.entities.Fish;
import org.piet.forumbackend.fish.entities.enums.FishingMethod;
import org.piet.forumbackend.globals.exceptions.BadRequestException;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.globals.exceptions.UnauthorizedAccessException;
import org.piet.forumbackend.users.core.entities.Role;
import org.piet.forumbackend.users.core.entities.User;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TutorialServiceImpl implements TutorialService {
    private final ContentService contentService;
    private final TutorialRepository tutorialRepository;
    private final MessageSource messageSource;

    private void canAccept(Tutorial tutorial, User currentUser) throws NotFoundException, BadRequestException {
        if (!currentUser.getRole().hasAtLeast(Role.ADMIN)) {
            throw new AccessDeniedException(
                    messageSource.getMessage("error.admins.tutorials.accept",
                            null,
                            LocaleContextHolder.getLocale())
            );
        }
        if (tutorial.getAuthor().equalsUser(currentUser)) {
            throw new BadRequestException(
                    messageSource.getMessage("error.content.tutorials.self_accept",
                            null,
                            LocaleContextHolder.getLocale()
                    )
            );
        }
    }


    @Override
    public Tutorial getById(Long id, User currentUser) throws NotFoundException {
        Tutorial t = tutorialRepository.findById(id).orElseThrow(() -> new NotFoundException(
                messageSource.getMessage("error.content.tutorials.not_found",
                        new Object[]{id},
                        LocaleContextHolder.getLocale()
                )
        ));

        if (!t.getVerificationStatus().isVerified()
                && !(t.getAuthor().equalsUser(currentUser)
                || currentUser.hasPermLevelAtLeast(Role.ADMIN))) {
            throw new AccessDeniedException(
                    messageSource.getMessage("error.content.tutorials.get_not_verified",
                            null,
                            LocaleContextHolder.getLocale())
            );
        }

        return t;
    }

    @Override
    @Transactional
    public TutorialDto createTutorial(User author, String title, String content, List<MultipartFile> photos) throws UnauthorizedAccessException, IOException {
        Tutorial tutorial = new Tutorial();
        tutorial.setAuthor(author);
        tutorial.setContent(content);
        tutorial.setTitle(title);
        tutorialRepository.save(tutorial);

        for (var photo : photos) {
            contentService.savePhotoToContent(tutorial, photo, author);
        }
        return TutorialDtoMapper.toTutorialDto(tutorial);
    }

    @Override
    @Transactional
    public void acceptTutorial(Long tutorialId, User currentUser) throws AccessDeniedException, NotFoundException, BadRequestException {
        Tutorial tutorial = getById(tutorialId, currentUser);
        canAccept(tutorial, currentUser);
        tutorial.setVerificationStatus(VerificationStatus.ACCEPTED);
        tutorial.setVerifiedBy(currentUser);
    }

    @Override
    @Transactional
    public void rejectTutorial(Long tutorialId, String rejectionReason, User currentUser) throws AccessDeniedException, NotFoundException, BadRequestException {
        Tutorial tutorial = getById(tutorialId, currentUser);
        canAccept(tutorial, currentUser);
        tutorial.setVerificationStatus(VerificationStatus.REJECTED);
        tutorial.setRejectionReason(rejectionReason);
        tutorial.setVerifiedBy(currentUser);
    }

    @Override
    public TutorialDto getDtoById(Long id, User currentUser) throws NotFoundException {
        return TutorialDtoMapper.toTutorialDto(getById(id, currentUser));
    }

    @Override
    public Page<TutorialDto> getTutorialsByMethod(FishingMethod method, Pageable pageable) {
        return tutorialRepository.findByMethod(method, pageable).map(TutorialDtoMapper::toTutorialDto);
    }

    @Override
    public Page<TutorialDto> getTutorialsByFish(Fish fish, Pageable pageable) {
        return tutorialRepository.findByFish(fish, pageable).map(TutorialDtoMapper::toTutorialDto);
    }

    @Override
    public void deleteTutorial(Long tutorialId, User currentUser) throws AccessDeniedException {
        try {
            Tutorial t = getById(tutorialId, currentUser);
            if (!currentUser.isAdmin() && !t.getAuthor().equalsUser(currentUser)) {
                throw new AccessDeniedException(
                        messageSource.getMessage("error.content.tutorials.no_perms_for_delete",
                                null,
                                LocaleContextHolder.getLocale()
                        )
                );
            }
            tutorialRepository.delete(t);
        } catch (NotFoundException ignored) {
        }
    }

    @Override
    public Page<TutorialDto> getTutorialsUnverified(Pageable pageable) {
        return tutorialRepository.findByVerificationStatus(VerificationStatus.IN_REVIEW, pageable)
                .map(TutorialDtoMapper::toTutorialDto);
    }
}
