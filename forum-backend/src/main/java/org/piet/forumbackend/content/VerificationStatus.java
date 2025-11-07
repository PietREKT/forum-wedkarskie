package org.piet.forumbackend.content;

import lombok.Getter;

@Getter
public enum VerificationStatus {
    IN_REVIEW(0),
    ACCEPTED(1),
    REJECTED(-1),
    MARKED_FOR_DELETION(2);

    final int verificationStage;

    VerificationStatus(int verificationStage) {
        this.verificationStage = verificationStage;
    }

    public boolean isVerified(){
        return this.verificationStage == 1;
    }

    public boolean isRejected(){
        return this.verificationStage == 0;
    }
}
