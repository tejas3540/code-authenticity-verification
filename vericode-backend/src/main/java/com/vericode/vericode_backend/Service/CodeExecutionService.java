package com.vericode.vericode_backend.Service;

import com.vericode.vericode_backend.dto.CodeExecutionRequestDTO;
import com.vericode.vericode_backend.dto.CodeExecutionResponseDTO;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Service
public class CodeExecutionService {
    private static final String IMAGE = "eclipse-temurin:21-jdk";
    private static final int MAX_TIMEOUT_SECONDS = 15;
    private static final int MAX_OUTPUT_CHARACTERS = 12_000;

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

            Process executionProcess = builder.start();
            process = executionProcess;

            // Drain output while the process runs to avoid filling the OS pipe and blocking it.
            BoundedOutputCapture capture = new BoundedOutputCapture(MAX_OUTPUT_CHARACTERS);
            Thread outputReader = new Thread(
                    () -> captureOutput(executionProcess, capture),
                    "vericode-output-reader"
            );
            outputReader.setDaemon(true);
            outputReader.start();

            boolean finished = executionProcess.waitFor(timeout, TimeUnit.SECONDS);
            long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
            if (!finished) {
                executionProcess.destroyForcibly();
                executionProcess.waitFor();
                outputReader.join();
                return new CodeExecutionResponseDTO(true, false, true, "", "Execution timed out.", elapsed);
            }

            outputReader.join();
            String output = capture.getOutput();
            if (capture.isTruncated()) {
                output += "\n[output truncated]";
            }

            int exitCode = executionProcess.exitValue();
            if (exitCode != 0) {
                if (isDockerUnavailableOutput(output)) {
                    return new CodeExecutionResponseDTO(false, false, false, "",
                            "Docker execution failed: Docker Desktop is not running on the backend computer.", elapsed);
                }
                return new CodeExecutionResponseDTO(false, false, false, "", output, elapsed);
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

    private void captureOutput(Process process, BoundedOutputCapture capture) {
        try (var reader = new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)) {
            char[] buffer = new char[1024];
            int count;
            while ((count = reader.read(buffer)) != -1) {
                capture.append(buffer, 0, count);
            }
        } catch (IOException ignored) {
            // The process may close its stream while being forcibly terminated on timeout.
        }
    }

    private long elapsed(long started) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
    }

    boolean isDockerUnavailableOutput(String output) {
        if (output == null) return false;
        String message = output.toLowerCase(Locale.ROOT);
        return message.contains("failed to connect to the docker api")
                || message.contains("cannot connect to the docker daemon")
                || message.contains("error during connect")
                || message.contains("is the docker daemon running");
    }

    private String normalize(String value) {
        return value == null ? "" : value.replace("\r\n", "\n").trim();
    }

    static final class BoundedOutputCapture {
        private final int maxCharacters;
        private final StringBuilder output = new StringBuilder();
        private boolean truncated;

        BoundedOutputCapture(int maxCharacters) {
            if (maxCharacters < 0) {
                throw new IllegalArgumentException("Maximum output size cannot be negative");
            }
            this.maxCharacters = maxCharacters;
        }

        void append(char[] buffer, int offset, int length) {
            int remaining = maxCharacters - output.length();
            int charactersToKeep = Math.min(remaining, length);
            if (charactersToKeep > 0) {
                output.append(buffer, offset, charactersToKeep);
            }
            if (charactersToKeep < length) {
                truncated = true;
            }
        }

        String getOutput() {
            return output.toString();
        }

        boolean isTruncated() {
            return truncated;
        }
    }

    private void deleteWorkspace(Path path) {
        try (var stream = Files.walk(path)) {
            stream.sorted(java.util.Comparator.reverseOrder()).forEach(item -> {
                try {
                    Files.deleteIfExists(item);
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
    }
}
