package com.example.seamstress.domain

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.seamstress.domain.client.Clients
import com.example.seamstress.domain.client.ClientsDao

@Database(entities = [Clients::class], version = 1, exportSchema = false)
abstract class SeamstressDataBase : RoomDatabase() {
    abstract fun clientsDao(): ClientsDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: SeamstressDataBase? = null

        fun getDatabase(context: Context): SeamstressDataBase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SeamstressDataBase::class.java,
                    "seamstress_db"
                )
                    .createFromAsset("pre_seamstress")
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}