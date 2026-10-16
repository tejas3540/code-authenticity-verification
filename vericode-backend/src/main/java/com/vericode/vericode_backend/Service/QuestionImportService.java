package com.vericode.vericode_backend.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vericode.vericode_backend.dto.QuestionImportResponseDTO;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class QuestionImportService {
    private static final String LEETCODE_GRAPHQL = "https://leetcode.com/graphql/";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
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
            throw new IllegalStateException("Question import was interrupted. Please try again.");
        } catch (IOException e) {
            throw new IllegalStateException("Could not connect to the question source. Check your internet connection and try again.");
        }
    }

    private QuestionImportResponseDTO importLeetCode(URI uri) throws IOException, InterruptedException {
        String slug = extractLeetCodeSlug(uri.getPath());
        if (slug == null) {
            throw new IllegalArgumentException("Use a LeetCode problem URL such as /problems/two-sum/.");
        }

        // LeetCode's public GraphQL endpoint accepts a GET query without requiring a
        // logged-in session. Keeping the slug inside the query also avoids sending a
        // POST body that some edge/proxy configurations reject.
        String query = "query { question(titleSlug: \"" + slug + "\") { title content difficulty exampleTestcases constraints codeSnippets { lang langSlug code } } }";
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        URI graphqlUri = URI.create(LEETCODE_GRAPHQL + "?query=" + encodedQuery);

        HttpRequest request = HttpRequest.newBuilder(graphqlUri)
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/json")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/131.0 Safari/537.36")
                .header("Referer", "https://leetcode.com/problems/" + slug + "/")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IllegalStateException("LeetCode returned HTTP " + response.statusCode() + ". The problem may be temporarily unavailable.");
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode errors = root.path("errors");
        if (errors.isArray() && !errors.isEmpty()) {
            String message = errors.get(0).path("message").asText("LeetCode rejected the request.");
            throw new IllegalStateException("LeetCode import failed: " + message);
        }

        JsonNode question = root.path("data").path("question");
        if (question.isMissingNode() || question.isNull() || question.path("title").asText().isBlank()) {
            throw new IllegalStateException("LeetCode question was not found. Check that the URL points to a public problem.");
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
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "text/html,application/xhtml+xml")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/131.0 Safari/537.36")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IllegalStateException("HackerRank returned HTTP " + response.statusCode() + ". The challenge may require authentication.");
        }

        String html = response.body();
        String title = firstMatch(html, "<title[^>]*>(.*?)</title>");
        String description = firstMatch(html, "<meta[^>]+name=[\\\"']description[\\\"'][^>]+content=[\\\"'](.*?)[\\\"']");
        if (title == null || title.isBlank()) {
            throw new IllegalStateException("Could not identify the HackerRank question. The page may require authentication.");
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
        result.setNote("HackerRank may not expose the complete problem body to unauthenticated clients. Review the imported draft before saving.");
        return result;
    }

    private URI validateUrl(String rawUrl) {
        try {
            URI uri = URI.create(rawUrl == null ? "" : rawUrl.trim());
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
        Matcher matcher = Pattern.compile("/problems/([a-zA-Z0-9-]+)/?").matcher(path);
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
