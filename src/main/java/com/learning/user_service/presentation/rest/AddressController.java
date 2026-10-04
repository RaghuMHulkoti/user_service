package com.learning.user_service.presentation.rest;

import com.learning.user_service.application.command.AddAddressCommand;
import com.learning.user_service.application.command.UpdateAddressCommand;
import com.learning.user_service.application.query.AddressView;
import com.learning.user_service.application.service.AddressService;
import com.learning.user_service.application.support.IdempotencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/me/addresses")
@Tag(name = "Addresses")
public class AddressController {

    private final AddressService addressService;
    private final IdempotencyService idempotencyService;

    public AddressController(AddressService addressService, IdempotencyService idempotencyService) {
        this.addressService = addressService;
        this.idempotencyService = idempotencyService;
    }

    @Operation(summary = "List addresses")
    @GetMapping
    public List<AddressView> list(@AuthenticationPrincipal Jwt jwt) {
        return addressService.list(CurrentUser.id(jwt));
    }

    @Operation(summary = "Add address (first one becomes default)")
    @PostMapping
    public ResponseEntity<AddressView> add(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(name = IdempotentResponses.KEY_HEADER, required = false) String idempotencyKey,
            @Valid @RequestBody AddAddressCommand cmd) {
        UUID userId = CurrentUser.id(jwt);
        return IdempotentResponses.created(idempotencyService.execute("addresses:" + userId, idempotencyKey, cmd,
                AddressView.class, () -> addressService.add(userId, cmd)));
    }

    @Operation(summary = "Update address (partial)")
    @PatchMapping("/{id}")
    public AddressView update(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                              @Valid @RequestBody UpdateAddressCommand cmd) {
        return addressService.update(CurrentUser.id(jwt), id, cmd);
    }

    @Operation(summary = "Remove address")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        addressService.delete(CurrentUser.id(jwt), id);
    }
}
