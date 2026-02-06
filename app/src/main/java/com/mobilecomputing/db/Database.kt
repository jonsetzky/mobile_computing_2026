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
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mobilecomputing.R
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
}

@Database(entities = [Food::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao

    // https://developer.android.com/codelabs/android-room-with-a-view-kotlin#7
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
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
            Log.i("STATE", "database intialized")
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
}

class FoodViewModel(private val repository: FoodRepository) : ViewModel() {

    private val _currentFood = MutableStateFlow<Food?>(null)
    val currentFood: StateFlow<Food?> = _currentFood.asStateFlow()

    private val _isLast = MutableStateFlow<Boolean>(false)
    val isLast: StateFlow<Boolean> = _isLast.asStateFlow()

    private val _isFirst = MutableStateFlow<Boolean>(false)
    val isFirst: StateFlow<Boolean> = _isFirst.asStateFlow()

    init {
        // Load the first food when ViewModel starts
        viewModelScope.launch {
            _currentFood.value = repository.getFirstFood()
            _isFirst.value = true
            _isLast.value = false
        }
    }

    fun insert(food: Food) = viewModelScope.launch {
        repository.insert(food)
    }

    fun getFirstFood() = viewModelScope.launch {
        repository.getFirstFood()
    }

    fun loadNextFood() {
        viewModelScope.launch {
            val uid = _currentFood.value?.uid ?: throw NullPointerException("currentfood is null")
            val next = repository.getNextFood(uid)
            if (next == null) {
                Log.w("WARN", "Trying to go to next food despite it being null")
                return@launch
            }
            _currentFood.value = next
            _isFirst.value = false
            _isLast.value = (repository.getNextFood(next.uid) == null)
        }
    }

    fun loadPreviousFood() {
        viewModelScope.launch {
            val uid = _currentFood.value?.uid ?: throw NullPointerException("currentfood is null")
            val prev = repository.getPreviousFood(uid)
            if (prev == null) {
                Log.w("WARN", "Trying to go to previous food despite it being null")
                return@launch
            }
            _currentFood.value = prev
            _isLast.value = false
            _isFirst.value = (repository.getPreviousFood(prev.uid) == null)
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

