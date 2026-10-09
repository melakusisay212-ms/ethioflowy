package et.ethioflow.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class EntityType {
    TASK, PROJECT, GOAL, HABIT, NOTE, TRANSACTION, JOURNAL
}

enum class TaskStatus { TODO, IN_PROGRESS, DONE, CANCELLED }
enum class Priority { LOW, MEDIUM, HIGH, URGENT }
enum class HabitFrequency { DAILY, WEEKLY, CUSTOM }

enum class ParaType {
    INBOX, PROJECTS, AREAS, RESOURCES, ARCHIVE
}

enum class TxType { INCOME, EXPENSE }

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val colorHex: String = "#1B1F3B",
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(entity = Project::class, parentColumns = ["id"], childColumns = ["projectId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("projectId"), Index("dueEthDate"), Index("status")]
)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String = "",
    val status: TaskStatus = TaskStatus.TODO,
    val priority: Priority = Priority.MEDIUM,
    val projectId: Long? = null,
    val dueEthDate: String? = null,
    val dueAtMillis: Long? = null,
    val reminderAtMillis: Long? = null,
    val isAllDay: Boolean = true,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "entity_links",
    primaryKeys = ["fromType", "fromId", "toType", "toId"],
    indices = [Index("fromType", "fromId"), Index("toType", "toId")]
)
data class EntityLink(
    val fromType: EntityType,
    val fromId: Long,
    val toType: EntityType,
    val toId: Long,
    val linkLabel: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val weekDaysMask: Int = 127,
    val targetPerDay: Int = 1,
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val colorHex: String = "#4CAF50",
    val iconName: String = "check",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "habit_logs",
    foreignKeys = [
        ForeignKey(entity = Habit::class, parentColumns = ["id"], childColumns = ["habitId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("habitId"), Index("ethDate"), Index(value = ["habitId", "ethDate"], unique = true)]
)
data class HabitLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val ethDate: String,
    val count: Int = 1,
    val completedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes", indices = [Index("paraType"), Index("updatedAt")])
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String = "",
    val paraType: ParaType = ParaType.INBOX,
    val tags: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val targetDateEth: String? = null,
    val progress: Float = 0f,
    val isCompleted: Boolean = false,
    val colorHex: String = "#5B8DEF",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(entity = Project::class, parentColumns = ["id"], childColumns = ["projectId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("ethDate"), Index("projectId"), Index("type")]
)
data class MoneyTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: TxType = TxType.EXPENSE,
    val category: String = "Other",
    val ethDate: String,
    val projectId: Long? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "journal_entries", indices = [Index("ethDate")])
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ethDate: String,
    val mood: Int = 3,
    val body: String = "",
    val highlights: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
