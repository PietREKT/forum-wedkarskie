package org.piet.forumbackend.content.core.entities.enums;

import lombok.Getter;

@Getter
public enum VerificationStatus {
    IN_REVIEW(0),
    ACCEPTED(1),
    REJECTED(-1);

    final int verificationStage;

    VerificationStatus(int verificationStage) {
        this.verificationStage = verificationStage;
    }

    public boolean isVerified(){
        return this.verificationStage == 1;
    }

    public boolean isRejected(){
        return this.verificationStage == -1;
    }
}
