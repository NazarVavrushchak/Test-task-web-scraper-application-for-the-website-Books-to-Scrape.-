package com.test.task.controller;

import com.test.task.dto.BookDto;
import com.test.task.service.ScraperService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/books")
@AllArgsConstructor
public class ScraperController {
    private final ScraperService scraperService;

    @GetMapping("/scrape")
    public List<BookDto> infoAboutBooks() {
        return List.copyOf(scraperService.scrapeBooks());
    }
}
