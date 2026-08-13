package com.helios.dialer.data.repository

import android.content.Context
import android.provider.ContactsContract
import com.helios.dialer.data.model.Contact
import com.helios.dialer.domain.T9SearchEngine

class ContactsRepository(private val context: Context) {
    fun getContacts(): List<Contact> {
        val list = mutableListOf<Contact>()
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
        )

        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} COLLATE LOCALIZED ASC"
        )?.use { cursor ->
            val idIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val photoIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

            while (cursor.moveToNext()) {
                val id = if (idIdx >= 0) cursor.getString(idIdx) else ""
                val name = if (nameIdx >= 0) cursor.getString(nameIdx).orEmpty().ifBlank { "Unknown" } else "Unknown"
                val number = if (numberIdx >= 0) cursor.getString(numberIdx).orEmpty() else ""
                val photo = if (photoIdx >= 0) cursor.getString(photoIdx) else null
                if (number.isNotBlank()) list += Contact(id, name, number, photo)
            }
        }

        return list.distinctBy {
            it.id to T9SearchEngine.normalizeNumber(it.number)
        }
    }

    fun findName(number: String): String? {
        val normalized = T9SearchEngine.normalizeNumber(number)
        if (normalized.isBlank()) return null
        return getContacts().firstOrNull {
            val candidate = T9SearchEngine.normalizeNumber(it.number)
            candidate == normalized || candidate.takeLast(10) == normalized.takeLast(10)
        }?.name
    }
}
