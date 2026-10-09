package et.ethioflow

import android.app.Application
import et.ethioflow.data.db.AppDatabase
import et.ethioflow.domain.Repository

class EthioFlowApp : Application() {
    lateinit var repository: Repository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.get(this)
        repository = Repository(
            taskDao = db.taskDao(),
            projectDao = db.projectDao(),
            linkDao = db.entityLinkDao(),
            habitDao = db.habitDao(),
            habitLogDao = db.habitLogDao(),
            noteDao = db.noteDao(),
            goalDao = db.goalDao(),
            transactionDao = db.transactionDao(),
            journalDao = db.journalDao()
        )
    }
}
