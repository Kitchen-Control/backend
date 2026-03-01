package org.luun.kitchencontrolbev1.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AssignShipperRequest {
    private Integer shipperId;
    private List<Integer> orderIds;
}
