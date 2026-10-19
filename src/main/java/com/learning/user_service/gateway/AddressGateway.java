package com.learning.user_service.gateway;

import com.learning.user_service.gateway.oracle.address.entity.AddressEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressGateway {

    List<AddressEntity> findByUserId(UUID userId);

    Optional<AddressEntity> findByIdAndUserId(UUID id, UUID userId);

    long countByUserId(UUID userId);

    Optional<AddressEntity> findFirstByUserId(UUID userId);

    /** Clears the current default before a new default row is written. */
    void clearDefault(UUID userId);

    AddressEntity saveAndFlush(AddressEntity address);

    void delete(AddressEntity address);

    void deleteAll(List<AddressEntity> addresses);

    void flush();
}
