package org.piet.forumbackend.fishing_spots.opinions.reports.exceptions;

import org.piet.forumbackend.globals.exceptions.NotFoundException;

public class ContentReportNotFoundException extends NotFoundException {
    public ContentReportNotFoundException(String message) {
        super(message);
    }
}
