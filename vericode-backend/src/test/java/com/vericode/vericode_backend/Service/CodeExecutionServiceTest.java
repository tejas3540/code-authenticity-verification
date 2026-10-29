package com.vericode.vericode_backend.Service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodeExecutionServiceTest {
    private final CodeExecutionService service = new CodeExecutionService();

    @Test
    void detectsWindowsDockerEngineUnavailableMessage() {
        String output = "failed to connect to the docker API at npipe:////./pipe/docker_engine; "
                + "check if the path is correct and if the daemon is running";

        assertTrue(service.isDockerUnavailableOutput(output));
    }

    @Test
    void leavesJavaCompilationErrorsAsCodeErrors() {
        assertFalse(service.isDockerUnavailableOutput("Main.java:5: error: ';' expected"));
    }

    @Test
    void capturesOutputUntilConfiguredLimitAndMarksTruncation() {
        CodeExecutionService.BoundedOutputCapture capture =
                new CodeExecutionService.BoundedOutputCapture(5);
        char[] firstChunk = "abc".toCharArray();
        char[] secondChunk = "defgh".toCharArray();

        capture.append(firstChunk, 0, firstChunk.length);
        capture.append(secondChunk, 0, secondChunk.length);

        assertEquals("abcde", capture.getOutput());
        assertTrue(capture.isTruncated());
    }

    @Test
    void doesNotMarkOutputTruncatedWhenItFitsWithinLimit() {
        CodeExecutionService.BoundedOutputCapture capture =
                new CodeExecutionService.BoundedOutputCapture(5);
        char[] output = "abcde".toCharArray();

        capture.append(output, 0, output.length);

        assertEquals("abcde", capture.getOutput());
        assertFalse(capture.isTruncated());
    }
}
