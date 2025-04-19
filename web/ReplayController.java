package com.example.transactionretryreplay.web;


import com.example.transactionretryreplay.model.Transaction;
import com.example.transactionretryreplay.service.ReplayService;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequiredArgsConstructor
@RequestMapping("/replay") // Keep the /replay mapping for HTML views
public class ReplayController {

    private static final Logger log = LoggerFactory.getLogger(ReplayController.class);

    private final ReplayService replayService;

    @GetMapping("/failed")
    public String showFailedTransactions(Model model) {
        List<Transaction> failedTransactions = replayService.getFailedTransactions();
        model.addAttribute("failedTransactions", failedTransactions);
        return "replay-form"; // Name of the Thymeleaf template
    }

    @PostMapping("/trigger")
    public String triggerReplay(@RequestParam("transactionId") Long transactionId, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String replayedBy = authentication.getName(); // Get the username of the logged-in user

        log.info("Replay triggered for transaction ID: {} by user: {}", transactionId, replayedBy);
        replayService.replayTransaction(transactionId, replayedBy);
        return "redirect:/replay/history"; // Redirect to replay history page
    }

    @GetMapping("/history")
    public String showReplayHistory(Model model) {
        // Implement logic to fetch and display replay history
        // List<ReplayLog> replayLogs = replayLogRepository.findAll();
        // model.addAttribute("replayLogs", replayLogs);
        model.addAttribute("message", "Replay history will be displayed here.");
        return "replay-history"; // Name of the Thymeleaf template
    }
}