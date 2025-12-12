package org.piet.forumbackend.content.core.entities.enums;

public enum VoteType {
    UPVOTE(1),
    NO_VOTE(0),
    DOWNVOTE(-1);

    private final Integer value;

    VoteType(Integer value) {
        this.value = value;
    }

    public Integer getValue(){
        return this.value;
    }

    public VoteType getOpposite(){
        switch (this.name().toLowerCase()){
            case "upvote" -> {
                return DOWNVOTE;
            }
            case "downvote" -> {
                return UPVOTE;
            }
            default -> {
                return NO_VOTE;
            }
        }
    }
}
