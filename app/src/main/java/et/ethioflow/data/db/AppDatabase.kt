package et.ethioflow.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import et.ethioflow.data.dao.*
import et.ethioflow.data.entity.*

class Converters {
    @TypeConverter fun fromTaskStatus(v: TaskStatus): String = v.name
    @TypeConverter fun toTaskStatus(v: String): TaskStatus = TaskStatus.valueOf(v)
    @TypeConverter fun fromPriority(v: Priority): String = v.name
    @TypeConverter fun toPriority(v: String): Priority = Priority.valueOf(v)
    @TypeConverter fun fromEntityType(v: EntityType): String = v.name
    @TypeConverter fun toEntityType(v: String): EntityType = EntityType.valueOf(v)
    @TypeConverter fun fromHabitFrequency(v: HabitFrequency): String = v.name
    @TypeConverter fun toHabitFrequency(v: String): HabitFrequency = HabitFrequency.valueOf(v)
    @TypeConverter fun fromParaType(v: ParaType): String = v.name
    @TypeConverter fun toParaType(v: String): ParaType = ParaType.valueOf(v)
    @TypeConverter fun fromTxType(v: TxType): String = v.name
    @TypeConverter fun toTxType(v: String): TxType = TxType.valueOf(v)
}

@Database(
    entities = [
        Task::class, Project::class, EntityLink::class,
        Habit::class, HabitLog::class,
        Note::class, Goal::class,
        MoneyTransaction::class, JournalEntry::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun projectDao(): ProjectDao
    abstract fun entityLinkDao(): EntityLinkDao
    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun noteDao(): NoteDao
    abstract fun goalDao(): GoalDao
    abstract fun transactionDao(): TransactionDao
    abstract fun journalDao(): JournalDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ethioflow.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}
