package com.vericode.vericode_backend.Service;

import org.junit.jupiter.api.Test;

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
}
