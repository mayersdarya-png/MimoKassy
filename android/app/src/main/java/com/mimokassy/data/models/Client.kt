package com.mimokassy.data.models

import java.util.UUID

data class Client(
    val id: UUID,
    val name: String?,
    val phone: String,
    val createdAt: String
)
