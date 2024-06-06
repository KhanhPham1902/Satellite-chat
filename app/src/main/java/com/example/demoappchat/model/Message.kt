package com.example.demoappchat.model

import android.os.Parcel
import android.os.Parcelable

data class Message(
    val id: Int,
    val data: String,
    val user_id: Int,
    val device: String,
    val time: String,
    val direc: Int,
    val state: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(data)
        parcel.writeInt(user_id)
        parcel.writeString(device)
        parcel.writeString(time)
        parcel.writeInt(direc)
        parcel.writeString(state)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Message> {
        override fun createFromParcel(parcel: Parcel): Message {
            return Message(parcel)
        }

        override fun newArray(size: Int): Array<Message?> {
            return arrayOfNulls(size)
        }
    }
}
