package org.luun.kitchencontrolbev1.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecipeDetailResponse {
    Integer recipeDetailId;
    Float quantity;
    Integer rawMaterialId;
    String rawMaterialName;
    String unit;
}
