package com.mobilecomputing.db

import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.AutoMigration
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mobilecomputing.R
import com.mobilecomputing.db.AppDatabase.Companion.databaseReady
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.io.File
import java.net.URI

@Entity
@Serializable
data class Food(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "imageUrl") val imageUrl: String? = null
)

@Entity
@Serializable
data class FoodComment(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "foodId") val foodId: Int = 0,
    @ColumnInfo(name = "content") val content: String,
)

data class FoodWithComments(
    @Embedded val food: Food,
    @Relation(
        parentColumn = "uid", entityColumn = "foodId"
    ) val comments: List<FoodComment>,
)


@Dao
interface FoodDao {
    @Query("SELECT * FROM food")
    suspend fun getAll(): List<Food>

    @Insert
    suspend fun insertAll(vararg foods: Food)

    @Insert
    suspend fun insert(food: Food)

    @Insert
    suspend fun insert(comment: FoodComment)

    @Query("SELECT COUNT(*) FROM food")
    fun getFoodCount(): Flow<Int>

    @Query("SELECT * FROM food WHERE uid < :currentUid ORDER BY uid DESC LIMIT 1")
    suspend fun getPreviousFood(currentUid: Int): Food?

    @Query("SELECT * FROM food WHERE uid > :currentUid ORDER BY uid ASC LIMIT 1")
    suspend fun getNextFood(currentUid: Int): Food?

    @Query("SELECT uid FROM food WHERE uid < :currentUid ORDER BY uid DESC LIMIT 1")
    suspend fun getPrevFoodId(currentUid: Int): Int?

    @Query("SELECT uid FROM food WHERE uid > :currentUid ORDER BY uid ASC LIMIT 1")
    suspend fun getNextFoodId(currentUid: Int): Int?

    @Query("SELECT * FROM food ORDER BY uid ASC LIMIT 1")
    suspend fun getFirstFood(): Food?

    @Query("SELECT uid FROM food ORDER BY uid ASC LIMIT 1")
    suspend fun getFirstFoodId(): Int?

    @Transaction
    @Query("SELECT * FROM food WHERE uid == :currentUid ORDER BY uid DESC LIMIT 1")
    fun getFoodWithComments(currentUid: Int?): Flow<FoodWithComments?>

    @Transaction
    @Query("SELECT * FROM food WHERE uid < :currentUid ORDER BY uid DESC LIMIT 1")
    fun getPreviousFoodWithComments(currentUid: Int?): Flow<FoodWithComments?>

    @Transaction
    @Query("SELECT * FROM food WHERE uid > :currentUid ORDER BY uid ASC LIMIT 1")
    fun getNextFoodWithComments(currentUid: Int?): Flow<FoodWithComments?>

    @Transaction
    @Query("SELECT * FROM food ORDER BY uid ASC LIMIT 1")
    fun getFirstFoodWithComments(): Flow<FoodWithComments?>


}

@Database(
    entities = [Food::class, FoodComment::class],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao

    // https://developer.android.com/codelabs/android-room-with-a-view-kotlin#7
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        val databaseReady = CompletableDeferred<Unit>()
        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {

            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java, "food_database"
                ).addCallback(AppDatabaseCallback(context, scope)).build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val context: Context, private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.foodDao())
                    databaseReady.complete(Unit)
                }
            }
        }

        suspend fun populateDatabase(foodDao: FoodDao) {
            // Delete all content here.
            //foodDao.deleteAll()

            val loremIpsum = context.getString(R.string.lorem_ipsum)

            foodDao.insertAll(
                Food(name = "A cheesecake", description = loremIpsum),
            )
            Log.i("STATE", "database initialized")
        }
    }
}

class FoodRepository(private val foodDao: FoodDao) {
    @WorkerThread
    suspend fun insert(food: Food) {
        foodDao.insert(food)
    }

    @WorkerThread
    suspend fun insert(comment: FoodComment) {
        foodDao.insert(comment);
    }

    @WorkerThread
    suspend fun getFirstFood(): Food? {
        return foodDao.getFirstFood()
    }

    @WorkerThread
    fun getFoodWithComments(currentUid: Int?): Flow<FoodWithComments?> {
        return foodDao.getFoodWithComments(currentUid);
    }

    @WorkerThread
    suspend fun getNextFood(currentUid: Int): Food? {
        return foodDao.getNextFood(currentUid)
    }

    @WorkerThread
    suspend fun getPreviousFood(currentUid: Int): Food? {
        return foodDao.getPreviousFood(currentUid)
    }

    @WorkerThread
    fun getFoodCount(): Flow<Int> {
        return foodDao.getFoodCount()
    }

    @WorkerThread
    suspend fun getFirstFoodWithComments(): Flow<FoodWithComments?> {
        return foodDao.getFirstFoodWithComments()
    }

    @WorkerThread
    fun getNextFoodWithComments(currentUid: Int?): Flow<FoodWithComments?> {
        return foodDao.getNextFoodWithComments(currentUid)
    }

    @WorkerThread
    fun getPreviousFoodWithComments(currentUid: Int?): Flow<FoodWithComments?> {
        return foodDao.getPreviousFoodWithComments(currentUid)
    }

    @WorkerThread
    suspend fun getFirstFoodId(): Int? {
        return foodDao.getFirstFoodId()
    }

    @WorkerThread
    suspend fun getNextFoodId(currentUid: Int?): Int? {
        if (currentUid == null) return null;
        return foodDao.getNextFoodId(currentUid)
    }

    @WorkerThread
    suspend fun getPrevFoodId(currentUid: Int?): Int? {
        if (currentUid == null) return null;
        return foodDao.getPrevFoodId(currentUid)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class FoodViewModel(private val repository: FoodRepository) : ViewModel() {
    private val currentFoodId = MutableStateFlow<Int?>(null);

    val currentFood: StateFlow<FoodWithComments?> = currentFoodId.filterNotNull().flatMapLatest { id ->
        repository.getFoodWithComments(id)
    }.stateIn(
    viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    private val prevFoodId = MutableStateFlow<Int?>(null);
    // hasPrevFood generated with AI
    val hasPrevFood: StateFlow<Boolean> =
        prevFoodId
            .map { it != null }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                false
            )
    private val nextFoodId = MutableStateFlow<Int?>(null);
    // hasNextFood generated with AI
    val hasNextFood: StateFlow<Boolean> =
        nextFoodId
            .map { it != null }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                false
            )

    val foodCount: StateFlow<Int> = repository.getFoodCount().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 0
    );

    private val _currentFoodIndex = MutableStateFlow<Int>(0)
    val currentFoodIndex = _currentFoodIndex.asStateFlow()


    init {
        // Load the first food when ViewModel starts
        viewModelScope.launch {
            var firstFoodId = repository.getFirstFoodId();
            while (firstFoodId == null) {
                Log.i("DB", "waiting for db to get ready")
                delay(500) // wait 500ms before checking again
                firstFoodId = repository.getFirstFoodId()
            }
            Log.i("DB", "first food id $firstFoodId")
            currentFoodId.value = firstFoodId;
            _currentFoodIndex.value = 0
            updateNextAndPrev();
        }
    }

    private fun updateNextAndPrev() = viewModelScope.launch {
        nextFoodId.value = repository.getNextFoodId(currentFoodId.value)
        prevFoodId.value = repository.getPrevFoodId(currentFoodId.value)
    }

    fun insert(food: Food) = viewModelScope.launch {
        repository.insert(food)
        if (nextFoodId.value == null) {
            nextFoodId.value = repository.getNextFoodId(currentFoodId.value);
        }
    }

    fun insert(comment: FoodComment) = viewModelScope.launch {
        repository.insert(comment)
    }

    fun loadNextFood() {
        viewModelScope.launch {
            if (nextFoodId.value == null) {
                Log.w("WARN", "Trying to go to next food despite it being null")
                return@launch;
            }

            currentFoodId.value = nextFoodId.value;
            updateNextAndPrev();
        }
    }

    fun loadPreviousFood() {
        viewModelScope.launch {
            if (prevFoodId.value == null) {
                Log.w("WARN", "Trying to go to previous food despite it being null")
                return@launch;
            }

            currentFoodId.value = prevFoodId.value;
            updateNextAndPrev();
        }
    }

}

class FoodViewModelFactory(private val repository: FoodRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FoodViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return FoodViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

