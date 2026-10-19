package com.learning.user_service.core.processor.profile.update;

import com.learning.user_service.core.processor.ProcessorRequest;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileProcessorRequest implements ProcessorRequest {

    private UUID userId;
    private String firstName;
    private String lastName;
    private String phone;
    private String locale;
    private Long version;
}
