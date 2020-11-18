package com.example.polypaintapp

class ClassicGame {

    constructor(name: String, nbRounds: Int, type: Int, creator: String, playerCount: Int,  teamA : ArrayList<MatchUserDetails>, teamB : ArrayList<MatchUserDetails>, placesLeft: Int) {
        this.name = name
        this.nbRounds = nbRounds
        this.type = type
        this.creator = creator
        this.playerCount = playerCount
        this.placesLeft = placesLeft
        this.teamA = teamA
        this.teamB = teamB



    }

    val name: String
    val nbRounds: Int
    val type: Int
    val creator: String
    val playerCount: Int
    val placesLeft: Int
    var teamA =  ArrayList<MatchUserDetails>()
    var teamB =  ArrayList<MatchUserDetails>()

}

class CoopGame {

    constructor(name: String, nbRounds: Int, type: Int, creator: String, playerCount: Int, players : ArrayList<MatchUserDetails>, aiplayer : String, placesLeft: Int) {
        this.name = name
        this.nbRounds = nbRounds
        this.type = type
        this.creator = creator
        this.playerCount = playerCount
        this.placesLeft = placesLeft
        this.players = players
        this.aiplayer = aiplayer

    }

    val name: String
    val nbRounds: Int
    val type: Int
    val creator: String
    val playerCount: Int
    val placesLeft: Int
    var players =  ArrayList<MatchUserDetails>()
    val aiplayer : String

}

class DuelGame {

    constructor(name: String, nbRounds: Int, type: Int, creator: String, playerCount: Int, player1 : MatchUserDetails, player2 : MatchUserDetails, placesLeft: Int) {
        this.name = name
        this.nbRounds = nbRounds
        this.type = type
        this.creator = creator
        this.playerCount = playerCount
        this.placesLeft = placesLeft
        this.player1 = player1
        this.player2 = player2


    }

    val name: String
    val nbRounds: Int
    val type: Int
    val creator: String
    val playerCount: Int
    val placesLeft: Int
    val player1 : MatchUserDetails
    val player2 : MatchUserDetails




}

class SoloGame {

    constructor(id : String ,name: String, nbRounds: Int, type: Int, creator: String, playerCount: Int, aiplayer : String) {

        this.id = id
        this.name = name
        this.nbRounds = nbRounds
        this.type = type
        this.creator = creator
        this.playerCount = playerCount
        this.aiplayer = aiplayer

    }
    val id: String
    val name: String
    val nbRounds: Int
    val type: Int
    val creator: String
    val playerCount: Int
    val aiplayer : String

}