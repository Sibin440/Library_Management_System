package com.example.opac.service;

import com.example.opac.model.Book;
import com.example.opac.model.Loan;
import com.example.opac.model.User;
import com.example.opac.model.Alert;
import com.example.opac.repository.BookRepository;
import com.example.opac.repository.LoanRepository;
import com.example.opac.repository.AlertRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final EmailService emailService;
    private final AlertRepository alertRepository;

    public LoanService(LoanRepository loanRepository, BookRepository bookRepository, EmailService emailService, AlertRepository alertRepository) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.emailService = emailService;
        this.alertRepository = alertRepository;
    }

    public List<Loan> getLoansByUserId(Long userId) {
        return loanRepository.findByUserId(userId);
    }

    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    public Loan borrowBook(User user, Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ID: " + bookId));
        
        // Optional: check if book is already borrowed by anyone
        // (For simple mock library, we can allow multiple borrows or check status. Let's allow borrowing directly).
        
        Loan loan = new Loan();
        loan.setBook(book);
        loan.setUser(user);
        loan.setLoanDate(LocalDateTime.now());
        loan.setDueDate(LocalDateTime.now().plusDays(14)); // 2 weeks duration
        loan.setStatus("BORROWED");
        
        Loan savedLoan = loanRepository.save(loan);

        // Send email notification asynchronously
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                emailService.sendBorrowConfirmation(
                        user.getEmail(),
                        user.getUsername(),
                        book.getTitle(),
                        savedLoan.getLoanDate(),
                        savedLoan.getDueDate()
                );
            } catch (Exception e) {
                // Ignore or log error
            }
        });

        return savedLoan;
    }

    public Loan requestReturn(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan record not found with ID: " + loanId));
        
        if (!"BORROWED".equals(loan.getStatus())) {
            throw new IllegalStateException("Book is not currently borrowed.");
        }
        
        loan.setStatus("RETURN_REQUESTED");
        return loanRepository.save(loan);
    }

    public Loan approveReturn(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan record not found with ID: " + loanId));
        
        if (!"RETURN_REQUESTED".equals(loan.getStatus())) {
            throw new IllegalStateException("Book return has not been requested.");
        }
        
        loan.setReturnedDate(LocalDateTime.now());
        loan.setStatus("RETURNED");
        
        Loan savedLoan = loanRepository.save(loan);

        // Add in-app Alert for user
        Alert alert = new Alert(
            savedLoan.getUser(),
            "📚 Return accepted! \"" + savedLoan.getBook().getTitle() + "\" has been successfully returned.",
            LocalDateTime.now()
        );
        alertRepository.save(alert);

        // Send email notification asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                emailService.sendReturnConfirmation(
                        savedLoan.getUser().getEmail(),
                        savedLoan.getUser().getUsername(),
                        savedLoan.getBook().getTitle(),
                        savedLoan.getReturnedDate()
                );
            } catch (Exception e) {
                // Ignore or log error
            }
        });

        return savedLoan;
    }
}
