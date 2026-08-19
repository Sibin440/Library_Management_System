package com.example.opac.service;

import com.example.opac.model.Alert;
import com.example.opac.model.Loan;
import com.example.opac.repository.AlertRepository;
import com.example.opac.repository.LoanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Scheduled service that checks for loans approaching their due date
 * and sends email reminders + in-app alerts to the borrowers.
 */
@Service
public class DueDateReminderService {

    private static final Logger log = LoggerFactory.getLogger(DueDateReminderService.class);

    private final LoanRepository loanRepository;
    private final AlertRepository alertRepository;
    private final EmailService emailService;

    @Value("${opac.reminder.days-before-due}")
    private int daysBeforeDue;

    public DueDateReminderService(LoanRepository loanRepository,
                                   AlertRepository alertRepository,
                                   EmailService emailService) {
        this.loanRepository = loanRepository;
        this.alertRepository = alertRepository;
        this.emailService = emailService;
    }

    /**
     * Scheduled task that runs on a configurable cron schedule.
     * Finds all BORROWED loans with due dates within the reminder window
     * and sends email + in-app notifications.
     */
    @Scheduled(cron = "${opac.reminder.cron}")
    public void sendDueDateReminders() {
        log.info("🔔 Running due-date reminder check...");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowEnd = now.plusDays(daysBeforeDue);

        // Find active loans due within the window that haven't been reminded yet
        List<Loan> upcomingDueLoans = loanRepository
                .findByStatusAndReminderSentFalseAndDueDateBetween("BORROWED", now, windowEnd);

        if (upcomingDueLoans.isEmpty()) {
            log.info("✅ No loans approaching due date. Nothing to do.");
            return;
        }

        log.info("📋 Found {} loan(s) approaching due date.", upcomingDueLoans.size());

        for (Loan loan : upcomingDueLoans) {
            try {
                String username = loan.getUser().getUsername();
                String email = loan.getUser().getEmail();
                String bookTitle = loan.getBook().getTitle();
                LocalDateTime dueDate = loan.getDueDate();

                long daysLeft = ChronoUnit.DAYS.between(now, dueDate);

                // 1. Send email notification
                emailService.sendDueDateReminder(email, username, bookTitle, dueDate);

                // 2. Create in-app alert
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy (EEEE)");
                String alertMessage = String.format(
                        "📚 Reminder: \"%s\" is due on %s (%d day(s) remaining). Please return it on time to avoid late fees.",
                        bookTitle, dueDate.format(formatter), daysLeft
                );
                Alert alert = new Alert(loan.getUser(), alertMessage, LocalDateTime.now());
                alertRepository.save(alert);

                // 3. Mark reminder as sent to avoid duplicates
                loan.setReminderSent(true);
                loanRepository.save(loan);

                log.info("📧 Reminder sent to {} for \"{}\" (due: {})",
                        username, bookTitle, dueDate.format(formatter));
            } catch (Exception e) {
                log.error("❌ Failed to send reminder for loan ID {}: {}",
                        loan.getId(), e.getMessage(), e);
            }
        }

        log.info("🔔 Due-date reminder check completed.");
    }
}
