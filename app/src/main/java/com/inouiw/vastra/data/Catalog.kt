package com.inouiw.vastra.data

import androidx.compose.runtime.mutableStateListOf

data class Product(val id: String, val category: String, val name: String, val style: String, val price: Int, val colour: String, val shape: Int, val fill: Long, val stock: Int = 5, val photo: String = "")

object Catalog {
    /** Live catalog, read from Firestore's products collection. */
    val items = mutableStateListOf<Product>()

    fun setAll(list: List<Product>) { items.clear(); items.addAll(list.sortedByDescending { it.photo.isNotEmpty() }) }

    /** Starting catalog, uploaded once when the products collection is empty. */
    val seed: List<Product> = listOf(
        Product("new01", "Tops", "Mesh flutter top", "formal", 28, "Burgundy", 0, 0xFF7a1f2b, photo = "products/new01.jpg"),
        Product("new02", "Tops", "Wrap shirt", "formal", 49, "Cream", 0, 0xFFf1ead9, photo = "products/new02.jpg"),
        Product("new03", "Dresses", "Leaf print shirt dress", "formal", 42, "Navy print", 0, 0xFF2b3a55, photo = "products/new03.jpg"),
        Product("new04", "Tops", "Ruffle tie crop top", "casual", 18, "Blue", 0, 0xFF3f6fa3, photo = "products/new04.jpg"),
        Product("new05", "Tops", "Ruched red bandeau top", "formal", 22, "Red", 0, 0xFFb3202a, photo = "products/new05.jpg"),
        Product("new06", "Tops", "Ruched frill peplum top", "formal", 24, "Black", 0, 0xFF111111, photo = "products/new06.jpg"),
        Product("new07", "Tops", "Twist-front cami", "casual", 16, "Black", 0, 0xFF111111, photo = "products/new07.jpg"),
        Product("new08", "Tops", "Toile tie-front cami", "casual", 26, "Blue and white", 0, 0xFF3f6fa3, photo = "products/new08.jpg"),
        Product("new09", "Bottoms", "Bow embroidered midi skirt", "casual", 34, "Grey", 0, 0xFF9a9a9a, photo = "products/new09.jpg"),
        Product("new10", "Bottoms", "Black pleated midi skirt", "formal", 38, "Black", 0, 0xFF111111, photo = "products/new10.jpg"),
        Product("new11", "Bottoms", "Bow mini skirt", "formal", 32, "Black", 0, 0xFF111111, photo = "products/new11.jpg"),
        Product("new12", "Bottoms", "Denim button mini skirt", "casual", 29, "Black", 0, 0xFF2a2a2a, photo = "products/new12.jpg"),
        Product("new13", "Bottoms", "Red button mini skirt", "formal", 45, "Red", 0, 0xFFb3202a, photo = "products/new13.jpg"),
        Product("dress1", "Dresses", "Silk midi dress", "formal", 48, "Camel", 0, 0xFFb98a5e),
        Product("dress2", "Dresses", "Satin cocktail dress", "formal", 52, "Black", 0, 0xFF111111),
        Product("dress3", "Dresses", "Pleat midi dress", "formal", 41, "Sage", 0, 0xFF9aa58c),
        Product("dress4", "Dresses", "Tailored sheath dress", "formal", 38, "Navy", 1, 0xFF1f2a44),
        Product("dress5", "Dresses", "Cotton shirt dress", "casual", 28, "Ivory", 2, 0xFFf1ead9),
        Product("dress6", "Dresses", "Linen midi dress", "casual", 31, "Mustard", 2, 0xFFc99a2e),
        Product("dress7", "Dresses", "Printed day dress", "casual", 27, "Blush", 0, 0xFFe6c3b8),
        Product("dress8", "Dresses", "Jersey wrap dress", "casual", 26, "Charcoal", 2, 0xFF3d3d3d),
        Product("dress9", "Dresses", "Modal nightdress", "lounge", 17, "Cream", 1, 0xFFf7f2e6),
        Product("dress10", "Dresses", "Cotton nightdress", "lounge", 14, "Olive", 1, 0xFF5d5e3a),
        Product("top1", "Tops", "Tailored blazer", "formal", 62, "Black", 3, 0xFF111111),
        Product("top2", "Tops", "Poplin shirt", "formal", 24, "Ivory", 4, 0xFFf1ead9),
        Product("top3", "Tops", "Silk blouse", "formal", 31, "Navy", 4, 0xFF1f2a44),
        Product("top4", "Tops", "Linen top", "casual", 28, "Sage", 5, 0xFF9aa58c),
        Product("top5", "Tops", "Basic tee", "casual", 9, "Charcoal", 5, 0xFF3d3d3d),
        Product("top6", "Tops", "Ribbed tank", "casual", 12, "Black", 6, 0xFF111111),
        Product("top7", "Tops", "Crop tee", "casual", 15, "Mustard", 6, 0xFFc99a2e),
        Product("top8", "Tops", "Striped tee", "casual", 14, "Navy", 5, 0xFF1f2a44),
        Product("top9", "Tops", "Lounge tee", "lounge", 11, "Cream", 5, 0xFFf7f2e6),
        Product("top10", "Tops", "Lounge camisole", "lounge", 10, "Blush", 6, 0xFFe6c3b8),
        Product("bottom1", "Bottoms", "Slim trousers", "formal", 28, "Charcoal", 7, 0xFF3d3d3d),
        Product("bottom2", "Bottoms", "Wide-leg trousers", "formal", 31, "Camel", 7, 0xFFb98a5e),
        Product("bottom3", "Bottoms", "Pleat midi skirt", "formal", 31, "Olive", 8, 0xFF5d5e3a),
        Product("bottom4", "Bottoms", "Satin midi skirt", "formal", 35, "Black", 8, 0xFF111111),
        Product("bottom5", "Bottoms", "Straight jeans", "casual", 27, "Navy", 9, 0xFF1f2a44),
        Product("bottom6", "Bottoms", "Cream denim", "casual", 28, "Cream", 9, 0xFFf7f2e6),
        Product("bottom7", "Bottoms", "A-line mini skirt", "casual", 19, "Blush", 8, 0xFFe6c3b8),
        Product("bottom8", "Bottoms", "Linen shorts", "casual", 17, "Sage", 7, 0xFF9aa58c),
        Product("bottom9", "Bottoms", "Lounge pyjama pants", "lounge", 12, "Charcoal", 7, 0xFF3d3d3d),
        Product("bottom10", "Bottoms", "Lounge shorts", "lounge", 9, "Ivory", 7, 0xFFf1ead9),
        Product("shoe1", "Footwear", "Strap heels", "formal", 31, "Black", 10, 0xFF111111),
        Product("shoe2", "Footwear", "Block heels", "formal", 36, "Camel", 10, 0xFFb98a5e),
        Product("shoe3", "Footwear", "Leather loafers", "formal", 40, "Camel", 11, 0xFFb98a5e),
        Product("shoe4", "Footwear", "Pointed flats", "formal", 28, "Navy", 11, 0xFF1f2a44),
        Product("shoe5", "Footwear", "Canvas sneakers", "casual", 27, "Ivory", 12, 0xFFf1ead9),
        Product("shoe6", "Footwear", "Strappy sandals", "casual", 24, "Mustard", 10, 0xFFc99a2e),
        Product("shoe7", "Footwear", "Slide sandals", "casual", 14, "Charcoal", 12, 0xFF3d3d3d),
        Product("shoe8", "Footwear", "White sneakers", "casual", 31, "Cream", 12, 0xFFf7f2e6),
        Product("shoe9", "Footwear", "House slippers", "lounge", 9, "Blush", 11, 0xFFe6c3b8),
        Product("shoe10", "Footwear", "Cosy slippers", "lounge", 11, "Olive", 11, 0xFF5d5e3a),
    )

    fun byId(id: String): Product? = items.firstOrNull { it.id == id }
}
