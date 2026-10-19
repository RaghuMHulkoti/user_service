package com.learning.user_service.web.mapper;

import com.learning.user_service.core.processor.paymentmethod.add.AddPaymentMethodProcessorRequest;
import com.learning.user_service.core.processor.paymentmethod.add.AddPaymentMethodProcessorResponse;
import com.learning.user_service.core.processor.paymentmethod.delete.DeletePaymentMethodProcessorRequest;
import com.learning.user_service.core.processor.paymentmethod.list.ListPaymentMethodsProcessorRequest;
import com.learning.user_service.core.processor.paymentmethod.list.ListPaymentMethodsProcessorResponse;
import com.learning.user_service.core.processor.paymentmethod.makedefault.MakePaymentMethodDefaultProcessorRequest;
import com.learning.user_service.core.processor.paymentmethod.makedefault.MakePaymentMethodDefaultProcessorResponse;
import com.learning.user_service.web.model.AddPaymentMethodWebRequest;
import com.learning.user_service.web.response.PaymentMethodResponse;

import java.util.List;
import java.util.UUID;

public final class PaymentMethodMapper {

    private PaymentMethodMapper() {
    }

    public static ListPaymentMethodsProcessorRequest toListPaymentMethodsRequest(UUID userId) {
        return ListPaymentMethodsProcessorRequest.builder().userId(userId).build();
    }

    public static List<PaymentMethodResponse> mapListPaymentMethodsResponse(ListPaymentMethodsProcessorResponse response) {
        return response.getMethods().stream().map(PaymentMethodResponse::from).toList();
    }

    public static AddPaymentMethodProcessorRequest toAddPaymentMethodRequest(UUID userId, AddPaymentMethodWebRequest cmd) {
        return AddPaymentMethodProcessorRequest.builder()
                .userId(userId)
                .provider(cmd.provider())
                .providerToken(cmd.providerToken())
                .brand(cmd.brand())
                .last4(cmd.last4())
                .expMonth(cmd.expMonth())
                .expYear(cmd.expYear())
                .isDefault(cmd.isDefault())
                .build();
    }

    public static PaymentMethodResponse mapAddPaymentMethodResponse(AddPaymentMethodProcessorResponse response) {
        return PaymentMethodResponse.from(response.getMethod());
    }

    public static MakePaymentMethodDefaultProcessorRequest toMakePaymentMethodDefaultRequest(UUID userId, UUID methodId) {
        return MakePaymentMethodDefaultProcessorRequest.builder().userId(userId).methodId(methodId).build();
    }

    public static PaymentMethodResponse mapMakePaymentMethodDefaultResponse(MakePaymentMethodDefaultProcessorResponse response) {
        return PaymentMethodResponse.from(response.getMethod());
    }

    public static DeletePaymentMethodProcessorRequest toDeletePaymentMethodRequest(UUID userId, UUID methodId) {
        return DeletePaymentMethodProcessorRequest.builder().userId(userId).methodId(methodId).build();
    }
}
