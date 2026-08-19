
package com.example.opac.config;

import com.example.opac.model.Book;
import com.example.opac.repository.BookRepository;
import com.example.opac.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(BookRepository bookRepository) {
        return args -> {
            // Only initialize if the database is empty
            if (bookRepository.count() == 0) {
                System.out.println("Preloading sample books...");
                bookRepository.save(new Book("The Great Gatsby", "F. Scott Fitzgerald", "9780743273565"));
                bookRepository.save(new Book("To Kill a Mockingbird", "Harper Lee", "9780060935467"));
                bookRepository.save(new Book("1984", "George Orwell", "9780451524935"));
                bookRepository.save(new Book("The Hobbit", "J.R.R. Tolkien", "9780547928227"));
                bookRepository.save(new Book("Pride and Prejudice", "Jane Austen", "9780141439518"));
                System.out.println("Sample books loaded successfully!");
            } else {
                System.out.println("Database already contains books. Skipping initialization.");
            }
        };
    }

    @Bean
    CommandLineRunner initUsers(UserService userService) {
        return args -> {
            // create roles and default users if not present
            userService.createAdminIfNotExists("admin", "admin@example.com", "adminpass");
            System.out.println("Ensured admin user exists (username=admin)");
            userService.createUserIfNotExists("user", "user@example.com", "userpass");
            System.out.println("Ensured normal user exists (username=user)");
        };
    }
}