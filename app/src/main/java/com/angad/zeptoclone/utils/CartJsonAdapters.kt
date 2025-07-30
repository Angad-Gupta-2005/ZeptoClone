package com.angad.zeptoclone.utils

import android.util.Log
import com.angad.zeptoclone.data.models.fakeApi.CartItem
import com.angad.zeptoclone.data.models.fakeApi.Product
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

object CartJsonAdapters {
    private const val TAG = "CartJsonAdapters"

    //    Create a Gson instance with all cart-related type adapters registered
    fun createGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(CartItem::class.java, CartItemAdapter())
            .registerTypeAdapter(Product::class.java, ProductAdapter())
            .create()
    }

    //    Adapter for CartItem to handle serialization/deserialization
    private class CartItemAdapter : JsonSerializer<CartItem>, JsonDeserializer<CartItem> {
        override fun serialize(
            src: CartItem,
            typeOfSrc: Type,
            context: JsonSerializationContext
        ): JsonElement {
            val jsonObject = JsonObject()

            //    Serialize the product
            jsonObject.add("product", context.serialize(src.product))

            //    Add the quantity
            jsonObject.addProperty("quantity", src.quantity)

            return jsonObject
        }

        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): CartItem {
            try {
                val jsonObject = json.asJsonObject

                //    Deserialized the product
                val product = context.deserialize<Product>(
                    jsonObject.get("product"),
                    Product::class.java
                )

                //    Get the quantity
                val quantity = jsonObject.get("quantity").asInt

                return CartItem(product = product, quantity = quantity)
            } catch (e: Exception) {
                Log.e(TAG, "deserialize: ${e.message}", e)
                throw JsonParseException("Failed to parse CartItem", e)
            }
        }
    }

    //    Adapter for Product to handle serialization/deserialization
    private class ProductAdapter : JsonSerializer<Product>, JsonDeserializer<Product> {
        override fun serialize(
            src: Product,
            typeOfSrc: Type?,
            context: JsonSerializationContext?
        ): JsonElement {
            val jsonObject = JsonObject()

            //    Save all essential product data
            jsonObject.addProperty("id", src.id)
            jsonObject.addProperty("name", src.name)
            jsonObject.addProperty("price", src.price)
            jsonObject.addProperty("imageUrl", src.imageUrl ?: "")

            //    Save optional data if present
            src.category?.let { jsonObject.addProperty("category", it) }
            src.description?.let { jsonObject.addProperty("description", it) }
            src.imageRes.let { jsonObject.addProperty("imageRes", it) }

            //    Handle rating object if present
            src.rating?.let { rating ->
                val ratingObject = JsonObject()
                ratingObject.addProperty("rate", rating.rate)
                ratingObject.addProperty("count", rating.count)
                jsonObject.add("rating", ratingObject)
            }
            return jsonObject
        }

        override fun deserialize(
            json: JsonElement,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): Product {
            try {
                val jsonObject = json.asJsonObject

                //    Extract required fields with callbacks for potential issues
                val id = getIntSafely(jsonObject, "id", 0)
                val name = getStringSafely(jsonObject, "name", "Unknown Product")
//                val price = jsonObject.get("price").asDouble
                val price = getDoubleSafely(jsonObject, "price", 0.0)

                //   Extract the optional field
                val imageUrl = getStringSafely(jsonObject, "imageUrl", "")
                val category = getStringSafelyOrNull(jsonObject, "category")
                val description = getStringSafelyOrNull(jsonObject, "description")
                val imageRes = getIntSafely(jsonObject, "imageRes", 0)

                //    Handle rating object if present
                val rating = if (jsonObject.has("rating") && !jsonObject.get("rating").isJsonNull) {
                    try {
                        val ratingObj = jsonObject.getAsJsonObject("rating")
                        val rate = getFloatSafely(ratingObj, "rate", 0.0f)
                        val count = getIntSafely(ratingObj, "count", 0)
                    } catch (e: Exception) {
                        Log.w(TAG, "Error parsing rating object: ${e.message}", e)
                        null
                    }
                } else {
                    null
                }

                //    Construct and return the Product
                return Product(
                    id = id,
                    name = name,
                    price = price,
                    imageUrl = imageUrl,
                    category = category,
                    description = description,
                    imageRes = imageRes
                )
            } catch (e: Exception) {
                Log.e(TAG, "deserialize: ${e.message}", e)
                //    Return a fallback product to avoid crashing
                return Product(
                    id = 0,
                    name = "Unknown Product",
                    price = 0.0,
                    imageUrl = "",
                    category = "",
                    description = "",
                    imageRes = 0,
                    rating = null
                )
            }
        }

        //    Safe getter methods to handle potential JSON parsing issues
        private fun getStringSafely(
            jsonObject: JsonObject,
            key: String,
            defaultValue: String
        ): String {
            return try {
                if (jsonObject.has(key) && !jsonObject.get(key).isJsonNull) {
                    jsonObject.get(key).asString
                } else {
                    defaultValue
                }
            } catch (e: Exception) {
                defaultValue
            }
        }

        private fun getStringSafelyOrNull(
            jsonObject: JsonObject,
            key: String,
        ): String? {
            return try {
                if (jsonObject.has(key) && !jsonObject.get(key).isJsonNull) {
                    jsonObject.get(key).asString
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }

        private fun getIntSafely(jsonObject: JsonObject, key: String, defaultValue: Int): Int {
            return try {
                if (jsonObject.has(key) && !jsonObject.get(key).isJsonNull) {
                    jsonObject.get(key).asInt
                } else {
                    defaultValue
                }
            } catch (e: Exception) {
                Log.e(TAG, "getIntSafely: ${e.message}", e)
                defaultValue
            }
        }

        private fun getDoubleSafely(
            jsonObject: JsonObject,
            key: String,
            defaultValue: Double
        ): Double {
            return try {
                if (jsonObject.has(key) && !jsonObject.get(key).isJsonNull) {
                    jsonObject.get(key)
                        .asDouble
                } else {
                    defaultValue
                }
            } catch (e: Exception) {
                Log.e(TAG, "getDoubleSafely: ${e.message}", e)
                defaultValue
            }
        }

        private fun getFloatSafely(
            jsonObject: JsonObject,
            key: String,
            defaultValue: Float
        ): Float {
            return try {
                if (jsonObject.has(key) && !jsonObject.get(key).isJsonNull) {
                    jsonObject.get(key)
                        .asFloat
                } else {
                    defaultValue
                }
            } catch (e: Exception) {
                Log.e(TAG, "getFloatSafely: ${e.message}", e)
                defaultValue
            }
        }

    }

}