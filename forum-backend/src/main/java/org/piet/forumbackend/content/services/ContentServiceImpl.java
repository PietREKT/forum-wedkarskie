package org.piet.forumbackend.content.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.piet.forumbackend.content.dtos.ContentDtoMapper;
import org.piet.forumbackend.content.dtos.responses.content.ContentDto;
import org.piet.forumbackend.content.dtos.responses.content.ParentContentDto;
import org.piet.forumbackend.content.entities.Content;
import org.piet.forumbackend.content.entities.ContentVote;
import org.piet.forumbackend.content.entities.enums.ContentType;
import org.piet.forumbackend.content.entities.enums.VoteType;
import org.piet.forumbackend.content.repositories.ContentRepository;
import org.piet.forumbackend.globals.exceptions.BadRequestException;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.globals.exceptions.UnauthorizedAccessException;
import org.piet.forumbackend.globals.pagination.PageDto;
import org.piet.forumbackend.globals.pagination.PaginationDto;
import org.piet.forumbackend.globals.properties.FileProperties;
import org.piet.forumbackend.users.core.entities.Role;
import org.piet.forumbackend.users.core.entities.User;
import org.piet.forumbackend.users.core.services.UserService;
import org.piet.forumbackend.users.groups.entities.UserGroup;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.time.Instant;
import java.util.*;

@Service
@Log4j2
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {
    private final ContentRepository contentRepository;
    private final MessageSource messageSource;
    private final FileProperties fileProperties;
    private final UserService userService;

    private File getContentFolder(Content content) throws FileSystemException {
        String folderName = content.getContentType().name() + '-' + content.getId();
        File contentFolder = new File(fileProperties.getContentFolder(),
                folderName);
        if (!contentFolder.exists() && !contentFolder.mkdir()) {
            throw new FileSystemException(
                    messageSource.getMessage("error.content.create_folder",
                            new Object[]{folderName},
                            LocaleContextHolder.getLocale())
            );
        }
        return contentFolder;
    }

    @Override
    public Content getContentById(Long id) throws NotFoundException {
        return contentRepository.findById(id).orElseThrow(() -> new NotFoundException(
                messageSource.getMessage("errors.content.not_found",
                        new Object[]{id},
                        LocaleContextHolder.getLocale())
        ));
    }

    @Override
    public Content getContentByIdOrNull(Long id) {
        if (id == null) return null;
        return contentRepository.findById(id).orElse(null);
    }

    @Override
    public Content getContentByIdOrNull(ParentContentDto parent) {
        if (parent != null)
            return getContentByIdOrNull(parent.getId());
        return null;
    }

    @Override
    public ContentDto getContentDtoById(Long id) {
        return contentRepository.findById(id)
                .map(c ->
                        ContentDtoMapper.toContentDto(
                                c,
                                c.getVotes(),
                                userService.getCurrentUserOrNull()
                        )
                )
                .orElse(null);
    }

    @Override
    public Page<ContentDto> getRecentPostsByGroup(UUID groupId, PaginationDto pagination) {
        return contentRepository.findAllByGroup_Id(groupId,
                pagination.toPageable(Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(c -> ContentDtoMapper.toContentDto(c, c.getVotes(), userService.getCurrentUserOrNull()));
    }

    @Override
    public Content createContent(User author, String content, ContentType type, Content parent, UserGroup group, List<MultipartFile> photos) throws BadRequestException, UnauthorizedAccessException, IOException {
        userService.checkIsMuted(author);

        Content c = new Content();
        c.setAuthor(author);
        c.setContent(content);
        if (group != null) {
            c.setGroup(group);
        }
        if (photos == null)
            photos = new ArrayList<>();
        if (type == ContentType.COMMENT) {
            if (photos.size() > 1) {
                throw new BadRequestException(
                        messageSource.getMessage("error.content.too_many_photos",
                                new Object[]{photos.size()},
                                LocaleContextHolder.getLocale())
                );
            }
            if (parent == null) {
                throw new BadRequestException(
                        messageSource.getMessage("error.content.no_parent_for_comment",
                                null,
                                LocaleContextHolder.getLocale())
                );
            }
            parent.addChild(c);
        }
        c.setContentType(type);
        contentRepository.save(c);
        for (MultipartFile p : photos) {
            savePhotoToContent(c, p, author);
        }
        return c;
    }

    private void updateContentEditHistory(Content content, String newContent) {
        Map<Instant, String> editHistory = content.getEditHistory();
        editHistory.put(Instant.now(), newContent);
        content.setEditHistory(editHistory);
    }

    @Override
    public Content editContent(User currentUser, Long contentId, String newContent, List<String> attachmentsToKeep, List<MultipartFile> photos) throws NotFoundException, BadRequestException, UnauthorizedAccessException, IOException {
        userService.checkIsMuted(currentUser);
        Content c = getContentById(contentId);
        if (!c.getAuthor().equalsUser(currentUser)) {
            throw new UnauthorizedAccessException(
                    messageSource.getMessage("errors.content.no_perms_for_edit",
                            null,
                            LocaleContextHolder.getLocale())
            );
        }

        c.getAttachedPhotos()
                .stream()
                .filter(url -> !attachmentsToKeep.contains(url))
                .toList()
                .forEach(c::removeAttachmentUrl);
        if (!newContent.equals(c.getContent())) {
            updateContentEditHistory(c, newContent);
            c.setContent(newContent);
        }
        contentRepository.save(c);

        for (var photo : (photos != null ? photos : new ArrayList<MultipartFile>())) {
            savePhotoToContent(c, photo, currentUser);
        }
        return c;
    }

    @Override
    public PageDto<ContentDto> getContentByParent(Long parentId, User currentUser, Pageable pageable) throws NotFoundException {
        Content c = getContentById(parentId);
        return getContentByParent(c, currentUser, pageable);
    }

    @Override
    public PageDto<ContentDto> getContentByParent(Content parent, User currentUser, Pageable pageable) {
        Page<Content> contentPage = contentRepository.findByParent(parent, pageable);
        if (currentUser != null) {
            return PageDto.of(contentPage.map(c -> ContentDtoMapper.toContentDto(c,
                            c.getVotes(),
                            currentUser
                    )
            ));
        }
        return PageDto.of(contentPage.map(ContentDtoMapper::toContentDto));
    }

    @Override
    public PageDto<ContentDto> getRecentPosts(PaginationDto pagination, User currentUser) {
        Pageable pageable = pagination.toPageable(Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Content> contentPage = contentRepository.findByContentType(ContentType.POST, pageable);
        return PageDto.of(contentPage.map(c ->
                        ContentDtoMapper.toContentDto(
                                c,
                                c.getVotes(),
                                currentUser
                        )
                )
        );
    }

    @Override
    public void deleteContent(Content content, User currentUser) throws UnauthorizedAccessException {
        if (!(content.getAuthor().equalsUser(currentUser) || currentUser.hasPermLevelAtLeast(Role.MOD))) {
            throw new UnauthorizedAccessException(
                    messageSource.getMessage("error.content.no_perms_for_deletion",
                            null,
                            LocaleContextHolder.getLocale())
            );
        }
        String contentLog = content.toLogString();
        contentRepository.delete(content);
        log.info("User with id: {} deleted content: {}", currentUser.getId(), contentLog);
    }

    @Override
    public void vote(Content content, User user, VoteType vote) {

        var existingOpt = content.getVotes()
                .stream()
                .filter(v -> v.getUser().equalsUser(user))
                .findFirst();

        if (existingOpt.isPresent()) {
            ContentVote existing = existingOpt.get();
            if (existing.getVote() == vote) {
                log.info("Removing content vote: {} under content with id: {}. Voting user's id: {}",
                        existing.toLogString(),
                        content.getId(),
                        user.getId()
                );
                content.getVotes().remove(existing);
            } else {
                log.info("Changed content vote under content with id: {} from type: {} to :{}. Voting user's id: {}",
                        content.getId(),
                        existing.getVote(),
                        vote,
                        user.getId()
                );
                existing.setVote(vote);
            }
        } else {
            ContentVote contentVote = new ContentVote();
            contentVote.setVote(vote);
            contentVote.setUser(user);
            contentVote.setContent(content);
            content.getVotes().add(contentVote);
        }
        contentRepository.save(content);
    }

    @Override
    public void savePhotoToContent(Content content, MultipartFile photo, User user) throws IOException, UnauthorizedAccessException {
        File contentFolder = getContentFolder(content);
        if (!content.getAuthor().equalsUser(user)) {
            throw new UnauthorizedAccessException(
                    messageSource.getMessage("error.users.unauthorized_access",
                            null,
                            LocaleContextHolder.getLocale())
            );
        }
        int fileIndex = contentFolder.listFiles() != null ? contentFolder.listFiles().length + 1 : 0;
        File file = new File(contentFolder,
                fileIndex + FileProperties.getFileExtension(photo.getOriginalFilename()));
        photo.transferTo(file);
        log.info(file.getName());
        String url = fileProperties.getContentFolderUploadsUrl() + contentFolder.getName() +
                '/' +
                file.getName();
        content.addAttachmentUrl(
                url);
        contentRepository.save(content);
    }

    @Override
    public void deletePhotoFromContent(Content content, String filename, User user) throws FileSystemException, UnauthorizedAccessException {
        File contentFolder = getContentFolder(content);

        if (!content.getAuthor().equalsUser(user)) {
            throw new UnauthorizedAccessException(
                    messageSource.getMessage("error.users.unauthorized_access",
                            null,
                            LocaleContextHolder.getLocale())
            );
        }

        Arrays.stream(contentFolder.listFiles())
                .filter(f -> f.getName().equals(filename))
                .forEach(f -> {
                    content.removeAttachmentUrl(
                            fileProperties.getContentFolderUploadsUrl() +
                                    '/' + contentFolder.getName() +
                                    '/' + filename);
                    f.delete();
                });
    }

    @Override
    public void deleteContentFolder(Content content) {
        try {
            File folder = getContentFolder(content);
            if (folder.exists()) {
                FileSystemUtils.deleteRecursively(folder);
            }
        } catch (FileSystemException e) {
            log.warn("Failed to delete content folder for: {}", content.toLogString(), e);
        }
    }

    @Override
    public Page<ContentDto> getUserPosts(User user, PaginationDto pagination) {
        User currentUser = userService.getCurrentUserOrNull();
        return contentRepository.findByContentTypeAndAuthor(ContentType.POST,
                user,
                pagination.toPageable(Sort.by(Sort.Direction.DESC, "createdAt"))
        ).map(c -> ContentDtoMapper.toContentDto(c, c.getVotes(), currentUser));

    }
}
