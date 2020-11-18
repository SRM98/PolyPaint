import "reflect-metadata";
import Message from "../../models/chat.model";
import ChatDB from "../../database/chatDB";
import { IMatch, MatchTypes } from "../../models/match.model";
import { Classic } from "./modes/ClassicMode";
import { Solo } from "./modes/SoloMode";
import { Coop } from "./modes/CoopMode";
import { Duel } from "./modes/DuelMode";
import { IMode, MatchState } from "./modes/IMode";
import { avatarsDictionnary } from "../../Image/imageManager";
import { HPlayer } from "../players/Player";


export class InMatchService {
    public matches: Map<string, IMode>;
    private db: ChatDB;
    
    public constructor() {
        this.matches = new Map();
    }
    
    public draw(io: SocketIO.Server, socket: SocketIO.Socket, json: any): void {
        socket.to(json.room as string).emit('draw', json.content); 
    }

    public startStroke(io: SocketIO.Server, socket: SocketIO.Socket, json: any): void {
        socket.to(json.room as string).emit('startStroke', json.content); 
    }

    public eraseStroke(io: SocketIO.Server, socket: SocketIO.Socket, json: any): void {
        socket.to(json.room as string).emit('eraseStroke', json); 
    }
    
    public guess(io: SocketIO.Server, socket: SocketIO.Socket, matchObject: any): void {
        if( matchObject.id && matchObject.guess && this.matches.has(matchObject.id)){
            matchObject.guess = (matchObject.guess as string).toLowerCase();
            this.matches.get(matchObject.id).guess(io, socket, matchObject.guess);
        }
    }

    public addMatch(match: IMode): void {
        match.onMachDestroy = () => { this.matches.delete(match.id) };
        this.matches.set(match.id, match);
    }

    public async leaveInMatch(io: SocketIO.Server, socket: SocketIO.Socket, request: any): Promise<void> {
        if( request.id ){
            socket.leave(request.id);
            socket.emit("leftMatch", request.id);
        } else if (request.username) {
            this.matches.forEach((match: IMode) => {
                if (match.getPlayers().some((player: HPlayer) => player.username === request.username)){
                    request.id = match.id;
                }
            });
        }
        
        socket.leave(request.id);
        if( request.id && request.username && this.matches.has(request.id)){
            let match: IMode = this.matches.get(request.id); 
            
            if (match.state === MatchState.InMatch) {
                await match.endMatch(io, socket, request.username);
            }
            match.userLeave(io, socket, request);

            if (match.getPlayers().length === 0)
                match.onMachDestroy();
        }
    }

    public giveAvatarurl(io, socket, request: any, toAll: boolean = false): void {
        
        if (!request.players) {
            request.players = this.matches.get(request.matchID).getPlayers().map((value: HPlayer) => value.username);
            request.players = request.players.concat(this.matches.get(request.matchID).getAiPlayers().map((value: HPlayer) => value.username));
        }
        let players = request.players;
        
        let avatars: string[] = players.map(element => {
            if (avatarsDictionnary.has(element)) 
                return avatarsDictionnary.get(element);
            else
                return "imagesStatic/defaultAvatar.png";
        });
        request.url = avatars;
        console.log(JSON.stringify(request));
        
        socket.emit("giveAvatarUrl", request);
    }
}

export var inMatchService: InMatchService = new InMatchService();