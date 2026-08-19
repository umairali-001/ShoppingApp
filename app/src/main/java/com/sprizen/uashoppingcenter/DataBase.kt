package com.sprizen.uashoppingcenter

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import com.sprizen.uashoppingcenter.DATA_CLASS.CartSQ
import com.sprizen.uashoppingcenter.DATA_CLASS.PHOTO

class DataBase(context: Context) : SQLiteOpenHelper(context, "IMAGES.DB", null, 1) {

    var context: Context

    init {
        this.context = context

        writableDatabase.execSQL("CREATE TABLE IF NOT EXISTS PHOTO(PHOTO_ID INTEGER PRIMARY KEY AUTOINCREMENT, PHOTO_LINK TEXT, PHOTO_NAME TEXT, CATEGORY TEXT )")
        writableDatabase.execSQL(
            "CREATE TABLE IF NOT EXISTS PRODUCT(PRODUCT_ID INTEGER PRIMARY " +
                    "KEY AUTOINCREMENT, PRODUCT_FB_ID TEXT, IN_STOKE TEXT)"
        )
    }

    fun insert_information(photoLink: String, photoName: String, category: String): String {
        var contentValue = contentValuesOf()
        contentValue.put("PHOTO_LINK", photoLink)
        contentValue.put("PHOTO_NAME", photoName)
        contentValue.put("CATEGORY", category)
        var result = writableDatabase.insert("PHOTO", null, contentValue)

        if (result == -1L) {
            return "Error"
        } else {
            return "Succes"
        }
    }

    fun getData(category: String): MutableList<PHOTO> {
        var photoList = mutableListOf<PHOTO>()

        val cursor = writableDatabase.rawQuery(
            "SELECT * FROM PHOTO WHERE CATEGORY = ?",
            arrayOf(category)
        )

        while (cursor.moveToNext()) {
            var photoId = cursor.getInt(0)
            var photoLink = cursor.getString(1)
            var photoName = cursor.getString(2)
            var photoCategory = cursor.getString(3)

            var sigle_Info = PHOTO(photoId.toString(), photoLink, photoName, photoCategory)
            photoList.add(sigle_Info)
        }
        return photoList

    }

    fun insertCartProduct(productId: String): String {

        var contentValue = contentValuesOf()

        contentValue.put("PRODUCT_FB_ID", productId)
        contentValue.put("IN_STOKE", "TURE")

        var result = writableDatabase.insert("PRODUCT", null, contentValue)

        if (result == -1L) {
            return "Error"
        } else {
            return "Succes"
        }
    }

    fun getCatProduct(): MutableList<CartSQ> {
        var productList = mutableListOf<CartSQ>()

        val cursor = writableDatabase.rawQuery(
            "SELECT * FROM PRODUCT WHERE IN_STOKE = 'TURE'", null
        )

        while (cursor.moveToNext()) {

            var autId = cursor.getInt(0)
            var productId = cursor.getString(1)
            var inStoke = cursor.getString(2)


            var sigle_Info = CartSQ(autId.toString(), productId.toString(), inStoke.toString())
            productList.add(sigle_Info)
        }
        return productList

    }

    fun getLastAddedProduct(): CartSQ {
        val cursor = writableDatabase.rawQuery("SELECT * FROM PRODUCT WHERE IN_STOKE = 'TURE'",null)
        cursor.moveToPosition(cursor.count - 1)
        var autId = cursor.getInt(0)
        var productId = cursor.getString(1)
        var inStoke = cursor.getString(2)

        var lastCartProduct = CartSQ(autId.toString(), productId.toString(), inStoke.toString())

        return lastCartProduct

    }

    fun deleteProduct(id: String): String {
        var contentValues = contentValuesOf()
        contentValues.put("IN_STOKE", "FALSE") // or "1", depending on how you want to store it

        val noOfUpdatedRecord = writableDatabase.update(
            "PRODUCT", contentValues, "PRODUCT_FB_ID=?", arrayOf(id.toString())
        )
        return if (noOfUpdatedRecord > 0) "Deleted Success" else "Error"
    }


    override fun onCreate(p0: SQLiteDatabase?) {


    }

    override fun onUpgrade(
        p0: SQLiteDatabase?,
        p1: Int,
        p2: Int,
    ) {
        TODO("Not yet implemented")
    }
}
