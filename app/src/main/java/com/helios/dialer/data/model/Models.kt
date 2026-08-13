package com.helios.dialer.data.model

data class Contact(
    val id: String,
    val name: String,
    val number: String,
    val photoUri: String? = null
)

typealias ContactItem = Contact

data class CallLogEntry(
    val id: String,
    val number: String,
    val name: String?,
    val type: Int,
    val timestamp: Long,
    val duration: Long,
    val simSlot: Int = 0
)
