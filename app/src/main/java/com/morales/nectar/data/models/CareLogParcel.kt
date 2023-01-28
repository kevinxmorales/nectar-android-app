package com.morales.nectar.data.models

import android.os.Parcel
import android.os.Parcelable
import java.time.LocalDate

data class CareLogParcel(
    val id: String? = null,
    val plantId: String? = null,
    val notes: String? = null,
    val wasFertilized: Boolean = false,
    val wasWatered: Boolean = false,
    val careDate: String? = LocalDate.now().toString(),
    val createdAt: String? = LocalDate.now().toString(),
    val plantImage: String? = "",
    val plantName: String? = "",

    ) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(plantId)
        parcel.writeString(notes)
        parcel.writeByte(if (wasFertilized) 1 else 0)
        parcel.writeByte(if (wasWatered) 1 else 0)
        parcel.writeString(careDate)
        parcel.writeString(createdAt)
        parcel.writeString(plantImage)
        parcel.writeString(plantName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CareLogParcel> {
        override fun createFromParcel(parcel: Parcel): CareLogParcel {
            return CareLogParcel(parcel)
        }

        override fun newArray(size: Int): Array<CareLogParcel?> {
            return arrayOfNulls(size)
        }
    }

    fun toCareLog(): CareLog {
        return CareLog(
            id = id!!,
            plantId = plantId!!,
            notes = notes,
            wasFertilized = wasFertilized,
            wasWatered = wasWatered,
            careDate = careDate!!,
            createdAt = createdAt!!,
            plantImage = plantImage ?: "",
            plantName = plantName!!
        )
    }
}
