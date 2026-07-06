package com.mimokassy.data.models

enum class ProgramType {
    SIMPLE, COMPLEX
}

data class Program(
    val id: String,
    val name: String,
    val description: String?,
    val type: ProgramType,
    val durationMin: Int,
    val menuDescription: String?
) {
    val capacityCap: Int
        get() = if (type == ProgramType.SIMPLE) 12 else 8

    val displayName: String
        get() = if (type == ProgramType.SIMPLE) "Простая" else "Сложная"
}
