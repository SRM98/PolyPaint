package com.example.polypaintapp


class Message {

    constructor(sender: String, text: String, date: Long, room: String, avatarUrl: String) {
        this.sender = sender
        this.text = text
        this.date = date
        this.room = room
        this.avatarUrl = avatarUrl
    }

    val sender: String
    val text: String
    val date: Long
    val room: String
    val avatarUrl: String

}

class Room {
    constructor(username : String, room : String) {
        this.username = username
        this.room = room
    }

    val username : String
    val room : String

}