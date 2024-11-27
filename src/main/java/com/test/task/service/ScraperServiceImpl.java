package com.test.task.service;

import com.test.task.dto.BookDto;
import com.test.task.entity.Book;
import com.test.task.repo.BookRepo;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@Transactional
@Slf4j
public class ScraperServiceImpl implements ScraperService {
    @Value("${scraper.url.books}")
    private String booksUrl;

    private final BookRepo bookRepo;
    private final ModelMapper modelMapper;

    private static final Map<String, Integer> RATING_MAP = Map.of(
            "one", 1, "two", 2, "three", 3, "four", 4, "five", 5);

    public ScraperServiceImpl(BookRepo bookRepo, ModelMapper modelMapper) {
        this.bookRepo = bookRepo;
        this.modelMapper = modelMapper;
    }

    @Override
    public Set<BookDto> scrapeBooks() {
        Set<BookDto> scrapedBooks = new HashSet<>();
        int page = 1;

        while (true) {
            String url = booksUrl + "catalogue/page-" + page + ".html";
            Document document;
            if (!hasNextPage(url)) {
                log.info("No more pages to scrape. Stopping at page {}", page);
                break;
            }
            try {
                document = Jsoup.connect(url).get();
                Elements bookElements = document.select(".product_pod");
                if (bookElements.isEmpty()) {
                    break;
                }

                List<Book> booksToSave = getBooksToSave(bookElements);
                bookRepo.saveAll(booksToSave);
                scrapedBooks.addAll(booksToSave.stream().map(e -> modelMapper.map(e, BookDto.class)).toList());
                page++;
            } catch (IOException e) {
                log.error("Error while scraping books", e);
                break;
            }
        }

        log.info("Scraping completed. Total books scraped: {}", scrapedBooks.size());
        return scrapedBooks;
    }

    @SneakyThrows
    public boolean hasNextPage(String url) {
        try {
            Jsoup.connect(url).get();
            return true;
        } catch (HttpStatusException e) {
            return false;
        }
    }

    private List<Book> getBooksToSave(Elements bookElements) {
        return bookElements.stream()
                .map(bookElement -> {
                    String title = bookElement.select("h3 > a").attr("title");
                    String priceText = bookElement.select(".price_color").text().replace("£", "");
                    String ratingClass = bookElement.select(".star-rating").attr("class");
                    boolean isAvailable = bookElement.select(".instock.availability").text().contains("In stock");

                    if (title.isEmpty() || priceText.isEmpty() || ratingClass.isEmpty()) {
                        return null;
                    }

                    BigDecimal price = new BigDecimal(priceText).setScale(2, RoundingMode.HALF_UP);
                    int rating = getRatingFromClass(ratingClass);

                    return Book.builder()
                            .title(title)
                            .price(price)
                            .isAvailable(isAvailable)
                            .rating(rating)
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private int getRatingFromClass(String ratingClass) {
        String key = ratingClass.replace("star-rating", "").trim().toLowerCase();
        return RATING_MAP.getOrDefault(key, 0);
    }
}
