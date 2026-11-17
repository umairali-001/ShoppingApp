package com.sprizen.uashoppingcenter

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import com.sprizen.uashoppingcenter.DATA_CLASS.CartSQ
import com.sprizen.uashoppingcenter.DATA_CLASS.PHOTO
import com.sprizen.uashoppingcenter.DATA_CLASS.SEARCH

class DataBase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "IMAGES.DB"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE IF NOT EXISTS PHOTO (PHOTO_ID INTEGER PRIMARY KEY AUTOINCREMENT, PHOTO_LINK TEXT, PHOTO_NAME TEXT, CATEGORY TEXT)")
        db?.execSQL("CREATE TABLE IF NOT EXISTS PRODUCT (PRODUCT_ID INTEGER PRIMARY KEY AUTOINCREMENT, PRODUCT_FB_ID TEXT, IN_STOKE TEXT)")
        db?.execSQL("CREATE TABLE IF NOT EXISTS SEARCHES (SEARCH_ID INTEGER PRIMARY KEY AUTOINCREMENT, SEARCH TEXT, IS_DELETED TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS PHOTO")
        db?.execSQL("DROP TABLE IF EXISTS PRODUCT")
        db?.execSQL("DROP TABLE IF EXISTS SEARCHES")
        onCreate(db)
    }

    //=====================================================================================
    //              FOR IMAGE LOADING FOR CATEGORY SECTION
    //======================================================================================

    fun insert_information(photoLink: String, photoName: String, category: String): String {
        val contentValue = contentValuesOf().apply {
            put("PHOTO_LINK", photoLink)
            put("PHOTO_NAME", photoName)
            put("CATEGORY", category)
        }
        val result = writableDatabase.insert("PHOTO", null, contentValue)
        return if (result == -1L) "Error" else "Success"
    }

    fun getData(category: String): MutableList<PHOTO> {
        val photoList = mutableListOf<PHOTO>()

        writableDatabase.rawQuery(
            "SELECT * FROM PHOTO WHERE CATEGORY = ?",
            arrayOf(category)
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val photoId = cursor.getInt(0)
                val photoLink = cursor.getString(1)
                val photoName = cursor.getString(2)
                val photoCategory = cursor.getString(3)

                photoList.add(PHOTO(photoId.toString(), photoLink, photoName, photoCategory))
            }
        }
        return photoList
    }

    //=====================================================================================
    //              FOR CARTED PRODUCTS IN CART SECTION
    //======================================================================================

    fun insertCartProduct(productId: String): String {
        val contentValue = contentValuesOf().apply {
            put("PRODUCT_FB_ID", productId)
            put("IN_STOKE", "TRUE")
        }

        val result = writableDatabase.insert("PRODUCT", null, contentValue)
        return if (result == -1L) "Error" else "Success"
    }

    fun getCatProduct(): MutableList<CartSQ> {
        val productList = mutableListOf<CartSQ>()

        writableDatabase.rawQuery(
            "SELECT * FROM PRODUCT WHERE IN_STOKE = 'TRUE'", null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val autId = cursor.getInt(0)
                val productId = cursor.getString(1)
                val inStoke = cursor.getString(2)

                productList.add(CartSQ(autId.toString(), productId, inStoke))
            }
        }
        return productList
    }

    fun getLastAddedProduct(): CartSQ? {
        writableDatabase.rawQuery("SELECT * FROM PRODUCT WHERE IN_STOKE = 'TRUE'", null).use { cursor ->
            if (cursor.moveToLast()) {
                val autId = cursor.getInt(0)
                val productId = cursor.getString(1)
                val inStoke = cursor.getString(2)
                return CartSQ(autId.toString(), productId, inStoke)
            }
        }
        return null
    }

    fun deleteProduct(id: String): String {
        val contentValues = contentValuesOf().apply {
            put("IN_STOKE", "FALSE")
        }

        val noOfUpdatedRecord = writableDatabase.update(
            "PRODUCT", contentValues, "PRODUCT_FB_ID=?", arrayOf(id)
        )
        return if (noOfUpdatedRecord > 0) "Deleted Success" else "Error"
    }

    //=====================================================================================
    //              FOR RECENT SEARCHES FOR SEARCH SECTION
    //======================================================================================

    fun insertResentSearch(search: String): String {
        val contentValue = contentValuesOf().apply {
            put("SEARCH", search)
            put("IS_DELETED", "FALSE")
        }

        val result = writableDatabase.insert("SEARCHES", null, contentValue)
        return if (result == -1L) "Error" else "Success"
    }

    fun getRecentSearches(): MutableList<SEARCH> {
        val searchList = mutableListOf<SEARCH>()

        writableDatabase.rawQuery(
            "SELECT * FROM SEARCHES WHERE IS_DELETED = 'FALSE'",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val searchId = cursor.getInt(0)
                val search = cursor.getString(1)

                searchList.add(SEARCH(searchId.toString(), search))
            }
        }

        return searchList.reversed().toMutableList()
    }

    // Single item soft delete
    fun deleteSearches(id: Int): String {
        val contentValues = contentValuesOf().apply {
            put("IS_DELETED", "TRUE")
        }
        val noOfUpdatedRecord = writableDatabase.update(
            "SEARCHES", contentValues, "SEARCH_ID=?", arrayOf(id.toString())
        )
        return if (noOfUpdatedRecord > 0) "Deleted Success" else "Error"
    }

    // Clear All soft delete (Saari active searches ko aik sath TRUE kar dega)
    fun deleteAllSearches(): String {
        val contentValues = contentValuesOf().apply {
            put("IS_DELETED", "TRUE")
        }
        val noOfUpdatedRecord = writableDatabase.update(
            "SEARCHES", contentValues, "IS_DELETED = ?", arrayOf("FALSE")
        )
        return if (noOfUpdatedRecord > 0) "Deleted Success" else "Error"
    }
}