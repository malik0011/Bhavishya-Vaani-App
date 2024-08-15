package com.example.astrochart.data


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class GeminiPredictionResponse(
    var title: String = "",
    var analysis: Analysis = Analysis()
): java.io.Serializable {
    @Keep
    data class Analysis(
        var career: Career = Career()
    ) {
        @Keep
        data class Career(
            var description: String = "",
            @SerializedName("suitable_fields") var suitableFields: List<String> = listOf(),
            var strengths: List<String> = listOf(),
            var challenges: List<String> = listOf(),
            var solutions: List<String> = listOf(),
            @SerializedName("success_strategies") var successStrategies: List<String> = listOf()
        )
    }
}