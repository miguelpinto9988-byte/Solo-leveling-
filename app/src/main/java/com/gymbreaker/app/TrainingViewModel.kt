package com.gymbreaker.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gymbreaker.app.data.ExerciseEntity
import com.gymbreaker.app.data.SessionEntity
import com.gymbreaker.app.data.TrainingDatabase
import com.gymbreaker.app.data.TrainingRepository
import com.gymbreaker.app.domain.HunterProgression
import com.gymbreaker.app.domain.LocalAssistant
import com.gymbreaker.app.domain.PlannedRoutine
import com.gymbreaker.app.domain.RoutinePlanner
import com.gymbreaker.app.domain.TrainingPlace
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardState(
    val exercises: List<ExerciseEntity> = emptyList(),
    val sessions: List<SessionEntity> = emptyList(),
    val routine: PlannedRoutine? = null,
    val companion: String = "Beru",
    val xp: Int = 0,
    val level: Int = 1,
    val rank: String = "E",
    val weeklySessions: Int = 0
)

class TrainingViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("hunter_preferences", 0)
    private val repository = TrainingRepository(TrainingDatabase.get(application).trainingDao())
    private val currentRoutine = MutableStateFlow<PlannedRoutine?>(null)
    private val selectedCompanion = MutableStateFlow(prefs.getString("companion", "Beru") ?: "Beru")

    val state: StateFlow<DashboardState> = combine(
        repository.exercises,
        repository.sessions,
        currentRoutine,
        selectedCompanion
    ) { exercises, sessions, routine, companion ->
        val xp = sessions.sumOf { it.xpEarned }
        val startOfWeek = LocalDate.now().with(
            TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
        ).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        DashboardState(
            exercises = exercises,
            sessions = sessions,
            routine = routine,
            companion = companion,
            xp = xp,
            level = HunterProgression.level(xp),
            rank = HunterProgression.rank(xp),
            weeklySessions = sessions.count { it.occurredAtMillis >= startOfWeek }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardState())

    init { viewModelScope.launch { repository.initializeSampleData() } }

    fun generateRoutine(place: TrainingPlace) {
        currentRoutine.value = RoutinePlanner.generate(state.value.exercises, place)
    }

    fun saveSession(effort: Int) {
        val routine = currentRoutine.value ?: return
        if (routine.exercises.isEmpty()) return
        viewModelScope.launch {
            repository.completeSession(routine.exercises.joinToString(", ") { it.name }, effort)
            currentRoutine.value = null
        }
    }

    fun chooseCompanion(name: String) {
        if (name !in setOf("Beru", "Igris", "Iron", "Tank", "Tusk", "Kaisel")) return
        prefs.edit().putString("companion", name).apply()
        selectedCompanion.value = name
    }

    fun askAssistant(question: String): String {
        val s = state.value
        return LocalAssistant.answer(question, s.sessions.size, s.xp)
    }
}
