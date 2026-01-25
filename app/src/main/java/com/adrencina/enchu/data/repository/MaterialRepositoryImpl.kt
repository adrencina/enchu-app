package com.adrencina.enchu.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.adrencina.enchu.data.local.MaterialDao
import com.adrencina.enchu.data.model.MaterialEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MaterialRepositoryImpl @Inject constructor(
    private val materialDao: MaterialDao,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    @ApplicationContext private val context: Context
) : MaterialRepository {

    private val MATERIALS_VERSION_KEY = intPreferencesKey("materials_version")
    private val gson = Gson()

    // Fallback local dummy data for immediate testing
    private val DUMMY_JSON = """
    [
      {"name": "Cable Unipolar 2.5mm Rojo", "category": "Conductores", "unit": "m", "keywords": "fase, linea, cable"},
      {"name": "Cable Unipolar 2.5mm Celeste", "category": "Conductores", "unit": "m", "keywords": "neutro, cable"},
      {"name": "Cable Unipolar 1.5mm Verde/Amarillo", "category": "Conductores", "unit": "m", "keywords": "tierra, cable"},
      {"name": "Térmica Bipolar 10A", "category": "Protecciones", "unit": "u", "keywords": "termica, llave, corte"},
      {"name": "Térmica Bipolar 16A", "category": "Protecciones", "unit": "u", "keywords": "termica, llave, corte"},
      {"name": "Térmica Bipolar 20A", "category": "Protecciones", "unit": "u", "keywords": "termica, llave, corte"},
      {"name": "Disyuntor Diferencial 25A", "category": "Protecciones", "unit": "u", "keywords": "disyuntor, salvavita, corte"},
      {"name": "Caja Rectangular Chapa", "category": "Cajas", "unit": "u", "keywords": "caja, luz, chapa"},
      {"name": "Bastidor 10x5", "category": "Módulos", "unit": "u", "keywords": "bastidor, soporte"},
      {"name": "Punto Simple", "category": "Módulos", "unit": "u", "keywords": "tecla, punto, llave"},
      {"name": "Tomacorriente Doble", "category": "Módulos", "unit": "u", "keywords": "toma, enchufe"}
    ]
    """

    override fun searchMaterials(query: String): Flow<List<MaterialEntity>> {
        return materialDao.searchMaterials(query)
    }

    override suspend fun getMaterialCount(): Int {
        return materialDao.getCount()
    }

    override suspend fun syncMaterials() {
        withContext(Dispatchers.IO) {
            // 0. Immediate Fallback: If DB is empty, populate with dummy data RIGHT NOW
            // This ensures the user has data instantly, even before checking network
            if (materialDao.getCount() == 0) {
                try {
                    val type = object : TypeToken<List<MaterialEntity>>() {}.type
                    val materials: List<MaterialEntity> = gson.fromJson(DUMMY_JSON, type)
                    materialDao.insertAll(materials)
                    
                    // Set version to 1 to mark as initialized
                    context.dataStore.edit { preferences ->
                        preferences[MATERIALS_VERSION_KEY] = 1
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            try {
                // 1. Check Remote Version (Cloud)
                // If this fails (e.g. offline or doc doesn't exist), we catch and verify if local DB is empty
                val configDoc = firestore.collection("config").document("metadata").get().await()
                
                val remoteVersion = if (configDoc.exists()) {
                    configDoc.getLong("materialsVersion")?.toInt() ?: 1
                } else {
                    // Default to 1 if config not found, forcing a sync if local is 0
                    1 
                }
                
                // 2. Check Local Version (DataStore)
                val localVersion = context.dataStore.data.map { preferences ->
                    preferences[MATERIALS_VERSION_KEY] ?: 0
                }.first()

                // 3. Sync if needed
                if (remoteVersion > localVersion) {
                    val materials = try {
                         // Try downloading from Storage
                        val storageRef = storage.reference.child("config/master_materials.json")
                        val localFile = File.createTempFile("materials", "json")
                        
                        storageRef.getFile(localFile).await()
                        
                        val jsonString = localFile.readText()
                        localFile.delete()
                        
                        val type = object : TypeToken<List<MaterialEntity>>() {}.type
                        gson.fromJson<List<MaterialEntity>>(jsonString, type)
                    } catch (e: Exception) {
                        // If download fails, we already have dummy data or old data, so just log/ignore
                        // unless we want to retry logic.
                        throw e 
                    }

                    // 4. Update Room
                    materialDao.clearAll()
                    materialDao.insertAll(materials)

                    // 5. Update Local Version
                    context.dataStore.edit { preferences ->
                        preferences[MATERIALS_VERSION_KEY] = remoteVersion
                    }
                }
            } catch (e: Exception) {
                // Network error or other sync issues. 
                // We already populated dummy data at step 0 if needed, so nothing critical to do here.
                e.printStackTrace()
            }
        }
    }
}
