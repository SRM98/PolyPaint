import "reflect-metadata";
import {
        ConnectedSocket, MessageBody, OnConnect,
        OnDisconnect, OnMessage, SocketController, SocketIO
    } from "socket-controllers";
import { chatService } from "../../services/chat/chatservice";
import Message from "../../models/chat.model";
import RoomJoin from "../../models/roomJoin.model";
import { userSocketMap } from '../../services/userSocketsMap/userSocketsMap';
import { inMatchService } from "../../services/match/inmatch";
import { mmService } from "../../services/match/matchmaking";
import { avatarsDictionnary } from "../../Image/imageManager";

@SocketController()
export class ChatController {
    @OnConnect()
    connection(@ConnectedSocket() socket: SocketIO.Socket) {
        socket.emit("connection", socket.id);
        socket.join("lobby");
    }
    @OnDisconnect()
    disconnect(@SocketIO() io: SocketIO.Server, @ConnectedSocket() socket: SocketIO.Socket) {
        console.log("client disconnected");
        let username: string = userSocketMap.getUsernameBySocket(socket.id);
        inMatchService.leaveInMatch(io, socket, {username: username});
        mmService.leaveMatch(io, socket, {username: username});
        userSocketMap.removeSocketId(socket.id);
        socket.disconnect(true);
    }

    @OnMessage("disconnectUser")
    disconnectUser(@ConnectedSocket() socket: SocketIO.Socket) {
        userSocketMap.removeSocketId(socket.id);
    }

    @OnMessage("send")
    onMessageReceived(@SocketIO() io: SocketIO.Server, @ConnectedSocket() socket: SocketIO.Socket,@MessageBody() message: Message) {  
        console.log(message)
        chatService.incommingMessage(io, socket, message);
    }
    @OnMessage("create")
    createRoom(@SocketIO() io: SocketIO.Server, @ConnectedSocket() socket: SocketIO.Socket,@MessageBody() roomJoin: RoomJoin) {     
        chatService.createRoom(io, socket, roomJoin);
    }
    @OnMessage("join")
    joinRoom(@SocketIO() io: SocketIO.Server, @ConnectedSocket() socket: SocketIO.Socket,@MessageBody() roomJoin: RoomJoin) {   
        chatService.createRoom(io, socket, roomJoin);
        chatService.getMessages(roomJoin.room).then( (messages: Message[]) => { 
            messages.forEach((message: Message) => {
                message.senderAvatarUrl = avatarsDictionnary.get(message.sender);
            })
            console.log("get history");
            socket.emit('roomMessages', messages);
        });
    }
    @OnMessage("getOwnRooms")
    async getOwnRooms(@ConnectedSocket() socket: SocketIO.Socket,@MessageBody() username: string) { 
        console.log("getOwnRooms");    
        chatService.getOwnRooms(username).then((rooms: String[]) =>{
            if (!rooms) 
                rooms = [];
            socket.emit('ownRooms', rooms);
            console.log("own Rooms")
            console.log(rooms)
        });
    }    
    @OnMessage("getAllRooms")
    getAllRooms(@ConnectedSocket() socket: SocketIO.Socket) {     
        chatService.getAllRooms().then((rooms: String[]) =>{
            socket.emit('allRooms', rooms);
            console.log(rooms);
        });
    }
    @OnMessage("leave")
    quitRoom(@ConnectedSocket() socket: SocketIO.Socket,@MessageBody() roomJoin: RoomJoin) {     
        chatService.leaveRoom(socket, roomJoin);
    }
}