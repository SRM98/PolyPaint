import Message from '../models/chat.model';
var MongoClient = require('mongodb').MongoClient;
export default class ChatDB {
    private db: any;
    public onConnect: Function;
    public constructor() {
        var url = "mongodb+srv://Admin:TLQ1lToFUGUkGtLQ@projet3-fb7mi.mongodb.net/chat?retryWrites=true&w=majority";
        MongoClient.connect(url, (err, db) => {
            this.db = db;
            if (err) throw err;
            this.onConnect();
        });
    }

    
    public addMessage(message: Message) {
        console.log("addMessage: " + JSON.stringify(message));
        this.db.db("chat").collection(message.room).insertOne(message, function(err, res) {
            if (err) throw err;
        });
    }
    
    public getAllRooms(): Promise<String[]> {
        console.log("getRooms");
        return this.db.db("chat").listCollections().toArray().then((res) => {
            var searchedRooms = [];
            res.forEach(element => {
                searchedRooms.push(element.name);
            });
            return searchedRooms;
        });
    }
    public async getMessagesRoom(room: String): Promise<Message[]> {
        console.log("getMessageRoom: " + room);
        return this.db.db("chat").collection(room).find({}).toArray();        
    }
}
export let chatDB: ChatDB = new ChatDB();