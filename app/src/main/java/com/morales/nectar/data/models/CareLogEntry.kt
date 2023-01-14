package com.morales.nectar.data.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import java.util.*

data class CareLogEntry(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("plantId")
    val plantId: String? = null,
    @SerializedName("notes")
    val notes: String? = null,
    @SerializedName("wasFertilized")
    val wasFertilized: Boolean? = null,
    @SerializedName("wasWatered")
    val wasWatered: Boolean? = null,
    @SerializedName("date")
    val date: String? = Calendar.getInstance().time.toString(),
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(plantId)
        parcel.writeString(notes)
        parcel.writeValue(wasFertilized)
        parcel.writeValue(wasWatered)
        parcel.writeString(date)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CareLogEntry> {
        override fun createFromParcel(parcel: Parcel): CareLogEntry {
            return CareLogEntry(parcel)
        }

        override fun newArray(size: Int): Array<CareLogEntry?> {
            return arrayOfNulls(size)
        }
    }
}