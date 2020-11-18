package com.example.polypaintapp

class Stats {

    constructor(nbGames: Number, nbGamesCoop: Number, nbGamesSolo: Number, nbVictories: Number, victoryPercentage: Number, averageMatchesTime: Number,
                totalMatchesTime: Number) {
        this.nbGames = nbGames
        this.nbGamesCoop = nbGamesCoop
        this.nbGamesSolo = nbGamesSolo
        this.nbVictories = nbVictories
        this.victoryPercentage = victoryPercentage
        this.averageMatchesTime = averageMatchesTime
        this.totalMatchesTime = totalMatchesTime
    }

    var nbGames: Number
    var nbGamesCoop: Number
    var nbGamesSolo: Number
    var nbVictories: Number
    var victoryPercentage: Number
    var averageMatchesTime: Number
    var totalMatchesTime: Number
}