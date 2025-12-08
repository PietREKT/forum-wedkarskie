package org.piet.forumbackend.events.entites;

public enum AttendanceStatus {
        INVITED(0),
        MAYBE(1),
        CONFIRMED(2);

        private final int statusValue;

    AttendanceStatus(int statusValue) {
        this.statusValue = statusValue;
    }

    public int getStatusValue(){
        return statusValue;
    };


}
