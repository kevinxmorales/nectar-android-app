package com.morales.nectar.data.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import java.util.*

data class PlantData(
    val plantId: String? = null,
    var commonName: String? = null,
    var scientificName: String? = null,
    var toxicity: String? = null,
    val userId: String? = null,
    @SerializedName("userProfileImage")
    val userImage: String? = null,
    val username: String? = null,
    var images: List<String>? = null,
    var likes: List<String>? = listOf(),
    var searchTerms: List<String>? = listOf(),
    val createdAt: String? = Calendar.getInstance().time.toString(),
    var imagesToDelete: List<String>? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.createStringArrayList(),
        parcel.createStringArrayList(),
        parcel.createStringArrayList(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(plantId)
        parcel.writeString(commonName)
        parcel.writeString(scientificName)
        parcel.writeString(toxicity)
        parcel.writeString(userId)
        parcel.writeString(userImage)
        parcel.writeString(username)
        parcel.writeStringList(images)
        parcel.writeStringList(likes)
        parcel.writeStringList(searchTerms)
        parcel.writeString(createdAt)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PlantData> {
        override fun createFromParcel(parcel: Parcel): PlantData {
            return PlantData(parcel)
        }

        override fun newArray(size: Int): Array<PlantData?> {
            return arrayOfNulls(size)
        }
    }
}