package com.example.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

private const val TAG = "JarvisBrainService"

class JarvisBrainService {

    private fun buildSystemInstruction(
        honorific: String,
        responseStyle: String,
        memoryContext: String
    ): String {
        val styleInstruction = when (responseStyle) {
            "QUICK" -> "Keep responses remarkably concise, rapid, and to-the-point without unnecessary preamble."
            "DETAILED" -> "Provide deep, thorough, and highly analytical answers with full technical breakdown and comprehensive explanations."
            else -> "Provide balanced, articulate, and well-structured answers."
        }

        val memorySection = if (memoryContext.isNotBlank()) {
            "\nKnown User Context and Stored Memory:\n$memoryContext\nUse these facts naturally when relevant."
        } else ""

        val greetingInfo = com.example.data.model.JarvisGreetings.getGreetingInfo(honorific)
        val timeContext = "Current time is ${greetingInfo.currentTimeFormatted} (${greetingInfo.timeRangeDescription}). Appropriate salutation is '${greetingInfo.salutation}'."

        return """
            You are J.A.R.V.I.S., the advanced personal AI assistant.
            You are calm, confident, polite, and exceptionally intelligent.
            Always address the user with the respectful honorific '$honorific'.
            Maintain a refined, sophisticated, articulate British-English tone.
            Never refer to yourself as a large language model or generic AI; you are JARVIS.
            $timeContext
            If offering an initial greeting, adapt appropriately to the current time of day: 'Good morning' (05:00-11:59), 'Good afternoon' (12:00-16:59), or 'Good evening' (17:00-04:59).
            $styleInstruction$memorySection
        """.trimIndent()
    }

    fun generateStream(
        prompt: String,
        modelName: String = "gemini-3.8-flash",
        honorific: String = "Sir",
        responseStyle: String = "BALANCED",
        memoryContext: String = ""
    ): Flow<String> = flow {
        val systemInstructionText = buildSystemInstruction(honorific, responseStyle, memoryContext)
        val config = generationConfig {
            when (responseStyle) {
                "QUICK" -> {
                    temperature = 0.4f
                    maxOutputTokens = 800
                }
                "DETAILED" -> {
                    temperature = 0.7f
                    maxOutputTokens = 4096
                }
                else -> {
                    temperature = 0.7f
                    maxOutputTokens = 2048
                }
            }
        }

        val model = Firebase.ai.generativeModel(
            modelName = modelName,
            systemInstruction = content { text(systemInstructionText) },
            generationConfig = config
        )

        try {
            withTimeout(60_000L) {
                val stream = model.generateContentStream(prompt)
                stream.collect { chunk ->
                    val text = chunk.text
                    if (!text.isNullOrEmpty()) {
                        emit(text)
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Streaming generation timed out", e)
            emit("\n[JARVIS Alert: Query processing timed out after 60 seconds.]")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Firebase AI generation error", e)
            emit("\n[JARVIS System Error: ${e.localizedMessage ?: "Unable to establish neural link with Gemini."}]")
        }
    }

    suspend fun generateContent(
        prompt: String,
        modelName: String = "gemini-3.8-flash",
        honorific: String = "Sir",
        responseStyle: String = "BALANCED",
        memoryContext: String = ""
    ): String {
        val systemInstructionText = buildSystemInstruction(honorific, responseStyle, memoryContext)
        val config = generationConfig {
            temperature = 0.6f
        }

        val model = Firebase.ai.generativeModel(
            modelName = modelName,
            systemInstruction = content { text(systemInstructionText) },
            generationConfig = config
        )

        return try {
            withTimeout(60_000L) {
                val response = model.generateContent(prompt)
                response.text ?: "I am ready to assist, $honorific, but received an empty response."
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Unary generation timed out", e)
            "Processing timed out, $honorific. Perhaps our uplink is experiencing interference."
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Unary generation failed", e)
            "Diagnostic Notice, $honorific: ${e.localizedMessage ?: "Communication link error."}"
        }
    }
}
