package org.luun.kitchencontrolbev1.repository;

import org.luun.kitchencontrolbev1.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeRepository extends JpaRepository<Recipe,Long> {
}
