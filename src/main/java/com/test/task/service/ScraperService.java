package com.test.task.service;

import com.test.task.dto.BookDto;

import java.util.Set;

public interface ScraperService {
    Set<BookDto> scrapeBooks();
}
