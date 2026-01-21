package com.adrencina.enchu.core.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.result.contract.ActivityResultContract

data class ContactData(
    val name: String,
    val phone: String,
    val email: String
)

/**
 * Contrato personalizado para seleccionar específicamente un número de teléfono.
 * Esto evita la necesidad de permisos READ_CONTACTS.
 */
class PickPhoneContact : ActivityResultContract<Unit, Uri?>() {
    override fun createIntent(context: Context, input: Unit): Intent {
        return Intent(Intent.ACTION_PICK).apply {
            type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (resultCode == Activity.RESULT_OK) intent?.data else null
    }
}

fun getContactDetails(context: Context, dataUri: Uri): ContactData {
    var name = ""
    var phone = ""
    // El email no se puede obtener de forma segura sin permisos usando este método,
    // ya que estamos consultando la tabla de teléfonos.
    val email = "" 

    val contentResolver = context.contentResolver

    // Consultamos la URI específica que nos devolvió el selector (que apunta a un teléfono)
    val cursor = contentResolver.query(dataUri, null, null, null, null)

    if (cursor != null && cursor.moveToFirst()) {
        val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

        if (nameIndex != -1) name = cursor.getString(nameIndex) ?: ""
        if (numberIndex != -1) phone = cursor.getString(numberIndex) ?: ""
        
        cursor.close()
    }
    
    return ContactData(name, phone, email)
}
