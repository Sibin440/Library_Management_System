package com.example.opac.controller;

import com.example.opac.model.Loan;
import com.example.opac.model.User;
import com.example.opac.service.LoanService;
import com.example.opac.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;
    private final UserService userService;

    public LoanController(LoanService loanService, UserService userService) {
        this.loanService = loanService;
        this.userService = userService;
    }

    @GetMapping("/my")
    public ResponseEntity<List<Loan>> getMyLoans(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(loanService.getLoansByUserId(user.getId()));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Loan>> getAllLoans() {
        return ResponseEntity.ok(loanService.getAllLoans());
    }

    @PostMapping("/borrow/{bookId}")
    public ResponseEntity<Loan> borrowBook(@PathVariable Long bookId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Loan loan = loanService.borrowBook(user, bookId);
        return ResponseEntity.ok(loan);
    }

    @PostMapping("/return/{loanId}")
    public ResponseEntity<Loan> requestReturn(@PathVariable Long loanId) {
        Loan loan = loanService.requestReturn(loanId);
        return ResponseEntity.ok(loan);
    }

    @PostMapping("/approve/{loanId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Loan> approveReturn(@PathVariable Long loanId) {
        Loan loan = loanService.approveReturn(loanId);
        return ResponseEntity.ok(loan);
    }
}
