package com.example.wgn_igloo.recipe

data class RecipeSearch(
    var imageId: String? = null,
    var recipeName: String = "",
    var totalTime: String = "",
    var dishType: List<String>? = listOf(),
    var cuisineType: List<String>? = listOf(),
    var dietType: List<String>? = listOf(),
    var servingSize: String = "",
    var instructions: List<RecipeInstruction>? = listOf(),
    var usedIngredients: List<Ingredient>? = listOf(),
    var unusedIngredients: List<Ingredient>? = listOf(),
    var missedIngredients: List<Ingredient>? = listOf()
)
