package com.learning.user_service.web.mapper;

import com.learning.user_service.core.processor.address.add.AddAddressProcessorRequest;
import com.learning.user_service.core.processor.address.add.AddAddressProcessorResponse;
import com.learning.user_service.core.processor.address.delete.DeleteAddressProcessorRequest;
import com.learning.user_service.core.processor.address.list.ListAddressesProcessorRequest;
import com.learning.user_service.core.processor.address.list.ListAddressesProcessorResponse;
import com.learning.user_service.core.processor.address.update.UpdateAddressProcessorRequest;
import com.learning.user_service.core.processor.address.update.UpdateAddressProcessorResponse;
import com.learning.user_service.web.model.AddAddressWebRequest;
import com.learning.user_service.web.model.UpdateAddressWebRequest;
import com.learning.user_service.web.response.AddressResponse;

import java.util.List;
import java.util.UUID;

public final class AddressMapper {

    private AddressMapper() {
    }

    public static ListAddressesProcessorRequest toListAddressesRequest(UUID userId) {
        return ListAddressesProcessorRequest.builder().userId(userId).build();
    }

    public static List<AddressResponse> mapListAddressesResponse(ListAddressesProcessorResponse response) {
        return response.getAddresses().stream().map(AddressResponse::from).toList();
    }

    public static AddAddressProcessorRequest toAddAddressRequest(UUID userId, AddAddressWebRequest cmd) {
        return AddAddressProcessorRequest.builder()
                .userId(userId)
                .label(cmd.label())
                .line1(cmd.line1())
                .line2(cmd.line2())
                .city(cmd.city())
                .state(cmd.state())
                .postalCode(cmd.postalCode())
                .country(cmd.country())
                .isDefault(cmd.isDefault())
                .geoLat(cmd.geoLat())
                .geoLng(cmd.geoLng())
                .build();
    }

    public static AddressResponse mapAddAddressResponse(AddAddressProcessorResponse response) {
        return AddressResponse.from(response.getAddress());
    }

    public static UpdateAddressProcessorRequest toUpdateAddressRequest(UUID userId, UUID addressId,
                                                                       UpdateAddressWebRequest cmd) {
        return UpdateAddressProcessorRequest.builder()
                .userId(userId)
                .addressId(addressId)
                .label(cmd.label())
                .line1(cmd.line1())
                .line2(cmd.line2())
                .city(cmd.city())
                .state(cmd.state())
                .postalCode(cmd.postalCode())
                .country(cmd.country())
                .isDefault(cmd.isDefault())
                .geoLat(cmd.geoLat())
                .geoLng(cmd.geoLng())
                .version(cmd.version())
                .build();
    }

    public static AddressResponse mapUpdateAddressResponse(UpdateAddressProcessorResponse response) {
        return AddressResponse.from(response.getAddress());
    }

    public static DeleteAddressProcessorRequest toDeleteAddressRequest(UUID userId, UUID addressId) {
        return DeleteAddressProcessorRequest.builder().userId(userId).addressId(addressId).build();
    }
}
