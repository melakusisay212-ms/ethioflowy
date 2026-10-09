package et.ethioflow.data.dao

import androidx.room.*
import et.ethioflow.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY dueAtMillis ASC, priority DESC")
    fun observeAll(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE dueEthDate = :ethDate ORDER BY priority DESC")
    fun observeByEthDate(ethDate: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE status != 'DONE' AND status != 'CANCELLED' ORDER BY dueAtMillis ASC")
    fun observeActive(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE status = 'DONE' AND completedAt >= :from AND completedAt <= :to")
    suspend fun getCompletedBetween(from: Long, to: Long): List<Task>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("SELECT * FROM tasks WHERE reminderAtMillis IS NOT NULL AND reminderAtMillis > :now")
    suspend fun getUpcomingReminders(now: Long): List<Task>
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects WHERE isArchived = 0 ORDER BY updatedAt DESC")
    fun observeActive(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getById(id: Long): Project?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: Project): Long

    @Update
    suspend fun update(project: Project)

    @Delete
    suspend fun delete(project: Project)
}

@Dao
interface EntityLinkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(link: EntityLink)

    @Delete
    suspend fun delete(link: EntityLink)

    @Query("SELECT * FROM entity_links WHERE fromType = :type AND fromId = :id")
    fun observeLinksFrom(type: EntityType, id: Long): Flow<List<EntityLink>>

    @Query("SELECT * FROM entity_links WHERE toType = :type AND toId = :id")
    fun observeLinksTo(type: EntityType, id: Long): Flow<List<EntityLink>>

    @Query("SELECT * FROM entity_links WHERE (fromType = :type AND fromId = :id) OR (toType = :type AND toId = :id)")
    suspend fun getAllLinksFor(type: EntityType, id: Long): List<EntityLink>
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY createdAt ASC")
    fun observeActive(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getById(id: Long): Habit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(habit: Habit): Long

    @Update
    suspend fun update(habit: Habit)

    @Delete
    suspend fun delete(habit: Habit)
}

@Dao
interface HabitLogDao {
    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND ethDate = :ethDate LIMIT 1")
    suspend fun getLog(habitId: Long, ethDate: String): HabitLog?

    @Query("SELECT * FROM habit_logs WHERE ethDate = :ethDate")
    fun observeLogsForDate(ethDate: String): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE ethDate >= :from AND ethDate <= :to")
    suspend fun getLogsBetween(from: String, to: String): List<HabitLog>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY ethDate DESC")
    suspend fun getAllLogs(habitId: Long): List<HabitLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: HabitLog): Long

    @Delete
    suspend fun delete(log: HabitLog)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND ethDate = :ethDate")
    suspend fun deleteForDate(habitId: Long, ethDate: String)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE paraType = :para ORDER BY updatedAt DESC")
    fun observeByPara(para: ParaType): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: Long): Note?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE isCompleted = 0 ORDER BY updatedAt DESC")
    fun observeActive(): Flow<List<Goal>>

    @Query("SELECT * FROM goals ORDER BY isCompleted ASC, updatedAt DESC")
    fun observeAll(): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getById(id: Long): Goal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: Goal): Long

    @Update
    suspend fun update(goal: Goal)

    @Delete
    suspend fun delete(goal: Goal)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY ethDate DESC, createdAt DESC")
    fun observeAll(): Flow<List<MoneyTransaction>>

    @Query("SELECT * FROM transactions WHERE ethDate = :ethDate ORDER BY createdAt DESC")
    fun observeByDate(ethDate: String): Flow<List<MoneyTransaction>>

    @Query("SELECT * FROM transactions WHERE ethDate >= :from AND ethDate <= :to ORDER BY ethDate DESC")
    suspend fun getBetween(from: String, to: String): List<MoneyTransaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tx: MoneyTransaction): Long

    @Update
    suspend fun update(tx: MoneyTransaction)

    @Delete
    suspend fun delete(tx: MoneyTransaction)
}

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries ORDER BY ethDate DESC")
    fun observeAll(): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE ethDate = :ethDate LIMIT 1")
    suspend fun getByDate(ethDate: String): JournalEntry?

    @Query("SELECT * FROM journal_entries WHERE ethDate >= :from AND ethDate <= :to ORDER BY ethDate DESC")
    suspend fun getBetween(from: String, to: String): List<JournalEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: JournalEntry): Long

    @Update
    suspend fun update(entry: JournalEntry)

    @Delete
    suspend fun delete(entry: JournalEntry)
}
