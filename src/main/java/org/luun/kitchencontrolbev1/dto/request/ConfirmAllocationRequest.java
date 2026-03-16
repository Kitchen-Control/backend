package org.luun.kitchencontrolbev1.dto.request;

import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ConfirmAllocationRequest {
    List<FinalProductAllocation> finalAllocations;

    @Data
    @FieldDefaults(level = lombok.AccessLevel.PRIVATE)
    public static class FinalProductAllocation {
        Integer orderDetailId;
        List<FinalBatchPick> batchPicks;

        @Data
        @FieldDefaults(level = lombok.AccessLevel.PRIVATE)
        public static class FinalBatchPick {
            Integer batchId;
            Float quantity;
        }
    }
}
