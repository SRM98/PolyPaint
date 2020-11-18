import "reflect-metadata";
import Message from "../../models/chat.model";
import ChatDB from "../../database/chatDB";
import { IMatch, MatchTypes } from "../../models/match.model";
import { Classic } from "./modes/ClassicMode";
import { Solo } from "./modes/SoloMode";
import { Coop } from "./modes/CoopMode";
import { Duel } from "./modes/DuelMode";
import { IMode } from "./modes/IMode";
import { inMatchService } from "./inmatch";
import { HPlayer } from "../players/Player";
import { userSocketMap } from "../userSocketsMap/userSocketsMap";
import { gameDB } from "../../database/gameDB";

class MatchKey {
    name: string;
    type: MatchTypes
}

export class MatchMakingService {
    public matches: Map<string, IMode>;
    
    public constructor() {
        this.matches = new Map<string, IMode>();
    }
    
    private getKey(match: IMatch): string{
        return match.name + match.type ;
    }

    private matchmakingError(socket: SocketIO.Socket, message: string) {
        socket.emit("matchmakingError", { description: message } );
    }

    public createMatch(io: SocketIO.Server, socket: SocketIO.Socket, matchObject: any) {
        let match: IMatch = matchObject as IMatch;
        if( match.creator != undefined && match.name != undefined && match.type != undefined ) {
            if ( !this.matches.has(this.getKey(match))) {

                if ( (match.type == MatchTypes.Duel ||  match.type == MatchTypes.Classic) && gameDB.getNbGames() < match.nbRounds) {
                    this.matchmakingError(socket, "We do not have enough games for the moment, maximum for now is " + gameDB.getNbGames() + "games");
                    return;
                }

                let newMatch: IMode = this.matchInit(match);
                this.matches.set(newMatch.id, newMatch);
                socket.join(newMatch.id);
                
                userSocketMap.setSocketId(newMatch.creator, socket.id);               
                if(newMatch.type === MatchTypes.Solo) {
                    this.startMatch(io, socket, {id : newMatch.id});
                    socket.emit('matchJoined', { type: newMatch.type, content:  newMatch.toJSON() } );
                    return;
                }
                socket.emit('matchJoined', { type: newMatch.type, content:  newMatch.toJSON(true) } );
                socket.broadcast.emit('matchCreated', { type: newMatch.type, content:  newMatch.toJSON(true) } ); 
            } else {
                console.log("match name already present");
                this.matchmakingError(socket, "match name already present");
            }
        } else {
            console.log("match creation error, not all attributes defined");
            this.matchmakingError(socket, "match creation error, not all attributes defined");
        }  
    }

    private matchInit(match: IMatch): IMode {
        switch(match.type) {
            case MatchTypes.Classic: return new Classic(match);
            case MatchTypes.Solo:    return new Solo(match);
            case MatchTypes.Coop:    return new Coop(match);
            case MatchTypes.Duel:    return new Duel(match);
        }
    }

    public getMatches(socket: SocketIO.Socket, request: any) {
        console.log(Array.from(this.matches.keys()));
        console.log(request.type)
        
        var matchesJSON: any[] = [];
        try {
            this.matches.forEach( (value: IMode) => {
                if (value.type === request.type)
                    matchesJSON.push(value.toJSON(true));
            });

        } catch (e) {
            console.log(e);
            
        }
        console.log("getMatches: " + matchesJSON);
        
        request.content = matchesJSON;
        console.log(socket.id);
        socket.emit('getMatches', request);
    }
    public getbroadcastMatches(socket: SocketIO.Socket, request: any) {
        console.log(Array.from(this.matches.keys()));
        console.log(request.type)
        
        var matchesJSON: any[] = [];
        try {
            this.matches.forEach( (value: IMode) => {
                if (value.type === request.type)
                    matchesJSON.push(value.toJSON(true));
            });

        } catch (e) {
            console.log(e);
            
        }
        console.log("getMatches: " + matchesJSON);
        
        request.content = matchesJSON;
        
        socket.broadcast.emit('getMatches', request);
    }

    public startMatch(io: SocketIO.Server, socket: SocketIO.Socket, request: any) {
        if (request.id) {
            var key: string = request.id;
            if (this.matches.has(key) && this.matches.get(key).canStart()){
                let match: IMode =  this.matches.get(key);
                console.log("match has started " + key);

                inMatchService.addMatch(match);
                if (match.type === MatchTypes.Solo)
                    socket.emit('matchStarted', { type: match.type, content: match.id, match: match.toJSON() } );
                else 
                    io.sockets.emit('matchStarted', { type: match.type, content: match.id, match: match.toJSON() } );

                this.matches.delete(key);
                setTimeout(()=> {
                    match.startMatch(io, socket);
                }, 3000);
            } else {
                this.matchmakingError(socket, 'Cannot start match');
                console.log('Cannot start match: ' + request.match);
            }
        } else {
            console.log('request has no attribute name and/or type: ' + request);
        }
    }

    public joinMatch(io: SocketIO.Server, socket: SocketIO.Socket, request: any) {
        console.log("join match")
        console.log(request)
        if (request.username && request.id) {
            console.log("join match")
            if ( this.matches.has(request.id) ){
                console.log("join match")
                if(this.matches.get(request.id).userJoin(io, socket, request)) {
                    console.log("join match")
                    let editedMatch: IMode = this.matches.get(request.id);
                    socket.join(request.id);
                    userSocketMap.setSocketId(request.username, socket.id);               
                    socket.emit('matchJoined', { type: editedMatch.type, content: editedMatch.toJSON(true) });
                    socket.broadcast.emit('matchEdit', { type: editedMatch.type, content: editedMatch.toJSON(true) } );
                } else {
                    console.log("2")
                    this.matchmakingError(socket, "The match was full was trying to join");
                }
            } else {
                this.matchmakingError(socket, "The match no longer exist");
            }
        } else {
            console.log('request has no attribute name or username or type: ' + request);
        }
    }

    public editMatch(io: SocketIO.Server, socket: SocketIO.Socket, request: any) {
        if (request.id && request.action != undefined) {
            if ( this.matches.has(request.id) ){
                this.matches.get(request.id).editMatch(io, socket, request);
                let editedMatch: IMode = this.matches.get(request.id);
                if (editedMatch.canStart())
                    this.startMatch(io, socket, {id: editedMatch.id});
                else
                    io.emit('matchEdit', { type: editedMatch.type, content: editedMatch.toJSON(true) } );
                
            } else {
                this.matchmakingError(socket, "The match no longer exist");
            }
        } else {
            console.log('request has no attribute id or action: ' + JSON.stringify(request));
        }
    }

    
    public leaveMatch(io: SocketIO.Server, socket: SocketIO.Socket, request: any) {
        if (request.username && !request.id) {
            this.matches.forEach((match: IMode) => {
                if (match.getPlayers().some((player: HPlayer) => player.username === request.username))
                    request.id = match.id;
            });
        }

        if (request.username && request.id) {
            if ( this.matches.has(request.id) ){
                this.matches.get(request.id).userLeave(io, socket, request);
                let editedMatch: IMode = this.matches.get(request.id);
                socket.leave(request.id);
                console.log(request.id)
                socket.emit("leftMatch", request.id);
                if (editedMatch.getPlayers().length <= 0) {
                    this.matches.delete(editedMatch.id);
                } 

                io.sockets.emit('matchEdit', { type: editedMatch.type, content: editedMatch.toJSON(true) } );
                this.getbroadcastMatches(socket, request)
            }
        } else {
            console.log('request has no attribute name or username or type: ' + request);            
        }
    }
    

}


export var mmService: MatchMakingService = new MatchMakingService();