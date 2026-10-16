package com.vericode.vericode_backend.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vericode.vericode_backend.dto.QuestionImportResponseDTO;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class QuestionImportService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public QuestionImportResponseDTO preview(String url) {
        URI uri = validateUrl(url);
        String host = uri.getHost().toLowerCase();

        try {
            if (host.equals("leetcode.com") || host.equals("www.leetcode.com")) {
                return importLeetCode(uri);
            }
            if (host.equals("hackerrank.com") || host.equals("www.hackerrank.com")) {
                return importHackerRank(uri);
            }
            throw new IllegalArgumentException("Only LeetCode and HackerRank question URLs are supported.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Question import was interrupted.");
        } catch (IOException e) {
            throw new IllegalStateException("Could not read the question from the supplied URL.");
        }
    }

    private QuestionImportResponseDTO importLeetCode(URI uri) throws IOException, InterruptedException {
        String slug = extractLeetCodeSlug(uri.getPath());
        if (slug == null) {
            throw new IllegalArgumentException("Use a LeetCode problem URL such as /problems/two-sum/.");
        }

        String query = "query questionData($titleSlug: String!) { question(titleSlug: $titleSlug) { title content difficulty exampleTestcases constraints codeSnippets { lang langSlug code } } }";
        String body = objectMapper.writeValueAsString(java.util.Map.of(
                "query", query,
                "variables", java.util.Map.of("titleSlug", slug)
        ));

        HttpRequest request = HttpRequest.newBuilder(URI.create("https://leetcode.com/graphql"))
                .timeout(Duration.ofSeconds(12))
                .header("Content-Type", "application/json")
                .header("User-Agent", "VeriCode/1.0")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IllegalStateException("LeetCode did not return the question. HTTP " + response.statusCode());
        }

        JsonNode question = objectMapper.readTree(response.body()).path("data").path("question");
        if (question.isMissingNode() || question.isNull()) {
            throw new IllegalStateException("LeetCode question was not found.");
        }

        QuestionImportResponseDTO result = new QuestionImportResponseDTO();
        result.setSource("LEETCODE");
        result.setSourceUrl(uri.toString());
        result.setTitle(question.path("title").asText(slug));
        result.setDescription(stripHtml(question.path("content").asText("")));
        result.setInputDescription("Use the input format described in the original problem.");
        result.setOutputDescription("Use the output format described in the original problem.");
        result.setConstraints(question.path("constraints").asText("Add constraints from the original problem before publishing."));
        result.setTimeLimitSeconds(60);
        result.setLanguage("JAVA");
        result.setNote("Imported as a draft. Review the problem statement, input/output format, constraints, and test cases before saving it to VeriCode.");
        return result;
    }

    private QuestionImportResponseDTO importHackerRank(URI uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(12))
                .header("User-Agent", "VeriCode/1.0")
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IllegalStateException("HackerRank did not return the question page. HTTP " + response.statusCode());
        }

        String html = response.body();
        String title = firstMatch(html, "<title[^>]*>(.*?)</title>");
        String description = firstMatch(html, "<meta[^>]+name=[\\\"']description[\\\"'][^>]+content=[\\\"'](.*?)[\\\"']");
        if (title == null || title.isBlank()) {
            throw new IllegalStateException("Could not identify the HackerRank question title.");
        }

        title = decodeHtml(title).replace(" | HackerRank", "").trim();
        description = description == null ? "Review and complete the problem statement from the source page." : decodeHtml(description);

        QuestionImportResponseDTO result = new QuestionImportResponseDTO();
        result.setSource("HACKERRANK");
        result.setSourceUrl(uri.toString());
        result.setTitle(title);
        result.setDescription(description);
        result.setInputDescription("Review the source question and enter its input format here.");
        result.setOutputDescription("Review the source question and enter its output format here.");
        result.setConstraints("Review the source question and enter its constraints here.");
        result.setTimeLimitSeconds(60);
        result.setLanguage("JAVA");
        result.setNote("HackerRank question pages may not expose the complete problem body to unauthenticated clients. VeriCode imports the available metadata and leaves the fields editable for review.");
        return result;
    }

    private URI validateUrl(String rawUrl) {
        try {
            URI uri = URI.create(rawUrl.trim());
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
                throw new IllegalArgumentException("Enter a valid HTTPS question URL.");
            }
            String host = uri.getHost().toLowerCase();
            if (!(host.equals("leetcode.com") || host.equals("www.leetcode.com") ||
                    host.equals("hackerrank.com") || host.equals("www.hackerrank.com"))) {
                throw new IllegalArgumentException("Only LeetCode and HackerRank URLs can be imported.");
            }
            return uri;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage() == null ? "Enter a valid question URL." : e.getMessage());
        }
    }

    private String extractLeetCodeSlug(String path) {
        Matcher matcher = Pattern.compile("/problems/([^/]+)/?").matcher(path);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String firstMatch(String text, String regex) {
        Matcher matcher = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String stripHtml(String value) {
        return decodeHtml(value.replaceAll("<br\\s*/?>", "\\n").replaceAll("</p>", "\\n\\n").replaceAll("<[^>]+>", ""))
                .replaceAll("\\n{3,}", "\\n\\n").trim();
    }

    private String decodeHtml(String value) {
        return value.replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'");
    }
}
