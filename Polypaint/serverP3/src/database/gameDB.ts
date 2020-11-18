import { Game } from "../models/game.model.js";

var MongoClient = require('mongodb').MongoClient;

export class GameDB {
    private db: any;
    private games: Game[];

    public constructor() {
        var url = "mongodb+srv://Admin:TLQ1lToFUGUkGtLQ@projet3-fb7mi.mongodb.net/games?retryWrites=true&w=majority";
        MongoClient.connect(url, (err, db) => {
            this.db = db;
            if (err) throw err;
            this.getGames();   
        });
        this.games = [];
    }

    public getNbGames(): number {
        return this.games.length;
    }

    public addGame(game: Game) {
        this.games.push(game);
        this.db.db("games").collection("games").insertOne(game, function(err, res) {
            if (err) throw err;
        });
    }

    public async getGames(): Promise<void> {
        this.games = await this.db.db("games").collection("games").find({}).toArray();  
    }

    public getRandomGames(nbRounds: number): Game[] {
        const lgames = this.deepCopy(this.games);
        let randomGames = [];
        if (nbRounds > this.games.length)
            throw new Error("Not enough games to play that much rounds");
        for (let i = 0; i < nbRounds; ++i) {
            const index = Math.floor(Math.random() * lgames.length);
            randomGames.push(lgames[index]);
            lgames[index] = lgames[lgames.length - 1];
            lgames.pop();
        }
        

        return randomGames;
    }

    private deepCopy(games) {
        return JSON.parse(JSON.stringify(games));
    }
}

export let gameDB: GameDB =  new GameDB();