import { initializeMap } from "../Image/imageManager" 

var MongoClient = require('mongodb').MongoClient;



export default class MatchHistoryDB {
    private db: any;
    public constructor() {
        var url = "mongodb+srv://Admin:TLQ1lToFUGUkGtLQ@projet3-fb7mi.mongodb.net/matches?retryWrites=true&w=majority";
        MongoClient.connect(url, (err, db) => {
            this.db = db;
            if (err) throw err;
        });
        
    }
    
    public addMatch(match: any) {
        console.log("add match history: " +  match.name);
        this.db.db("matches").collection("matches").insertOne(match, function(err, res) {
            if (err) throw err;
        });
    }

    // TODO: return a player from players where player1 has played with recently
    public referencePlayers(player1: string, players: string[]): Promise<string> {
        let otherPlayers = players.filter( player => player !== player1);
        return this.db.db("matches").collection("matches")
            .findOne({$or:[
                {$and: [
                    {teamA: player1}, 
                    {teamB: {$in: otherPlayers}}]
                },
                {$and: [
                    {teamA: {$in: otherPlayers}}, 
                    {teamB: player1}]
                },
                {$and: [
                    {players: {$in: otherPlayers}}, 
                    {aiplayer: player1}]
                }]})
            .then( (referencedGame) => {
                if (!referencedGame)
                    return "";
                let intersection = this.arrayIntersection(referencedGame.teamA, players);
                
                if (intersection.length > 0)
                    return intersection[0];

                intersection = this.arrayIntersection(referencedGame.teamB, players);
                if (intersection.length > 0)
                    return intersection[0];

                return "";
            });
                                                                    
    }

    public arrayIntersection(array1, array2) {    
        if (!array1 || !array2)
            return [];
        return array1.filter(value => -1 !== array2.indexOf(value))
    }

    // TODO: returns a player from players who has a good winrate

}

export var matchHistoryDB: MatchHistoryDB = new MatchHistoryDB();