package com.bilkent.devinsight.controller;

import com.bilkent.devinsight.entity.Contributor;
import com.bilkent.devinsight.entity.Issue;
import com.bilkent.devinsight.entity.PullRequest;
import com.bilkent.devinsight.request.QGetRepository;
import com.bilkent.devinsight.request.QGithubScrape;
import com.bilkent.devinsight.response.struct.ApiResponse;
import com.bilkent.devinsight.service.IssueService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@AllArgsConstructor
@RestController
@RequestMapping("/issue")
public class IssueController {

    private final IssueService issueService;


    @GetMapping("/{repoOwner}/{repoName}")
    public ResponseEntity<ApiResponse<Set<Issue>>> getIssues(
            @PathVariable("repoOwner") String repoOwner,
            @PathVariable("repoName") String repoName) {
        QGetRepository qGetRepository = QGetRepository.builder()
                .repoOwner(repoOwner)
                .repoName(repoName)
                .build();
        Set<Issue> issues = issueService.getIssues(qGetRepository);

        return ResponseEntity.ok(
                ApiResponse.<Set<Issue>>builder()
                        .data(issues)
                        .status(HttpStatus.OK.value())
                        .message("Successfully scraped issues")
                        .build());
    }


    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, path="scrape")
    public ResponseEntity<ApiResponse<Set<Issue>>> scrapePullRequests(@Valid @RequestBody QGithubScrape qGithubScrape) {
        Set<Issue> issues = issueService.scrapeIssues(qGithubScrape);

        return ResponseEntity.ok(
                ApiResponse.<Set<Issue>>builder()
                        .data(issues)
                        .status(HttpStatus.OK.value())
                        .message("Successfully fetched issues")
                        .build());
    }

}
