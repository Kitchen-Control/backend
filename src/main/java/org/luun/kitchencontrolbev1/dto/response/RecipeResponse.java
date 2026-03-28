package org.luun.kitchencontrolbev1.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecipeResponse {
    Integer id;
    String recipeName;
    Float yieldQuantity;
    String description;
    Integer productId;
    String productName;
    List<RecipeDetailResponse> recipeDetails;
}
