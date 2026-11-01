package com.vericode.vericode_backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "VeriCode REST API",
                version = "1.0.0",
                description = """
                        API for the VeriCode coding assessment platform.
                        Recruiters manage questions, test cases and assessments.
                        Students execute code, start timed attempts and submit solutions.
                        Registration and login establish an HTTP session. Protected endpoints
                        use the JSESSIONID session cookie. Role checks are enforced by the
                        application controllers.
                        """
        )
)
@SecurityScheme(
        name = "SessionCookie",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.COOKIE,
        paramName = "JSESSIONID",
        description = "HTTP session cookie created by student/recruiter registration or login."
)
public class OpenApiConfiguration {
}
