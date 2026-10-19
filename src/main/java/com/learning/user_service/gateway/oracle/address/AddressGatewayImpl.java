package com.learning.user_service.gateway.oracle.address;

import com.learning.user_service.gateway.AddressGateway;
import com.learning.user_service.gateway.oracle.address.entity.AddressEntity;
import com.learning.user_service.gateway.oracle.address.repository.AddressRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AddressGatewayImpl implements AddressGateway {

    private final com.learning.user_service.gateway.oracle.address.repository.AddressRepository repository;

    public AddressGatewayImpl(com.learning.user_service.gateway.oracle.address.repository.AddressRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<AddressEntity> findByUserId(UUID userId) {
        return repository.findByUserIdOrderByCreatedAtAsc(userId);
    }

    @Override
    public Optional<AddressEntity> findByIdAndUserId(UUID id, UUID userId) {
        return repository.findByIdAndUserId(id, userId);
    }

    @Override
    public long countByUserId(UUID userId) {
        return repository.countByUserId(userId);
    }

    @Override
    public Optional<AddressEntity> findFirstByUserId(UUID userId) {
        return repository.findFirstByUserIdOrderByCreatedAtAsc(userId);
    }

    @Override
    public void clearDefault(UUID userId) {
        repository.clearDefault(userId);
    }

    @Override
    public AddressEntity saveAndFlush(AddressEntity address) {
        return repository.saveAndFlush(address);
    }

    @Override
    public void delete(AddressEntity address) {
        repository.delete(address);
    }

    @Override
    public void deleteAll(List<AddressEntity> addresses) {
        repository.deleteAll(addresses);
    }

    @Override
    public void flush() {
        repository.flush();
    }
}
