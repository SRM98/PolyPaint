package com.example.polypaintapp

class Account {

    constructor(firstName: String, lastName: String, username: String, password: String, imageData: ByteArray?, avatarUrl: String, firstTimeUser: Boolean,
                joinedRooms: Array<String>, stats: Stats) {

        this.firstName = firstName
        this.lastName = lastName
        this.username = username
        this.password = password
        this.imageData = imageData
        this.avatarUrl = avatarUrl
        this.firstTimeUser = firstTimeUser
        this.joinedRooms = joinedRooms
        this.stats = stats
    }

    var firstName: String
    var lastName: String
    var username: String
    var password: String
    var imageData: ByteArray?
    var avatarUrl: String
    var firstTimeUser: Boolean
    var joinedRooms: Array<String>
    var stats: Stats
}