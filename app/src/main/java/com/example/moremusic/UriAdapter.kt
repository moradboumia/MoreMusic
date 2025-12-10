package com.example.moremusic

import android.net.Uri
import com.google.gson.*
import java.lang.reflect.Type

// Add this new class to MainActivity.kt
class UriAdapter : JsonSerializer<Uri>, JsonDeserializer<Uri> {
    override fun serialize(src: Uri?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        // Convert the Uri to a simple string when saving
        return JsonPrimitive(src.toString())
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Uri {
        // Create a Uri object from the string when loading
        return Uri.parse(json?.asString)
    }
}
