package org.piet.forumbackend.users.groups.repositories;

import org.piet.forumbackend.users.core.entities.User;
import org.piet.forumbackend.users.groups.entities.UserGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface UserGroupRepository extends JpaRepository<UserGroup, UUID> {
    @Query("""
            select distinct ug from UserGroup ug
                                    join ug.members um
                                          where um.id = :userId
                                    order by size(ug.members) desc
            """)
    Page<UserGroup> findAllByMember_Id(@Param("userId") UUID userId, Pageable pageable);

    @Query("""
            select distinct m from UserGroup ug
                        join ug.members m
                        where ug.id = :groupId
            """)
    Page<User> findMemberCandidatesById(@Param("groupId") UUID groupId, Pageable pageable);

    @Query("""
            select distinct ug from UserGroup ug
                        join ug.memberCandidates mc
                                    where mc.id = :userId
            """)
    Page<UserGroup> findAllByMemberCandidatesContainsUser(@Param("userId") UUID userId, Pageable pageable);
}
