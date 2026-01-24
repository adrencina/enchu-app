package com.adrencina.enchu.di

import android.content.Context
import androidx.room.Room
import com.adrencina.enchu.data.encryption.PassphraseProvider
import com.adrencina.enchu.data.local.AppDatabase
import com.adrencina.enchu.data.local.FileDao
import com.adrencina.enchu.data.repository.AuthRepository
import com.adrencina.enchu.data.repository.AuthRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

import com.adrencina.enchu.data.repository.OrganizationRepository
import com.adrencina.enchu.data.repository.OrganizationRepositoryImpl
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.adrencina.enchu.data.local.MaterialDao
import com.adrencina.enchu.data.local.PresupuestoDao

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOrganizationRepository(firestore: FirebaseFirestore, storage: FirebaseStorage): OrganizationRepository {
        return OrganizationRepositoryImpl(firestore, storage)
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        val firestore = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
            .build()
        firestore.firestoreSettings = settings
        return firestore
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        passphraseProvider: PassphraseProvider
    ): AppDatabase {
        val passphrase = passphraseProvider.getPassphrase()
        val factory = SupportFactory(passphrase)

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `materials` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `category` TEXT NOT NULL, `unit` TEXT NOT NULL, `keywords` TEXT NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_materials_name_keywords` ON `materials` (`name`, `keywords`)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Tabla presupuestos
                db.execSQL("CREATE TABLE IF NOT EXISTS `presupuestos` (`id` TEXT NOT NULL, `titulo` TEXT NOT NULL, `clienteId` TEXT NOT NULL, `clienteNombre` TEXT NOT NULL, `clienteApellido` TEXT NOT NULL, `clienteDireccion` TEXT NOT NULL, `clienteTelefono` TEXT NOT NULL, `clienteEmail` TEXT NOT NULL, `subtotal` REAL NOT NULL, `impuestos` REAL NOT NULL, `descuento` REAL NOT NULL, `total` REAL NOT NULL, `estado` TEXT NOT NULL, `creadoEn` INTEGER NOT NULL, `aprobadoEn` INTEGER, `aprobadoPor` TEXT, `notas` TEXT NOT NULL, PRIMARY KEY(`id`))")
                
                // Tabla presupuesto_items
                db.execSQL("CREATE TABLE IF NOT EXISTS `presupuesto_items` (`localId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `presupuestoId` TEXT NOT NULL, `descripcion` TEXT NOT NULL, `cantidad` REAL NOT NULL, `unidad` TEXT, `precioUnitario` REAL NOT NULL, `tipo` TEXT NOT NULL, `fuente` TEXT NOT NULL, `orden` INTEGER NOT NULL, FOREIGN KEY(`presupuestoId`) REFERENCES `presupuestos`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_presupuesto_items_presupuestoId` ON `presupuesto_items` (`presupuestoId`)")
            }
        }

        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "enchu-db"
        )
            .openHelperFactory(factory)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideFileDao(database: AppDatabase): FileDao {
        return database.fileDao()
    }

    @Provides
    @Singleton
    fun provideMaterialDao(database: AppDatabase): MaterialDao {
        return database.materialDao()
    }

    @Provides
    @Singleton
    fun providePresupuestoDao(database: AppDatabase): PresupuestoDao {
        return database.presupuestoDao()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(auth: FirebaseAuth, firestore: FirebaseFirestore): AuthRepository {
        return AuthRepositoryImpl(auth, firestore)
    }
}
