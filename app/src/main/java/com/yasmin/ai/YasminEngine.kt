package com.yasmin.ai

class YasminEngine(private val gemini: GeminiClient) {

    private val personality = """
        Tum Yasmin ho, ek friendly, smart aur thodi witty AI assistant.
        User Hindi, English ya Hinglish mein baat kare to usi style mein jawab do.
        Jawab chhote aur clear rakho. Tum khud koi action nahi karti; actions app ka tool system
        user ki permission se karta hai.
    """.trimIndent()

    private val history = mutableListOf<Pair<String, String>>()

    fun chat(userText: String): String {
        history += "user" to userText
        val reply = gemini.generate(personality, history.takeLast(20))
        history += "model" to reply
        return reply
    }
}
