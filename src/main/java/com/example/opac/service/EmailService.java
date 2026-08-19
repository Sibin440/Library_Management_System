package com.example.opac.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Service for sending email notifications via Gmail SMTP.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${opac.reminder.from-name}")
    private String fromName;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send a due-date reminder email to the user.
     *
     * @param toEmail   recipient email address
     * @param username  recipient's display name / username
     * @param bookTitle title of the borrowed book
     * @param dueDate   the loan's due date
     */
    public void sendDueDateReminder(String toEmail, String username, String bookTitle, LocalDateTime dueDate) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);

            long daysLeft = ChronoUnit.DAYS.between(LocalDateTime.now(), dueDate);
            String urgency = daysLeft <= 0 ? "TODAY" : daysLeft + " day(s)";

            helper.setSubject("📚 Library Reminder: \"" + bookTitle + "\" is due in " + urgency);
            helper.setText(buildHtmlBody(username, bookTitle, dueDate, daysLeft), true);

            mailSender.send(message);
            log.info("✅ Due-date reminder sent to {} for book \"{}\"", toEmail, bookTitle);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("❌ Failed to send reminder email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    /**
     * Build a rich HTML email body for the due-date reminder.
     */
    private String buildHtmlBody(String username, String bookTitle, LocalDateTime dueDate, long daysLeft) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE");

        String formattedDate = dueDate.format(dateFormatter);
        String dayOfWeek = dueDate.format(dayFormatter);

        String urgencyColor = daysLeft <= 0 ? "#e74c3c" : daysLeft == 1 ? "#f39c12" : "#3498db";
        String urgencyText = daysLeft <= 0 ? "Due TODAY!" : "Due in " + daysLeft + " day(s)";

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                </head>
                <body style="margin:0;padding:0;font-family:'Segoe UI',Arial,sans-serif;background:#f4f6f9;">
                    <div style="max-width:600px;margin:30px auto;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.08);">

                        <!-- Header -->
                        <div style="background:linear-gradient(135deg,#2c3e50,#3498db);padding:30px 32px;text-align:center;">
                            <h1 style="color:#ffffff;margin:0;font-size:24px;">📚 OPAC Library System</h1>
                            <p style="color:rgba(255,255,255,0.8);margin:6px 0 0;font-size:14px;">Book Return Reminder</p>
                        </div>

                        <!-- Body -->
                        <div style="padding:32px;">
                            <p style="color:#333;font-size:16px;margin:0 0 20px;">
                                Hello <strong>%s</strong>,
                            </p>

                            <p style="color:#555;font-size:15px;line-height:1.6;margin:0 0 24px;">
                                This is a friendly reminder that the following book you borrowed is due for return soon:
                            </p>

                            <!-- Book Card -->
                            <div style="background:#f8fafc;border:1px solid #e2e8f0;border-radius:10px;padding:20px;margin:0 0 24px;">
                                <table width="100%%" cellpadding="0" cellspacing="0" border="0">
                                    <tr>
                                        <td style="padding:6px 0;">
                                            <span style="color:#94a3b8;font-size:12px;text-transform:uppercase;letter-spacing:1px;">Book Title</span><br>
                                            <span style="color:#1e293b;font-size:17px;font-weight:600;">%s</span>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:12px 0 6px;">
                                            <span style="color:#94a3b8;font-size:12px;text-transform:uppercase;letter-spacing:1px;">Due Date</span><br>
                                            <span style="color:#1e293b;font-size:17px;font-weight:600;">%s (%s)</span>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:12px 0 0;">
                                            <span style="display:inline-block;background:%s;color:#ffffff;padding:6px 16px;border-radius:20px;font-size:13px;font-weight:600;">
                                                ⏰ %s
                                            </span>
                                        </td>
                                    </tr>
                                </table>
                            </div>

                            <p style="color:#555;font-size:14px;line-height:1.6;margin:0 0 8px;">
                                Please return the book on or before the due date to avoid any late fees
                                (₹1 per day overdue).
                            </p>
                        </div>

                        <!-- Footer -->
                        <div style="background:#f8fafc;border-top:1px solid #e2e8f0;padding:20px 32px;text-align:center;">
                            <p style="color:#94a3b8;font-size:12px;margin:0;">
                                This is an automated notification from OPAC Library Management System.<br>
                                Please do not reply to this email.
                            </p>
                        </div>

                    </div>
                </body>
                </html>
                """.formatted(username, bookTitle, formattedDate, dayOfWeek, urgencyColor, urgencyText);
    }

    /**
     * Send a borrow confirmation email to the user.
     *
     * @param toEmail   recipient email address
     * @param username  recipient's display name / username
     * @param bookTitle title of the borrowed book
     * @param loanDate  the date the book was borrowed
     * @param dueDate   the due date for the book
     */
    public void sendBorrowConfirmation(String toEmail, String username, String bookTitle, LocalDateTime loanDate, LocalDateTime dueDate) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);

            helper.setSubject("📚 Book Borrowed Successfully: \"" + bookTitle + "\"");
            helper.setText(buildBorrowHtmlBody(username, bookTitle, loanDate, dueDate), true);

            mailSender.send(message);
            log.info("✅ Borrow confirmation email sent to {} for book \"{}\"", toEmail, bookTitle);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("❌ Failed to send borrow confirmation email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    /**
     * Build a rich HTML email body for borrow confirmation.
     */
    private String buildBorrowHtmlBody(String username, String bookTitle, LocalDateTime loanDate, LocalDateTime dueDate) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        String formattedLoanDate = loanDate.format(dateFormatter);
        String formattedDueDate = dueDate.format(dateFormatter);

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                </head>
                <body style="margin:0;padding:0;font-family:'Segoe UI',Arial,sans-serif;background:#f4f6f9;">
                    <div style="max-width:600px;margin:30px auto;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.08);">

                        <!-- Header -->
                        <div style="background:linear-gradient(135deg,#2ecc71,#27ae60);padding:30px 32px;text-align:center;">
                            <h1 style="color:#ffffff;margin:0;font-size:24px;">📚 OPAC Library System</h1>
                            <p style="color:rgba(255,255,255,0.8);margin:6px 0 0;font-size:14px;">Book Borrowed Successfully</p>
                        </div>

                        <!-- Body -->
                        <div style="padding:32px;">
                            <p style="color:#333;font-size:16px;margin:0 0 20px;">
                                Hello <strong>%s</strong>,
                            </p>

                            <p style="color:#555;font-size:15px;line-height:1.6;margin:0 0 24px;">
                                You have successfully borrowed the following book from the library:
                            </p>

                            <!-- Book Card -->
                            <div style="background:#f8fafc;border:1px solid #e2e8f0;border-radius:10px;padding:20px;margin:0 0 24px;">
                                <table width="100%%" cellpadding="0" cellspacing="0" border="0">
                                    <tr>
                                        <td style="padding:6px 0;">
                                            <span style="color:#94a3b8;font-size:12px;text-transform:uppercase;letter-spacing:1px;">Book Title</span><br>
                                            <span style="color:#1e293b;font-size:17px;font-weight:600;">%s</span>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:6px 0;">
                                            <span style="color:#94a3b8;font-size:12px;text-transform:uppercase;letter-spacing:1px;">Borrow Date</span><br>
                                            <span style="color:#1e293b;font-size:15px;font-weight:600;">%s</span>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:6px 0;">
                                            <span style="color:#94a3b8;font-size:12px;text-transform:uppercase;letter-spacing:1px;">Due Date</span><br>
                                            <span style="color:#1e293b;font-size:15px;font-weight:600;color:#e74c3c;">%s</span>
                                        </td>
                                    </tr>
                                </table>
                            </div>

                            <p style="color:#555;font-size:14px;line-height:1.6;margin:0 0 8px;">
                                Please make sure to return the book by the due date to avoid overdue fines (₹1 per day).
                                Thank you for using our library services!
                            </p>
                        </div>

                        <!-- Footer -->
                        <div style="background:#f8fafc;border-top:1px solid #e2e8f0;padding:20px 32px;text-align:center;">
                            <p style="color:#94a3b8;font-size:12px;margin:0;">
                                This is an automated notification from OPAC Library Management System.<br>
                                Please do not reply to this email.
                            </p>
                        </div>

                    </div>
                </body>
                </html>
                """.formatted(username, bookTitle, formattedLoanDate, formattedDueDate);
    }

    /**
     * Send a return confirmation email to the user.
     *
     * @param toEmail      recipient email address
     * @param username     recipient's display name / username
     * @param bookTitle    title of the returned book
     * @param returnedDate the date the book was returned
     */
    public void sendReturnConfirmation(String toEmail, String username, String bookTitle, LocalDateTime returnedDate) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);

            helper.setSubject("📚 Book Returned Successfully: \"" + bookTitle + "\"");
            helper.setText(buildReturnHtmlBody(username, bookTitle, returnedDate), true);

            mailSender.send(message);
            log.info("✅ Return confirmation email sent to {} for book \"{}\"", toEmail, bookTitle);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("❌ Failed to send return confirmation email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    /**
     * Build a rich HTML email body for return confirmation.
     */
    private String buildReturnHtmlBody(String username, String bookTitle, LocalDateTime returnedDate) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        String formattedReturnDate = returnedDate.format(dateFormatter);

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                </head>
                <body style="margin:0;padding:0;font-family:'Segoe UI',Arial,sans-serif;background:#f4f6f9;">
                    <div style="max-width:600px;margin:30px auto;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.08);">

                        <!-- Header -->
                        <div style="background:linear-gradient(135deg,#3498db,#2980b9);padding:30px 32px;text-align:center;">
                            <h1 style="color:#ffffff;margin:0;font-size:24px;">📚 OPAC Library System</h1>
                            <p style="color:rgba(255,255,255,0.8);margin:6px 0 0;font-size:14px;">Book Returned Successfully</p>
                        </div>

                        <!-- Body -->
                        <div style="padding:32px;">
                            <p style="color:#333;font-size:16px;margin:0 0 20px;">
                                Hello <strong>%s</strong>,
                            </p>

                            <p style="color:#555;font-size:15px;line-height:1.6;margin:0 0 24px;">
                                You have successfully returned the following book to the library:
                            </p>

                            <!-- Book Card -->
                            <div style="background:#f8fafc;border:1px solid #e2e8f0;border-radius:10px;padding:20px;margin:0 0 24px;">
                                <table width="100%%" cellpadding="0" cellspacing="0" border="0">
                                    <tr>
                                        <td style="padding:6px 0;">
                                            <span style="color:#94a3b8;font-size:12px;text-transform:uppercase;letter-spacing:1px;">Book Title</span><br>
                                            <span style="color:#1e293b;font-size:17px;font-weight:600;">%s</span>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:6px 0;">
                                            <span style="color:#94a3b8;font-size:12px;text-transform:uppercase;letter-spacing:1px;">Return Date</span><br>
                                            <span style="color:#1e293b;font-size:15px;font-weight:600;">%s</span>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:12px 0 0;">
                                            <span style="display:inline-block;background:#2ecc71;color:#ffffff;padding:6px 16px;border-radius:20px;font-size:13px;font-weight:600;">
                                                ✅ Returned
                                            </span>
                                        </td>
                                    </tr>
                                </table>
                            </div>

                            <p style="color:#555;font-size:14px;line-height:1.6;margin:0 0 8px;">
                                Thank you for returning the book on time. We hope you enjoyed reading it!
                            </p>
                        </div>

                        <!-- Footer -->
                        <div style="background:#f8fafc;border-top:1px solid #e2e8f0;padding:20px 32px;text-align:center;">
                            <p style="color:#94a3b8;font-size:12px;margin:0;">
                                This is an automated notification from OPAC Library Management System.<br>
                                Please do not reply to this email.
                            </p>
                        </div>

                    </div>
                </body>
                </html>
                """.formatted(username, bookTitle, formattedReturnDate);
    }
}
