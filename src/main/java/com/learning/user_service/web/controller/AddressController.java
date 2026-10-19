package com.learning.user_service.web.controller;

import com.learning.user_service.web.model.AddAddressWebRequest;
import com.learning.user_service.web.model.UpdateAddressWebRequest;
import com.learning.user_service.web.response.AddressResponse;
import com.learning.user_service.core.support.IdempotencyService;
import com.learning.user_service.core.processor.address.list.ListAddressesProcessor;
import com.learning.user_service.core.processor.address.add.AddAddressProcessor;
import com.learning.user_service.core.processor.address.update.UpdateAddressProcessor;
import com.learning.user_service.core.processor.address.delete.DeleteAddressProcessor;
import com.learning.user_service.web.mapper.AddressMapper;
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

    private final ListAddressesProcessor listAddressesProcessor;
    private final AddAddressProcessor addAddressProcessor;
    private final UpdateAddressProcessor updateAddressProcessor;
    private final DeleteAddressProcessor deleteAddressProcessor;
    private final IdempotencyService idempotencyService;

    public AddressController(ListAddressesProcessor listAddressesProcessor, AddAddressProcessor addAddressProcessor,
                             UpdateAddressProcessor updateAddressProcessor,
                             DeleteAddressProcessor deleteAddressProcessor, IdempotencyService idempotencyService) {
        this.listAddressesProcessor = listAddressesProcessor;
        this.addAddressProcessor = addAddressProcessor;
        this.updateAddressProcessor = updateAddressProcessor;
        this.deleteAddressProcessor = deleteAddressProcessor;
        this.idempotencyService = idempotencyService;
    }

    @Operation(summary = "List addresses")
    @GetMapping
    public List<AddressResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return AddressMapper.mapListAddressesResponse(
                listAddressesProcessor.process(AddressMapper.toListAddressesRequest(CurrentUser.id(jwt))));
    }

    @Operation(summary = "Add address (first one becomes default)")
    @PostMapping
    public ResponseEntity<AddressResponse> add(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(name = IdempotentResponses.KEY_HEADER, required = false) String idempotencyKey,
            @Valid @RequestBody AddAddressWebRequest cmd) {
        UUID userId = CurrentUser.id(jwt);
        return IdempotentResponses.created(idempotencyService.execute("addresses:" + userId, idempotencyKey, cmd,
                AddressResponse.class, () -> AddressMapper.mapAddAddressResponse(
                        addAddressProcessor.process(AddressMapper.toAddAddressRequest(userId, cmd)))));
    }

    @Operation(summary = "Update address (partial)")
    @PatchMapping("/{id}")
    public AddressResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                              @Valid @RequestBody UpdateAddressWebRequest cmd) {
        return AddressMapper.mapUpdateAddressResponse(updateAddressProcessor.process(
                AddressMapper.toUpdateAddressRequest(CurrentUser.id(jwt), id, cmd)));
    }

    @Operation(summary = "Remove address")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        deleteAddressProcessor.process(AddressMapper.toDeleteAddressRequest(CurrentUser.id(jwt), id));
    }
}
