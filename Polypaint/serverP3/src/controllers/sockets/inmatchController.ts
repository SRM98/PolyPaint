import { inMatchService } from "../../services/match/inmatch";
import Message from "../../models/chat.model";
import {
    ConnectedSocket, MessageBody, OnConnect,
    OnDisconnect, OnMessage, SocketController, SocketIO
} from "socket-controllers";
import { inject, injectable } from "inversify";
import { request } from "http";

@SocketController()
@injectable()
export class InMatchController {

    @OnMessage("draw")
    draw(@SocketIO() io: SocketIO.Server, @ConnectedSocket() socket: SocketIO.Socket,@MessageBody() message: any) {
        inMatchService.draw(io, socket, message);        
    }

    @OnMessage("startStroke")
    startStroke(@SocketIO() io: SocketIO.Server, @ConnectedSocket() socket: SocketIO.Socket,@MessageBody() message: any) {
        inMatchService.startStroke(io, socket, message);        
    }

    @OnMessage("eraseStroke")
    eraseStroke(@SocketIO() io: SocketIO.Server, @ConnectedSocket() socket: SocketIO.Socket,@MessageBody() message: any) {
        inMatchService.eraseStroke(io, socket, message);        
    }

    @OnMessage("guess")
    guess(@SocketIO() io: SocketIO.Server, @ConnectedSocket() socket: SocketIO.Socket,@MessageBody() match: any) {    
        inMatchService.guess(io, socket, match);        
    }

    @OnMessage("leaveInMatch")
    leaveInMatch(@SocketIO() io: SocketIO.Server, @ConnectedSocket() socket: SocketIO.Socket,@MessageBody() request: any) {    
        inMatchService.leaveInMatch(io, socket, request)       
    }

    @OnMessage("getAvatarUrl")
    getAvatarUrl(@SocketIO() io: SocketIO.Server, @ConnectedSocket() socket: SocketIO.Socket,@MessageBody() request: any) {   
        console.log("AVATAR")
        inMatchService.giveAvatarurl(io, socket, request)       
    }
}