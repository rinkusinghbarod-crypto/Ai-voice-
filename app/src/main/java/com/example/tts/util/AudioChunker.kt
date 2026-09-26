package com.example.tts.util

object AudioChunker {

    private const val TARGET_CHUNK_WORD_COUNT = 90
    private const val MAX_CHUNK_WORD_COUNT = 140

    /**
     * Splits a long story into smaller natural storytelling sections.
     * Preserves sentences and paragraph rhythm without unnatural breaks.
     */
    fun chunkStory(script: String): List<String> {
        val trimmed = script.trim()
        if (trimmed.isEmpty()) return emptyList()

        val words = trimmed.split("\\s+".toRegex())
        if (words.size <= MAX_CHUNK_WORD_COUNT) {
            return listOf(trimmed)
        }

        val chunks = mutableListOf<String>()
        val paragraphs = trimmed.split("\n+".toRegex()).filter { it.isNotBlank() }

        var currentChunk = StringBuilder()
        var currentWordCount = 0

        for (paragraph in paragraphs) {
            val paragraphWords = paragraph.split("\\s+".toRegex()).size

            // If adding the whole paragraph stays within target
            if (currentWordCount + paragraphWords <= TARGET_CHUNK_WORD_COUNT) {
                if (currentChunk.isNotEmpty()) currentChunk.append(" ")
                currentChunk.append(paragraph.trim())
                currentWordCount += paragraphWords
            } else {
                // Split paragraph by sentences
                val sentences = splitIntoSentences(paragraph)
                for (sentence in sentences) {
                    val sentenceWords = sentence.split("\\s+".toRegex()).filter { it.isNotBlank() }.size

                    if (currentWordCount + sentenceWords > TARGET_CHUNK_WORD_COUNT && currentWordCount > 0) {
                        chunks.add(currentChunk.toString().trim())
                        currentChunk = StringBuilder()
                        currentWordCount = 0
                    }

                    if (currentChunk.isNotEmpty()) currentChunk.append(" ")
                    currentChunk.append(sentence.trim())
                    currentWordCount += sentenceWords
                }
            }
        }

        if (currentChunk.isNotEmpty()) {
            chunks.add(currentChunk.toString().trim())
        }

        return if (chunks.isEmpty()) listOf(trimmed) else chunks
    }

    private fun splitIntoSentences(text: String): List<String> {
        // Regex for sentence split keeping delimiters
        val regex = Regex("(?<=[.!?])\\s+")
        return text.split(regex).map { it.trim() }.filter { it.isNotBlank() }
    }
}
