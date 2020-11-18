package com.example.polypaintapp

class Match {

    constructor(name: String, nbRounds: Int, type: Int, creator: String, playerCount: Int = 0) {
        this.name = name
        this.nbRounds = nbRounds
        this.type = type
        this.creator = creator
        this.playerCount = playerCount

    }

    val name: String
    val nbRounds: Int
    val type: Int
    val creator: String
    val playerCount: Int

}

class GameRequest {

    constructor(type: Int) {

        this.type = type

    }

    val type: Int


}

class MatchRequest {

    constructor(username: String, id : String) {

        this.username = username
        this.id = id

    }

    val username: String
    val id: String


}


class MatchUserDetails {
    constructor(username : String, ready : Boolean) {
        this.username = username
        this.ready = ready
    }

    var username: String
    var ready: Boolean
}