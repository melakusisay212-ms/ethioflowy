package et.ethioflow.domain

import et.ethioflow.calendar.EthiopianDate
import et.ethioflow.data.dao.*
import et.ethioflow.data.entity.*
import kotlinx.coroutines.flow.Flow
import java.time.ZoneId

class Repository(
    private val taskDao: TaskDao,
    private val projectDao: ProjectDao,
    private val linkDao: EntityLinkDao,
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao,
    private val noteDao: NoteDao,
    private val goalDao: GoalDao,
    private val transactionDao: TransactionDao,
    private val journalDao: JournalDao
) {
    // ---- Tasks ----
    fun observeTasks(): Flow<List<Task>> = taskDao.observeAll()
    fun observeActiveTasks(): Flow<List<Task>> = taskDao.observeActive()
    fun observeTasksOn(ethDate: EthiopianDate): Flow<List<Task>> =
        taskDao.observeByEthDate(ethDate.toString())

    suspend fun addTask(
        title: String, notes: String = "", ethDate: EthiopianDate? = null,
        priority: Priority = Priority.MEDIUM, projectId: Long? = null,
        reminderHour: Int? = null, reminderMinute: Int? = null
    ): Long {
        val dueAt = ethDate?.toGregorian()?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
        val reminderAt = if (ethDate != null && reminderHour != null) {
            ethDate.toGregorian().atTime(reminderHour, reminderMinute ?: 0)
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } else null
        return taskDao.insert(
            Task(title = title, notes = notes, priority = priority, projectId = projectId,
                dueEthDate = ethDate?.toString(), dueAtMillis = dueAt, reminderAtMillis = reminderAt)
        )
    }

    suspend fun updateTask(task: Task) = taskDao.update(task.copy(updatedAt = System.currentTimeMillis()))
    suspend fun deleteTask(task: Task) = taskDao.delete(task)
    suspend fun completeTask(task: Task) =
        taskDao.update(task.copy(status = TaskStatus.DONE, completedAt = System.currentTimeMillis()))

    // ---- Projects ----
    fun observeProjects(): Flow<List<Project>> = projectDao.observeActive()
    suspend fun addProject(title: String, description: String = "", colorHex: String = "#1B1F3B"): Long =
        projectDao.insert(Project(title = title, description = description, colorHex = colorHex))
    suspend fun updateProject(p: Project) = projectDao.update(p.copy(updatedAt = System.currentTimeMillis()))
    suspend fun deleteProject(p: Project) = projectDao.delete(p)

    // ---- Links ----
    suspend fun link(fromType: EntityType, fromId: Long, toType: EntityType, toId: Long, label: String = "") =
        linkDao.insert(EntityLink(fromType, fromId, toType, toId, label))
    suspend fun unlink(link: EntityLink) = linkDao.delete(link)
    fun observeLinksFrom(type: EntityType, id: Long) = linkDao.observeLinksFrom(type, id)
    suspend fun getLinksFor(type: EntityType, id: Long) = linkDao.getAllLinksFor(type, id)

    // ---- Habits ----
    fun observeHabits(): Flow<List<Habit>> = habitDao.observeActive()
    fun observeHabitLogsForDate(ethDate: String): Flow<List<HabitLog>> = habitLogDao.observeLogsForDate(ethDate)

    suspend fun addHabit(title: String, description: String = "", frequency: HabitFrequency = HabitFrequency.DAILY, colorHex: String = "#4CAF50"): Long =
        habitDao.insert(Habit(title = title, description = description, frequency = frequency, colorHex = colorHex))

    suspend fun updateHabit(habit: Habit) = habitDao.update(habit.copy(updatedAt = System.currentTimeMillis()))
    suspend fun deleteHabit(habit: Habit) = habitDao.delete(habit)

    suspend fun toggleHabitForDate(habitId: Long, ethDate: EthiopianDate): Boolean {
        val dateStr = ethDate.toString()
        val existing = habitLogDao.getLog(habitId, dateStr)
        return if (existing != null) {
            habitLogDao.deleteForDate(habitId, dateStr)
            recalculateStreak(habitId)
            false
        } else {
            habitLogDao.insert(HabitLog(habitId = habitId, ethDate = dateStr))
            recalculateStreak(habitId)
            true
        }
    }

    private suspend fun recalculateStreak(habitId: Long) {
        val habit = habitDao.getById(habitId) ?: return
        val logs = habitLogDao.getAllLogs(habitId).map { it.ethDate }.toSet()
        var streak = 0
        var d = EthiopianDate.now()
        if (d.toString() !in logs) d = d.minusDays(1)
        while (d.toString() in logs) { streak++; d = d.minusDays(1) }
        habitDao.update(habit.copy(streak = streak, bestStreak = maxOf(habit.bestStreak, streak), updatedAt = System.currentTimeMillis()))
    }

    // ---- Notes (PARA) ----
    fun observeNotes(): Flow<List<Note>> = noteDao.observeAll()
    fun observeNotesByPara(para: ParaType): Flow<List<Note>> = noteDao.observeByPara(para)

    suspend fun addNote(title: String, body: String = "", para: ParaType = ParaType.INBOX, tags: String = ""): Long =
        noteDao.insert(Note(title = title, body = body, paraType = para, tags = tags))

    suspend fun updateNote(note: Note) = noteDao.update(note.copy(updatedAt = System.currentTimeMillis()))
    suspend fun deleteNote(note: Note) = noteDao.delete(note)
    suspend fun moveNote(note: Note, to: ParaType) = noteDao.update(note.copy(paraType = to, updatedAt = System.currentTimeMillis()))

    // ---- Goals ----
    fun observeGoals(): Flow<List<Goal>> = goalDao.observeAll()
    fun observeActiveGoals(): Flow<List<Goal>> = goalDao.observeActive()

    suspend fun addGoal(title: String, description: String = "", targetDateEth: String? = null, colorHex: String = "#5B8DEF"): Long =
        goalDao.insert(Goal(title = title, description = description, targetDateEth = targetDateEth, colorHex = colorHex))

    suspend fun updateGoal(goal: Goal) = goalDao.update(goal.copy(updatedAt = System.currentTimeMillis()))
    suspend fun deleteGoal(goal: Goal) = goalDao.delete(goal)

    suspend fun linkTaskToGoal(taskId: Long, goalId: Long) =
        link(EntityType.TASK, taskId, EntityType.GOAL, goalId, "supports")

    suspend fun linkHabitToGoal(habitId: Long, goalId: Long) =
        link(EntityType.HABIT, habitId, EntityType.GOAL, goalId, "supports")

    /** Recalculate progress from linked completed tasks + habit logs */
    suspend fun refreshGoalProgress(goalId: Long) {
        val goal = goalDao.getById(goalId) ?: return
        val links = linkDao.getAllLinksFor(EntityType.GOAL, goalId)
        val taskIds = links.filter { it.fromType == EntityType.TASK || it.toType == EntityType.TASK }
            .map { if (it.fromType == EntityType.TASK) it.fromId else it.toId }
        val habitIds = links.filter { it.fromType == EntityType.HABIT || it.toType == EntityType.HABIT }
            .map { if (it.fromType == EntityType.HABIT) it.fromId else it.toId }

        var done = 0
        var total = taskIds.size + habitIds.size
        if (total == 0) return

        for (tid in taskIds) {
            val t = taskDao.getById(tid)
            if (t?.status == TaskStatus.DONE) done++
        }
        // Habits count as "done" if they have any log (simplified)
        for (hid in habitIds) {
            val logs = habitLogDao.getAllLogs(hid)
            if (logs.isNotEmpty()) done++
        }
        val progress = done.toFloat() / total
        goalDao.update(goal.copy(progress = progress, isCompleted = progress >= 1f, updatedAt = System.currentTimeMillis()))
    }

    // ---- Finance ----
    fun observeTransactions(): Flow<List<MoneyTransaction>> = transactionDao.observeAll()
    fun observeTransactionsOn(ethDate: String): Flow<List<MoneyTransaction>> = transactionDao.observeByDate(ethDate)

    suspend fun addTransaction(
        title: String, amount: Double, type: TxType, category: String = "Other",
        ethDate: EthiopianDate = EthiopianDate.now(), projectId: Long? = null, notes: String = ""
    ): Long = transactionDao.insert(
        MoneyTransaction(title = title, amount = amount, type = type, category = category,
            ethDate = ethDate.toString(), projectId = projectId, notes = notes)
    )

    suspend fun deleteTransaction(tx: MoneyTransaction) = transactionDao.delete(tx)

    // ---- Journal ----
    fun observeJournal(): Flow<List<JournalEntry>> = journalDao.observeAll()

    suspend fun getJournalFor(ethDate: EthiopianDate): JournalEntry? =
        journalDao.getByDate(ethDate.toString())

    suspend fun saveJournal(ethDate: EthiopianDate, mood: Int, body: String, highlights: String = ""): Long {
        val existing = journalDao.getByDate(ethDate.toString())
        return if (existing != null) {
            journalDao.update(existing.copy(mood = mood, body = body, highlights = highlights, updatedAt = System.currentTimeMillis()))
            existing.id
        } else {
            journalDao.insert(JournalEntry(ethDate = ethDate.toString(), mood = mood, body = body, highlights = highlights))
        }
    }

    // ---- Weekly Review data ----
    data class WeeklyReview(
        val from: EthiopianDate,
        val to: EthiopianDate,
        val tasksCompleted: List<Task>,
        val habitLogs: List<HabitLog>,
        val transactions: List<MoneyTransaction>,
        val journalEntries: List<JournalEntry>,
        val notesCreated: Int
    )

    suspend fun buildWeeklyReview(end: EthiopianDate = EthiopianDate.now()): WeeklyReview {
        val start = end.minusDays(6)
        val fromMs = start.toGregorian().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val toMs = end.toGregorian().atTime(23, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return WeeklyReview(
            from = start,
            to = end,
            tasksCompleted = taskDao.getCompletedBetween(fromMs, toMs),
            habitLogs = habitLogDao.getLogsBetween(start.toString(), end.toString()),
            transactions = transactionDao.getBetween(start.toString(), end.toString()),
            journalEntries = journalDao.getBetween(start.toString(), end.toString()),
            notesCreated = 0 // simplified
        )
    }
}
