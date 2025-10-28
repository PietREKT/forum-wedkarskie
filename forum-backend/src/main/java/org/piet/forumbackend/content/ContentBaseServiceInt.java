package org.piet.forumbackend.content;

import org.piet.forumbackend.users.entities.User;

public interface ContentBaseServiceInt<T extends ContentBase> {
    public T getContentById(Long id) throws Exception;

    public void deleteContent(Long contentId, User user) throws Exception;

    public T editContent(User currentUser, Long contentId, String newContent) throws Exception;

    public void upvote(Long contentId) throws Exception;

    public void downvote(Long contentId) throws Exception;
}
