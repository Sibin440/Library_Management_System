package com.example.opac.controller;

import com.example.opac.model.Book;
import com.example.opac.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*") // Added CORS support
public class BookController {
    
    private static final Logger logger = LoggerFactory.getLogger(BookController.class);
    private final BookRepository bookRepository;
    
    public BookController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
        logger.info("BookController initialized with repository: {}", bookRepository);
    }
    
    @GetMapping
    public List<Book> getAllBooks() {
        logger.info("Request received to get all books");
        List<Book> books = bookRepository.findAll();
        logger.info("Retrieved {} books from database", books.size());
        return books;
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ResponseEntity<Book> addBook(@RequestBody Book book) {
        logger.info("Request received to add a new book: {}", book);
        Book savedBook = bookRepository.save(book);
        logger.info("Book saved successfully with ID: {}", savedBook.getId());
        return new ResponseEntity<>(savedBook, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        logger.info("Request received to get book with ID: {}", id);
        return bookRepository.findById(id)
                .map(book -> {
                    logger.info("Book found: {}", book);
                    return new ResponseEntity<>(book, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody Book updatedBook) {
        logger.info("Request received to update book with ID: {}", id);
        return bookRepository.findById(id)
                .map(book -> {
                    book.setTitle(updatedBook.getTitle());
                    book.setAuthor(updatedBook.getAuthor());
                    book.setIsbn(updatedBook.getIsbn());
                    Book saved = bookRepository.save(book);
                    logger.info("Book updated successfully: {}", saved);
                    return new ResponseEntity<>(saved, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        logger.info("Request received to delete book with ID: {}", id);
        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
            logger.info("Book deleted successfully");
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        logger.warn("Book with ID {} not found for deletion", id);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    
    // Added search functionality
    @GetMapping("/search")
    public List<Book> searchBooks(@RequestParam String query) {
        logger.info("Request received to search books with query: {}", query);
        List<Book> results = bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(query, query);
        logger.info("Found {} books matching the search query", results.size());
        return results;
    }
}