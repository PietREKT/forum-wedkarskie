package org.piet.forumbackend.content.services;

import org.piet.forumbackend.content.dtos.responses.content.ContentDto;
import org.piet.forumbackend.content.dtos.responses.content.ParentContentDto;
import org.piet.forumbackend.content.entities.Content;
import org.piet.forumbackend.content.entities.enums.ContentType;
import org.piet.forumbackend.content.entities.enums.VoteType;
import org.piet.forumbackend.globals.exceptions.BadRequestException;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.globals.exceptions.UnauthorizedAccessException;
import org.piet.forumbackend.globals.pagination.PageDto;
import org.piet.forumbackend.globals.pagination.PaginationDto;
import org.piet.forumbackend.users.core.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.util.List;


public interface ContentService {

    public Content getContentById(Long id) throws NotFoundException;

    public Content getContentByIdOrNull(Long id);

    public Content getContentByIdOrNull(ParentContentDto parent);

    public Content createContent(User author, String content, ContentType type, Content parent, List<MultipartFile> photos) throws BadRequestException, UnauthorizedAccessException, IOException;

    public Content editContent(User currentUser, Long contentId, String newContent, List<String> alreadyAttached, List<MultipartFile> photos) throws NotFoundException, BadRequestException, UnauthorizedAccessException, IOException;

    public PageDto<ContentDto> getContentByParent(Long parentId, User currentUser, Pageable pageable) throws NotFoundException;

    public PageDto<ContentDto> getContentByParent(Content parent, User currentUser, Pageable pageable);

    public PageDto<ContentDto> getRecentPosts(PaginationDto pagination, User currentUser);

    default void deleteContent(Long id, User currentUser) throws UnauthorizedAccessException, NotFoundException {
        Content c = getContentById(id);
        deleteContent(c, currentUser);
    }

    public void deleteContent(Content content, User currentUser) throws UnauthorizedAccessException;

    public void vote(Content content, User user, VoteType vote);

    public void savePhotoToContent(Content content, MultipartFile file, User user) throws IOException, UnauthorizedAccessException;

    public void deletePhotoFromContent(Content content, String filename, User user) throws FileSystemException, UnauthorizedAccessException;

    public void deleteContentFolder(Content content) throws UnauthorizedAccessException;

    public Page<Content> getUserPosts(User user, PaginationDto pagination);

}
