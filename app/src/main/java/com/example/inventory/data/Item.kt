package com.example.inventory.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.NumberFormat

@Entity(tableName = "item")
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "name")
    val itemName: String,
    @ColumnInfo(name = "price")
    val itemPrice: Double,
    @ColumnInfo(name = "quantity")
    val quantityInStock: Int,
    @ColumnInfo(name = "providerName")
    var providerName: String,
    @ColumnInfo(name = "providerEmail")
    var providerEmail: String,
    @ColumnInfo(name = "providerPhone")
    var providerPhone : String,
    @ColumnInfo(name = "Record")
    var record: Record = Record.MANUAL

) {
    override fun toString() : String {
        return "Название товара - ${itemName}, " +
                "Цена - ${itemPrice}, " +
                "Количество - ${quantityInStock}, " +
                "Поставщик - ${providerName}, " +
                "Email Постовщика - ${providerEmail}, " +
                "Номер телефона поставщика - ${providerPhone}."
    }
}

fun Item.getFormattedPrice(): String =
    NumberFormat.getCurrencyInstance().format(itemPrice)

enum class Record{
    MANUAL, FILE
}