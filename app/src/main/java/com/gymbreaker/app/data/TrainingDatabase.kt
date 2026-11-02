package com.gymbreaker.app.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val category: String,
    val equipment: String,
    val level: String,
    val isDemoEligible: Boolean = true
)

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val occurredAtMillis: Long,
    val summary: String,
    val perceivedEffort: Int,
    val xpEarned: Int = 50
)

@Dao
interface TrainingDao {
    @Query("SELECT * FROM exercises ORDER BY name")
    fun observeExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun countExercises(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    @Query("SELECT * FROM sessions ORDER BY occurredAtMillis DESC")
    fun observeSessions(): Flow<List<SessionEntity>>

    @Insert
    suspend fun insertSession(session: SessionEntity)
}

@Database(entities = [ExerciseEntity::class, SessionEntity::class], version = 1, exportSchema = false)
abstract class TrainingDatabase : RoomDatabase() {
    abstract fun trainingDao(): TrainingDao

    companion object {
        @Volatile private var instance: TrainingDatabase? = null
        fun get(context: Context): TrainingDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    TrainingDatabase::class.java,
                    "gym_breaker_v1.db"
                ).build().also { instance = it }
            }
    }
}

class TrainingRepository(private val dao: TrainingDao) {
    val exercises = dao.observeExercises()
    val sessions = dao.observeSessions()

    suspend fun initializeSampleData() {
        if (dao.countExercises() > 0) return
        // Catálogo ilustrativo inicial, no los 2000 ejercicios definitivos.
        // La revisión editorial/técnica individual es obligatoria antes de ampliar el catálogo.
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

    suspend fun completeSession(summary: String, effort: Int) {
        require(summary.isNotBlank())
        require(effort in 1..10)
        dao.insertSession(
            SessionEntity(
                occurredAtMillis = System.currentTimeMillis(),
                summary = summary,
                perceivedEffort = effort
            )
        )
    }
}
