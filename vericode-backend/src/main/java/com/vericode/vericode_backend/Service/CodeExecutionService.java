package com.vericode.vericode_backend.Service;

import com.vericode.vericode_backend.dto.CodeExecutionRequestDTO;
import com.vericode.vericode_backend.dto.CodeExecutionResponseDTO;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Service
public class CodeExecutionService {
    private static final String IMAGE = "eclipse-temurin:21-jdk";
    private static final int MAX_TIMEOUT_SECONDS = 15;

    public CodeExecutionResponseDTO execute(CodeExecutionRequestDTO request) {
        long started = System.nanoTime();
        Path workspace = null;
        Process process = null;
        try {
            workspace = Files.createTempDirectory("vericode-run-");
            Files.writeString(workspace.resolve("Main.java"), request.getSourceCode(), StandardCharsets.UTF_8);
            Files.writeString(workspace.resolve("input.txt"), request.getInput(), StandardCharsets.UTF_8);

            int timeout = Math.min(Math.max(request.getTimeLimitSeconds(), 1), MAX_TIMEOUT_SECONDS);
            ProcessBuilder builder = new ProcessBuilder(
                    "docker", "run", "--rm", "--network", "none",
                    "--memory", "256m", "--cpus", "1", "--pids-limit", "64",
                    "--cap-drop", "ALL", "--security-opt", "no-new-privileges",
                    "--read-only", "--tmpfs", "/tmp:rw,nosuid,nodev,size=64m",
                    "--mount", "type=bind,source=" + workspace.toAbsolutePath() + ",target=/workspace",
                    IMAGE,
                    "sh", "-c",
                    "javac /workspace/Main.java && java -cp /workspace Main < /workspace/input.txt"
            );
            builder.redirectErrorStream(true);
            process = builder.start();

            boolean finished = process.waitFor(timeout, TimeUnit.SECONDS);
            long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
            if (!finished) {
                process.destroyForcibly();
                return new CodeExecutionResponseDTO(true, false, true, "", "Execution timed out.", elapsed);
            }

            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                return new CodeExecutionResponseDTO(false, false, false, "", limit(output), elapsed);
            }

            String actual = normalize(output);
            String expected = normalize(request.getExpectedOutput());
            boolean passed = !expected.isEmpty() && actual.equals(expected);
            String message = expected.isEmpty() ? "Program executed successfully." : (passed ? "All provided output checks passed." : "Output did not match the expected result.");
            return new CodeExecutionResponseDTO(true, passed, false, output.isBlank() ? message : output, passed || expected.isEmpty() ? "" : message, elapsed);
        } catch (IOException exception) {
            return new CodeExecutionResponseDTO(false, false, false, "", "Docker execution failed: " + exception.getMessage(), elapsed(started));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return new CodeExecutionResponseDTO(false, false, true, "", "Execution was interrupted.", elapsed(started));
        } finally {
            if (process != null && process.isAlive()) process.destroyForcibly();
            if (workspace != null) deleteWorkspace(workspace);
        }
    }

    private long elapsed(long started) { return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started); }

    private String normalize(String value) {
        return value == null ? "" : value.replace("\r\n", "\n").trim();
    }

    private String limit(String value) {
        if (value == null) return "";
        return value.length() > 12000 ? value.substring(0, 12000) + "\n[output truncated]" : value;
    }

    private void deleteWorkspace(Path path) {
        try (var stream = Files.walk(path)) {
            stream.sorted(java.util.Comparator.reverseOrder()).forEach(item -> {
                try { Files.deleteIfExists(item); } catch (IOException ignored) { }
            });
        } catch (IOException ignored) { }
    }
}
