import Account from '../models/account.model';
import { initializeMap } from "../Image/imageManager" 
import { Stats } from 'fs';

var MongoClient = require('mongodb').MongoClient;

export class PlayerWinRate {
    username: string;
    winrate: number;
}

export default class AccountDB {
    private db: any;
    public constructor() {
        var url = "mongodb+srv://Admin:TLQ1lToFUGUkGtLQ@projet3-fb7mi.mongodb.net/accounts?retryWrites=true&w=majority";
        MongoClient.connect(url, (err, db) => {
            if (err) throw err;
            this.db = db;
            initializeMap(MongoClient);
        });
    }
    
    public addAccount(account: Account) {
        account.firstTimeUserFatClient = true;
        account.firstTimeUserThinClient = true;
        account.joinedRooms = ["lobby"];
        account.stats = {
            nbGames: 0,
            nbGamesCoop: 0,
            nbGamesSolo: 0,
            nbVictories: 0,
            victoryPercentage: 0,
            averageMatchesTime: 0,
            totalMatchesTime: 0,
        }
        console.log("addAccount: " +  account.username);
        this.db.db("accounts").collection("accounts").insertOne(account, function(err, res) {
            if (err) throw err;
        });
    }

    public async getAllAvatars(): Promise<Map<string, string>> {

        return this.db.db("accounts").collection("accounts").find().toArray().then((res) => {
            var avatars = new Map();
            res.forEach(element => {
                avatars.set(element.username, element.imageData);
            });
            return avatars;
        });             
    }

    public async getAccountByUsername(username: String): Promise<Account> {
        console.log("getaccount: " +  username);
        try {
            return this.db.db("accounts").collection("accounts").findOne({username: username}, {fields: ['firstName', 'lastName', 'username',
                                                                                                         'password', 'firstTimeUserFatClient', 'firstTimeUserThinClient',
                                                                                                          'joinedRooms', 'stats'
                                                                                                       ]});              
        } catch {
            throw "Please try to reconnect later a problem happened";
        }
    }

    public addRoomToUser(username: String, room: String): void {
        console.log("addRoomToUser: " + room + " to " + username)
        this.db.db("accounts").collection("accounts").updateOne({username: username}, {$addToSet: {joinedRooms: room}});              
    }

    public removeRoomToUser(username: String, room: String): void {
        console.log("removeRoomToUser: " + room + " to " + username)
        this.db.db("accounts").collection("accounts").updateOne({username: username}, {$pull: {joinedRooms: room}});              
    }

    public async editAccountFirstName(username: String, newFirstName: String): Promise<void> {
        console.log(username + " new first name is " + newFirstName);

        return this.db.db("accounts").collection("accounts").updateOne({username: username},
                                                                       {$set : {firstName: newFirstName}});              
    }

    public async editAccountLastName(username: String, newLastName: String): Promise<void> {
        console.log(username + " new last name is " + newLastName);

        return this.db.db("accounts").collection("accounts").updateOne({username: username},
                                                                       {$set : {lastName: newLastName}});              
    }

    public async editAccountUsername(oldUsername: String, newUsername: String): Promise<void> {
        console.log(oldUsername + " new username is " + newUsername);

        return this.db.db("accounts").collection("accounts").updateOne({username: oldUsername},
                                                                       {$set : {username: newUsername}});              
    }

    public async editAccountPassword(username: String, newPassword: String): Promise<void> {
        console.log(username + " new password is " + newPassword);

        return this.db.db("accounts").collection("accounts").updateOne({username: username},
                                                                       {$set : {password: newPassword}});              
    }

    public async editAccountAvatar(username: String, newAvatar: any[]): Promise<void> {
        return this.db.db("accounts").collection("accounts").updateOne({username: username},
                                                                       {$set : {imageData: newAvatar}});              
    }

    public async uncheckFirstTimeUserFatClient(username: string): Promise<void> {
        return this.db.db("accounts").collection("accounts").updateOne({username: username},
                                                                       {$set : {firstTimeUserFatClient: false}});              
    }

    public async uncheckFirstTimeUserThinClient(username: string): Promise<void> {
        return this.db.db("accounts").collection("accounts").updateOne({username: username},
                                                                       {$set : {firstTimeUserThinClient: false}});              
    }

    public async getStats(username: String): Promise<any> {
        return this.db.db("accounts").collection("accounts").findOne({username: username}, {fields: ['stats']}); 
    }

    public async updateNbGames(username: String, newValue: number): Promise<void> {
        return this.db.db("accounts").collection("accounts").update({username: username},
                                                                       {'$set' : {"stats.nbGames" : newValue}});              
    }

    public async updateNbGamesCoop(username: String, newValue: number): Promise<void> {
        return this.db.db("accounts").collection("accounts").update({username: username},
                                                                       {'$set' : {"stats.nbGamesCoop" : newValue}});              
    }

    public async updateNbGamesSolo(username: String, newValue: number): Promise<void> {
        return this.db.db("accounts").collection("accounts").update({username: username},
                                                                       {'$set' : {"stats.nbGamesSolo" : newValue}});              
    }

    public async updateNbVictories(username: String, newValue: number): Promise<void> {
        return this.db.db("accounts").collection("accounts").update({username: username},
                                                                       {'$set' : {"stats.nbVictories" : newValue}});              
    }

    public async updateWinRate(username: String, newValue: number): Promise<void> {
        return this.db.db("accounts").collection("accounts").update({username: username},
                                                                       {'$set' : {"stats.victoryPercentage" : newValue}});              
    }

    public async updateTotalMatchesTime(username: String, newValue: number): Promise<void> {
        return this.db.db("accounts").collection("accounts").update({username: username},
                                                                       {'$set' : {"stats.totalMatchesTime" : newValue}});              
    }

    public async updateAverageMatchesTime(username: String, newValue: number): Promise<void> {
        return this.db.db("accounts").collection("accounts").update({username: username},
                                                                       {'$set' : {"stats.averageMatchesTime" : newValue}});              
    }

    public async findGoodPlayer(players: string[]): Promise<PlayerWinRate> {
        return this.db.db("accounts").collection("accounts")
                    .findOne({$and: [ {username : {$in: players}}, 
                        {"stats.victoryPercentage": {$gte: 70} }]})
                    .then((sortedPlayers) => {

                        if (sortedPlayers) 
                            return {username: sortedPlayers.username, winrate: sortedPlayers.stats.victoryPercentage};
                        return {username: "", winrate: 0};
                    });
    }

    // TODO: returns a player from players who has a bad winrate
    public async findBadPlayer(players: string[]): Promise<PlayerWinRate> {
        return this.db.db("accounts").collection("accounts")
                    .findOne({$and: [ {username : {$in: players}}, 
                        {"stats.victoryPercentage": {$lte: 30} }]})
                    .then((sortedPlayers) => {
                        
                        if (sortedPlayers) 
                            return {username: sortedPlayers.username, winrate: sortedPlayers.stats.victoryPercentage};
                        return {username: "", winrate: 0};
                    });
    }

}

export var accountDB: AccountDB = new AccountDB();