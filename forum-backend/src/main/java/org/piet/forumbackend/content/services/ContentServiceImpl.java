package org.piet.forumbackend.content.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.piet.forumbackend.content.dtos.ContentDto;
import org.piet.forumbackend.content.dtos.ContentDtoMapper;
import org.piet.forumbackend.content.dtos.ParentContentDto;
import org.piet.forumbackend.content.entities.Content;
import org.piet.forumbackend.content.entities.ContentVote;
import org.piet.forumbackend.content.entities.enums.ContentType;
import org.piet.forumbackend.content.entities.enums.VoteType;
import org.piet.forumbackend.content.repositories.ContentRepository;
import org.piet.forumbackend.content.repositories.ContentVoteRepository;
import org.piet.forumbackend.exceptions.BadRequestException;
import org.piet.forumbackend.exceptions.NotFoundException;
import org.piet.forumbackend.exceptions.UnauthorizedAccessException;
import org.piet.forumbackend.pagination.PageDto;
import org.piet.forumbackend.properties.FileProperties;
import org.piet.forumbackend.users.entities.Role;
import org.piet.forumbackend.users.entities.User;
import org.piet.forumbackend.users.repos.UserRepository;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@Log4j2
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {
    private final ContentVoteRepository contentVoteRepository;
    private final UserRepository userRepository;
    private final ContentRepository contentRepository;
    private final MessageSource messageSource;
    private final FileProperties fileProperties;

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
    public Content createContent(User author, String content, ContentType type, Content parent, List<MultipartFile> photos) throws BadRequestException, UnauthorizedAccessException, IOException {
        Content c = new Content();
        c.setAuthor(author);
        c.setContent(content);
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
            c.setParent(parent);
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
        Content c = getContentById(contentId);
        if (!c.getAuthor().equals(currentUser)) {
            throw new UnauthorizedAccessException(
                    messageSource.getMessage("errors.content.no_perms_for_edit",
                            null,
                            LocaleContextHolder.getLocale())
            );
        }

        c.getAttachedPhotos()
                .stream()
                .filter(url -> !attachmentsToKeep.contains(url))
                .forEach(c::removeAttachmentUrl);
        if (!newContent.equals(c.getContent())) {
            updateContentEditHistory(c, newContent);
            c.setContent(newContent);
        }
        contentRepository.save(c);

        for (var photo : photos) {
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
            return PageDto.createDto(contentPage.map(c -> ContentDtoMapper.toContentDto(c,
                    c.getVotes().stream()
                            .filter(v -> v.getUser().equals(currentUser))
                            .findFirst()
                            .map(ContentVote::getVote)
                            .orElse(VoteType.NO_VOTE)
            )
            ));
        }
        return PageDto.createDto(contentPage.map(ContentDtoMapper::toContentDto));
    }

    @Override
    public PageDto<ContentDto> getRecentPosts(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Content> contentPage = contentRepository.findByContentType(ContentType.POST, pageable);
        return PageDto.createDto(contentPage.map(ContentDtoMapper::toContentDto));
    }

    @Override
    public void deleteContent(Long id, User currentUser) throws UnauthorizedAccessException {
        try {
            Content c = getContentById(id);
            deleteContent(c, currentUser);
        } catch (NotFoundException e) {
            log.info("User with id: {} tried to delete content with id: {} but it wasn't found in database.",
                    currentUser.getId(), id);
        }
    }

    @Override
    public void deleteContent(Content content, User currentUser) throws UnauthorizedAccessException {
        if (!(content.getAuthor().equals(currentUser) || currentUser.hasPermLevelAtLeast(Role.MOD))) {
            throw new UnauthorizedAccessException(
                    messageSource.getMessage("error.content.no_perms_for_deletion",
                            null,
                            LocaleContextHolder.getLocale())
            );
        }
        contentRepository.delete(content);
        log.info("User with id: {} deleted content: {}", currentUser.getId(), content.toLogString());
    }

    @Override
    public void vote(Content content, User user, VoteType vote) {
        if (content.getVotes().stream().anyMatch(c -> c.getUser().equals(user))) {
            content.getVotes()
                    .stream()
                    .takeWhile(c -> c.getUser().equals(user))
                    .forEach(c -> {
                        log.info("Removed content vote: {}", c.toLogString());
                    });
            return;
        }
        ContentVote contentVote = new ContentVote();
        contentVote.setVote(vote);
        contentVote.setUser(user);
        contentVote.setContent(content);
        contentVoteRepository.save(contentVote);
    }

    @Override
    public void savePhotoToContent(Content content, MultipartFile photo, User user) throws IOException, UnauthorizedAccessException {
        File contentFolder = getContentFolder(content);
        if (!content.getAuthor().equals(user)) {
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
        content.addAttachmentUrl(
                fileProperties.getContentFolderUploadsUrl() +
                        '/' + contentFolder.getName() +
                        '/' + file.getName());
        contentRepository.save(content);
    }

    @Override
    public void deletePhotoFromContent(Content content, String filename, User user) throws FileSystemException, UnauthorizedAccessException {
        File contentFolder = getContentFolder(content);

        if (!content.getAuthor().equals(user)) {
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
    public void deleteContentFolder(Content content) throws FileSystemException {
        getContentFolder(content).delete();
    }
}
