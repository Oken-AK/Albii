package com.oken.albii.data.album

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAtMillis: Long,
)

/** アルバムに入れた写真・動画の参照。実体は端末の MediaStore 上にあり、ここでは URI だけを持つ。 */
@Entity(tableName = "album_items", primaryKeys = ["albumId", "uri"])
data class AlbumItemEntity(
    val albumId: Long,
    val uri: String,
    val isVideo: Boolean,
    val addedAtMillis: Long,
)

@Dao
interface AlbumDao {
    @Query("SELECT * FROM albums ORDER BY createdAtMillis DESC")
    fun observeAlbums(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM album_items WHERE albumId = :albumId ORDER BY addedAtMillis DESC")
    fun observeItems(albumId: Long): Flow<List<AlbumItemEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAlbum(album: AlbumEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertItems(items: List<AlbumItemEntity>)

    @Delete
    suspend fun deleteItem(item: AlbumItemEntity)

    @Query("DELETE FROM album_items WHERE albumId = :albumId")
    suspend fun deleteItemsOf(albumId: Long)

    @Query("DELETE FROM albums WHERE id = :albumId")
    suspend fun deleteAlbum(albumId: Long)
}

@Database(entities = [AlbumEntity::class, AlbumItemEntity::class], version = 1, exportSchema = false)
abstract class AlbumDatabase : RoomDatabase() {
    abstract fun albumDao(): AlbumDao

    companion object {
        fun create(context: Context): AlbumDatabase =
            Room.databaseBuilder(context, AlbumDatabase::class.java, "albums.db").build()
    }
}
