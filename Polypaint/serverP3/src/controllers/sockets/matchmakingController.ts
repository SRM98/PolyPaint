import "reflect-metadata";
import { inject, injectable } from "inversify";
import {
        ConnectedSocket, MessageBody, OnConnect,
        OnDisconnect, OnMessage, SocketController, SocketIO
    } from "socket-controllers";
import { mmService } from "../../services/match/matchmaking";
import Message from "../../models/chat.model";

@SocketController()
@injectable()
export class MatchMakingController {

    @OnMessage("getMatches")
    getMatches(@ConnectedSocket() socket: SocketIO.Socket,@MessageBody() request: any) {
        mmService.getMatches(socket, request);        
    }

    @OnMessage("createMatch")
    createMatch(@SocketIO() io: SocketIO.Server, @ConnectedSocket() socket: SocketIO.Socket,@MessageBody() match: any) {         
        mmService.createMatch(io, socket, match);        
    }


    @OnMessage("joinMatch")
    joinMatch(@SocketIO() io: SocketIO.Server, @ConnectedSocket() socket: SocketIO.Socket,@MessageBody() request: any) {  
        mmService.joinMatch(io, socket, request);
    }

    @OnMessage("editMatch")
    editMatch(@SocketIO() io: SocketIO.Server, @ConnectedSocket() socket: SocketIO.Socket,@MessageBody() request: any) {  
        mmService.editMatch(io, socket, request);
    }

    @OnMessage("leaveMatch")
    leaveMatch(@SocketIO() io: SocketIO.Server, @ConnectedSocket() socket: SocketIO.Socket,@MessageBody() request: any) { 
        mmService.leaveMatch(io, socket, request);
    }
}