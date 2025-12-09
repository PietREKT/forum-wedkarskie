package org.piet.forumbackend.globals.utils;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class UserMessagesDateTimeFormatter {
    public static final DateTimeFormatter BAN_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
            .withZone(ZoneId.systemDefault());
}
