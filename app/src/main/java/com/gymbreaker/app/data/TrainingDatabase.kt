package com.gymbreaker.app.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val category: String,
    val equipment: String,
    val level: String,
    // Marca provisional del catálogo de demostración; no constituye validación clínica.
    val isDemoEligible: Boolean = true
)

/**
 * Variante subordinada: nunca contabilizada como otro ejercicio principal.
 * Todas las variantes apuntan al ID local del ejercicio canónico en Room.
 */
@Entity(
    tableName = "exercise_variants",
    primaryKeys = ["exerciseId", "variantId"],
    foreignKeys = [ForeignKey(
        entity = ExerciseEntity::class,
        parentColumns = ["id"],
        childColumns = ["exerciseId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["exerciseId"])]
)
data class ExerciseVariantEntity(
    val exerciseId: Int,
    val variantId: String,
    val name: String,
    val equipment: String,
    val sourceRefsCsv: String
)

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val occurredAtMillis: Long,
    val summary: String,
    val perceivedEffort: Int,
    val xpEarned: Int = 50,
    val exerciseIdsCsv: String = "",
    val equipment: String = "HOME",
    val confirmedComplete: Boolean = true,
    val reportedDiscomfort: Boolean = false
)

@Entity(tableName = "training_profile")
data class TrainingProfileEntity(
    @PrimaryKey val id: Int = 1,
    val experience: String = "BEGINNER",
    val availableMinutes: Int = 25,
    val excludedCategoriesCsv: String = "",
    val excludedExerciseIdsCsv: String = "",
    val requiresReview: Boolean = false
)

@Dao
interface TrainingDao {
    @Query("SELECT * FROM exercises ORDER BY name")
    fun observeExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun countExercises(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertVariants(variants: List<ExerciseVariantEntity>)

    @Query("SELECT * FROM exercise_variants WHERE exerciseId = :exerciseId ORDER BY name")
    fun observeVariants(exerciseId: Int): Flow<List<ExerciseVariantEntity>>

    @Query("SELECT COUNT(*) FROM exercise_variants")
    suspend fun countVariants(): Int

    @Query("SELECT * FROM sessions ORDER BY occurredAtMillis DESC")
    fun observeSessions(): Flow<List<SessionEntity>>

    @Insert
    suspend fun insertSession(session: SessionEntity)

    @Query("SELECT * FROM training_profile WHERE id = 1")
    fun observeProfile(): Flow<TrainingProfileEntity?>

    @Query("SELECT * FROM training_profile WHERE id = 1")
    suspend fun getProfile(): TrainingProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: TrainingProfileEntity)
}

@Database(
    entities = [
        ExerciseEntity::class, ExerciseVariantEntity::class,
        SessionEntity::class, TrainingProfileEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class TrainingDatabase : RoomDatabase() {
    abstract fun trainingDao(): TrainingDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE sessions ADD COLUMN exerciseIdsCsv TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE sessions ADD COLUMN equipment TEXT NOT NULL DEFAULT 'HOME'")
                db.execSQL("ALTER TABLE sessions ADD COLUMN confirmedComplete INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE sessions ADD COLUMN reportedDiscomfort INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS training_profile (" +
                    "id INTEGER NOT NULL PRIMARY KEY, " +
                    "experience TEXT NOT NULL, " +
                    "availableMinutes INTEGER NOT NULL, " +
                    "excludedCategoriesCsv TEXT NOT NULL, " +
                    "excludedExerciseIdsCsv TEXT NOT NULL, " +
                    "requiresReview INTEGER NOT NULL)"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `exercise_variants` (" +
                        "`exerciseId` INTEGER NOT NULL, " +
                        "`variantId` TEXT NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`equipment` TEXT NOT NULL, " +
                        "`sourceRefsCsv` TEXT NOT NULL, " +
                        "PRIMARY KEY(`exerciseId`, `variantId`), " +
                        "FOREIGN KEY(`exerciseId`) REFERENCES `exercises`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_exercise_variants_exerciseId` " +
                        "ON `exercise_variants` (`exerciseId`)"
                )
            }
        }

        @Volatile private var instance: TrainingDatabase? = null

        fun get(context: Context): TrainingDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    TrainingDatabase::class.java,
                    "gym_breaker_v1.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build().also { instance = it }
            }
    }
}

class TrainingRepository(private val dao: TrainingDao, private val context: Context) {
    val exercises = dao.observeExercises()
    val sessions = dao.observeSessions()
    val profile = dao.observeProfile()

    suspend fun initializeSampleData() {
        if (dao.getProfile() == null) dao.upsertProfile(TrainingProfileEntity())
        if (dao.countExercises() == 0) {
        // 12 ejemplos iniciales: NO representan el catálogo final de 2000 fichas verificadas.
        dao.insertExercises(
            listOf(
                ExerciseEntity(1, "Caminar en el lugar", "Cardio", "none", "inicio"),
                ExerciseEntity(2, "Movilidad de hombros", "Movilidad", "none", "inicio"),
                ExerciseEntity(3, "Sentarse y levantarse de una silla", "Piernas", "none", "inicio"),
                ExerciseEntity(4, "Elevaciones suaves de talones", "Piernas", "none", "inicio"),
                ExerciseEntity(5, "Movilidad de cadera", "Movilidad", "none", "inicio"),
                ExerciseEntity(6, "Equilibrio con apoyo", "Estabilidad", "none", "inicio"),
                ExerciseEntity(7, "Remo ligero con mancuernas", "Espalda", "dumbbell", "intermedio"),
                ExerciseEntity(8, "Elevación lateral ligera", "Hombros", "dumbbell", "intermedio"),
                ExerciseEntity(9, "Curl con mancuernas", "Brazos", "dumbbell", "intermedio"),
                ExerciseEntity(10, "Prensa de piernas en máquina", "Piernas", "gym", "intermedio"),
                ExerciseEntity(11, "Remo sentado en máquina", "Espalda", "gym", "intermedio"),
                ExerciseEntity(12, "Bicicleta estática", "Cardio", "gym", "inicio")
            )
        )
        }
        // Solo incorpora fichas procedentes de assets validados; jamás importa
        // JSON de investigación. Actualmente el catálogo aprobado está vacío.
        val approvedFamilies = ApprovedCatalogLoader.loadFamilies(context)
        dao.insertExercises(ApprovedCatalogLoader.toExerciseEntities(approvedFamilies))
        // La relación familiar se mantiene también en SQLite, sin inflar
        // el contador de ejercicios principales.
        dao.insertVariants(ApprovedCatalogLoader.toVariantEntities(approvedFamilies))
    }

    suspend fun saveProfile(profile: TrainingProfileEntity) {
        require(profile.id == 1 && profile.availableMinutes in 10..90)
        dao.upsertProfile(profile)
    }

    suspend fun completeSession(
        summary: String,
        effort: Int,
        equipment: String = "HOME",
        exerciseIds: List<Int> = emptyList(),
        confirmed: Boolean = true,
        discomfort: Boolean = false
    ) {
        require(summary.isNotBlank())
        require(effort in 1..10)
        require(equipment in setOf("HOME", "DUMBBELLS", "GYM"))
        dao.insertSession(
            SessionEntity(
                occurredAtMillis = System.currentTimeMillis(),
                summary = summary,
                perceivedEffort = effort,
                equipment = equipment,
                exerciseIdsCsv = exerciseIds.distinct().joinToString(","),
                confirmedComplete = confirmed,
                reportedDiscomfort = discomfort,
                xpEarned = if (confirmed) 50 else 0
            )
        )
    }
}
