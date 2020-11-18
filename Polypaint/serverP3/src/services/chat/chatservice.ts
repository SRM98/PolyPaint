import "reflect-metadata";
import Message from "../../models/chat.model";
import RoomJoin from "../../models/roomJoin.model";
import { chatDB } from "../../database/chatDB";
import { accountDB } from "../../database/accountDB";
import { inMatchService } from "../../services/match/inmatch";
import { ChatFilter } from './chatFilter';
import { avatarsDictionnary } from "../../Image/imageManager"
import { mmService } from "../match/matchmaking";

export class ChatService {
    private rooms;
    private chatFilter: ChatFilter;

    public constructor(){
        this.rooms = new Set();
        chatDB.onConnect = () => this.getAllRooms().then((value: String[]) => {
            value.forEach((room) => this.rooms.add(room));
        })
        this.chatFilter = new ChatFilter();
    }
    public incommingMessage(io: SocketIO.Server, socket: SocketIO.Socket, message: Message) {
        message.text = message.text.trim();
        if (message.text != ""){
            message.text = this.chatFilter.filterMessage(socket, message);
            message.date = Date.now();
            message.senderAvatarUrl = avatarsDictionnary.get(message.sender);
            if(!inMatchService.matches.has(message.room) &&
               !mmService.matches.has(message.room)) chatDB.addMessage(message);
            io.sockets.in(message.room).emit("message", message);
        }

        if(message.text === "hint!" && inMatchService.matches.has(message.room)) {
            inMatchService.matches.get(message.room).hint(io, socket, message.sender);
        }
    }
    public getMessages(room: String): Promise<Message[]> {
        return chatDB.getMessagesRoom(room);
    }

    public async getOwnRooms(username: String): Promise<String[]> {
        try{
            return await accountDB.getAccountByUsername(username).then((user) => {
                return user.joinedRooms;
            });
        }
        catch(e){
            return e;
        }
    }

    public getAllRooms(): Promise<String[]> {
        return chatDB.getAllRooms();
    }
    public createRoom(io: any, socket: any, roomJoin: RoomJoin) {
        roomJoin.room = roomJoin.room.trim();
        if(roomJoin.room != ""){
            console.log("create room");
            accountDB.addRoomToUser(roomJoin.username, roomJoin.room);
            socket.join(roomJoin.room);
            if(!this.rooms.has(roomJoin.room)){
                this.rooms.add(roomJoin.room);
                var message = <Message> {
                    sender: "Lascaux",
                    text:   "Welcome to the new room " + roomJoin.room + "!",
                    date:   0,
                    room:   roomJoin.room,
                }
                socket.emit("validRoom", roomJoin.room);
                this.incommingMessage(io, socket, message);
            }
        }
        else{
            socket.emit("invalidRoom");
        }
    }

    public leaveRoom(socket: any, roomJoin: RoomJoin){
        if(roomJoin.room != "lobby"){
            accountDB.removeRoomToUser(roomJoin.username, roomJoin.room);
            socket.emit("leaveRoom", roomJoin.room);
            socket.leave(roomJoin.room);    
        }
    }
}

export var chatService: ChatService = new ChatService();