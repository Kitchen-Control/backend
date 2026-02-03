package org.luun.kitchencontrolbev1.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecipeRequest {
    String recipeName;
    Float yieldQuantity;
    String description;
    Integer productId;
    List<RecipeDetailRequest> recipeDetails;
}
