package com.learning.user_service.presentation.rest;

import com.learning.user_service.infrastructure.security.JwtKeyProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Public signing keys. The API gateway validates access tokens against these (TDD §11.1, §15.1). */
@RestController
@Tag(name = "Keys")
public class JwksController {

    private final JwtKeyProvider keyProvider;

    public JwksController(JwtKeyProvider keyProvider) {
        this.keyProvider = keyProvider;
    }

    @Operation(summary = "JSON Web Key Set")
    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwks() {
        return keyProvider.publicJwkSet().toJSONObject();
    }
}
