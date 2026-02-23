package com.mobilecomputing.db

import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        parentColumn = "uid",
        entityColumn = "foodId"
    )
    val comments: List<FoodComment>,
)


@Dao
interface FoodDao {
    @Query("SELECT * FROM food")
    suspend fun getAll(): List<Food>

    @Insert
    suspend fun insertAll(vararg foods: Food)

    @Insert
    suspend fun insert(food: Food)

    @Query("SELECT COUNT(*) FROM food")
    suspend fun getFoodCount(): Int

    @Query("SELECT * FROM food WHERE uid < :currentUid ORDER BY uid DESC LIMIT 1")
    suspend fun getPreviousFood(currentUid: Int): Food?

    @Query("SELECT * FROM food WHERE uid > :currentUid ORDER BY uid ASC LIMIT 1")
    suspend fun getNextFood(currentUid: Int): Food?

    @Query("SELECT * FROM food ORDER BY uid ASC LIMIT 1")
    suspend fun getFirstFood(): Food?

    @Transaction
    @Query("SELECT * FROM food WHERE uid < :currentUid ORDER BY uid DESC LIMIT 1")
    suspend fun getPreviousFoodWithComments(currentUid: Int): FoodWithComments?

    @Transaction
    @Query("SELECT * FROM food WHERE uid > :currentUid ORDER BY uid ASC LIMIT 1")
    suspend fun getNextFoodWithComments(currentUid: Int): FoodWithComments?

    @Transaction
    @Query("SELECT * FROM food ORDER BY uid ASC LIMIT 1")
    suspend fun getFirstFoodWithComments(): FoodWithComments?

}

@Database(entities = [Food::class], version = 1)
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
    suspend fun getFirstFood(): Food? {
        return foodDao.getFirstFood()
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
    suspend fun getFoodCount(): Int {
        return foodDao.getFoodCount()
    }

    @WorkerThread
    suspend fun getFirstFoodWithComments(): FoodWithComments? {
        return foodDao.getFirstFoodWithComments()
    }

    @WorkerThread
    suspend fun getNextFoodWithComments(currentUid: Int): FoodWithComments? {
        return foodDao.getNextFoodWithComments(currentUid)
    }

    @WorkerThread
    suspend fun getPreviousFoodWithComments(currentUid: Int): FoodWithComments? {
        return foodDao.getPreviousFoodWithComments(currentUid)
    }

    @WorkerThread
    suspend fun getFoodCountWithComments(): Int {
        return foodDao.getFoodCount()
    }
}

class FoodViewModel(private val repository: FoodRepository) : ViewModel() {

    private val _currentFood = MutableStateFlow<FoodWithComments?>(null)
    val currentFood: StateFlow<FoodWithComments?> = _currentFood.asStateFlow()

    private val _foodCount = MutableStateFlow<Int>(0)
    val foodCount: StateFlow<Int> = _foodCount.asStateFlow()

    private val _currentFoodIndex = MutableStateFlow<Int>(0)
    val currentFoodIndex = _currentFoodIndex.asStateFlow()

    init {
        // Load the first food when ViewModel starts
        viewModelScope.launch {
            var firstFood = repository.getFirstFoodWithComments()
            if (firstFood == null)
                databaseReady.await()
            firstFood = repository.getFirstFoodWithComments()
            _currentFood.value = firstFood
            _currentFoodIndex.value = 0
            _foodCount.value = repository.getFoodCount()
        }
    }

    fun insert(food: Food) = viewModelScope.launch {
        repository.insert(food)
        _foodCount.value += 1
    }

    fun loadNextFood() {
        viewModelScope.launch {
            val uid = _currentFood.value?.food?.uid ?: throw NullPointerException("currentfood is null")
            val next = repository.getNextFoodWithComments(uid)
            if (next == null) {
                Log.w("WARN", "Trying to go to next food despite it being null")
                return@launch
            }
            _currentFood.value = next
            _currentFoodIndex.value += 1
        }
    }

    fun loadPreviousFood() {
        viewModelScope.launch {
            val uid = _currentFood.value?.food?.uid ?: throw NullPointerException("currentfood is null")
            val prev = repository.getPreviousFoodWithComments(uid)
            if (prev == null) {
                Log.w("WARN", "Trying to go to previous food despite it being null")
                return@launch
            }
            _currentFood.value = prev
            _currentFoodIndex.value -= 1
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

