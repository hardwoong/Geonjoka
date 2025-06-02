package com.example.finalpj.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AppDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_NAME = "finalpj.db"
        const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE User (
                id TEXT PRIMARY KEY,
                pw TEXT,
                name TEXT,
                email TEXT,
                birth TEXT,
                isPregnant INTEGER,
                recommendedCaffeine INTEGER
            )
        """)
        db.execSQL("""
            CREATE TABLE CaffeineIntake (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                userId TEXT,
                date TEXT,
                intake INTEGER,
                FOREIGN KEY(userId) REFERENCES User(id)
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS User")
        db.execSQL("DROP TABLE IF EXISTS CaffeineIntake")
        onCreate(db)
    }
}
