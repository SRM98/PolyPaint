import { IMatch, MatchTypes } from "../../../models/match.model";
import { userSocketMap } from '../../userSocketsMap/userSocketsMap';
import { matchHistoryDB } from '../../../database/matchHistoryDB';
import { IPlayer, AIPlayer, HPlayer } from "../../players/Player";
import { Game } from '../../../models/game.model';
import { gameDB } from '../../../database/gameDB';
import { Classic } from "./ClassicMode";

export class RoundEndInfo {
    guessed: boolean;
    guesser: string;
    message: string;
}

export enum MatchState {
    MatchMaking,
    InMatch,
    Ended,
}

export abstract class IMode extends IMatch{
    constructor(imatch: IMatch){
        super(imatch);
        this.state = MatchState.MatchMaking;
    }
    onMachDestroy: Function = () => {};
    //for match begin
    protected games: Game[];
    protected currentRound: number;

    public state: MatchState;
    public virtualDrawingInterval: any;
    protected matchInterval: any;
    protected roundCallBack: any;
    protected roundTime: number;
    
    public startMatch(io: SocketIO.Server, socket: SocketIO.Socket): void {
        this.state = MatchState.InMatch;
        
        this.games = gameDB.getRandomGames(this.nbRounds);

        if (this.type == MatchTypes.Classic){
            let teamA: IPlayer[] = (this as any).teamA.team;
            let teamB: IPlayer[] = (this as any).teamB.teamA;
            this.getAiPlayers().forEach( (ai: AIPlayer) => ai.init(teamA, teamB));    
        }
        this.getAiPlayers().forEach( (ai: AIPlayer) => ai.init(this.getPlayers()) );

        this.getAiPlayers().forEach( (ai: AIPlayer) => ai.startMatchMessage(io, socket));

        this.setMatchTimeout(io, () =>  this.endMatch(io, socket), this.maxTime );
    }

    public hint(io: SocketIO.Server, socket: SocketIO.Socket, sender: string): void {
        let hintInfo: any = {};
        let clues: string[] = this.games[this.currentRound].clues
        hintInfo.hint = clues[Math.floor(Math.random()*clues.length)];
        hintInfo.username = sender;

        if (this.type === MatchTypes.Classic){
            let drawer: string = (this as any).drawerUsername;
            this.getAiPlayers().forEach( (ai: AIPlayer) => { if(ai.username === drawer) ai.hintMessage(io, socket, hintInfo) });
        } else {
            this.getAiPlayers().forEach( (ai: AIPlayer) => ai.hintMessage(io, socket, hintInfo));
        }
        
    }

    public endMatch(io: SocketIO.Server, socket: SocketIO.Socket, playerLeft?: string): void {
        this.state = MatchState.Ended;
        this.stopMatchEvents();
        this.getAiPlayers().forEach( (ai: AIPlayer) => ai.endMatchMessage(io, socket,this.matchStateInfo()));
        let matchJSON = this.toJSON(false);
        matchJSON.datetime = Date.now();
        matchHistoryDB.addMatch(matchJSON);
    }

    public nextRound(io: SocketIO.Server, socket: SocketIO.Socket, roundEndInfo: RoundEndInfo): void {
        this.getAiPlayers().forEach( (ai: AIPlayer) => ai.nextRoundMessage(io, socket, roundEndInfo, this.matchStateInfo()));
    }

    public sendToUsers(io: SocketIO.Server, users: IPlayer[], message: string, content: any): void {
        users.forEach((player: IPlayer) => {
            if (player.isHuman()) {
                io.to(userSocketMap.getSocketId(player.username)).emit(message, content); 
            }
        });
    }

    public setRoundTimeout(callback: Function, milliseconds: number) {
        this.roundTime = milliseconds / 1000;
        this.roundCallBack = callback;
    }

    public setMatchTimeout(io: SocketIO.Server, callback: Function, seconds: number) {
        this.matchInterval = setInterval( () => {
            if (this.roundTime > 0)
                this.roundTime -= 1;
            this.maxTime -= 1;
            
            
            io.sockets.in(this.id).emit("timerUpdate", { matchTime: this.maxTime, roundTime: this.roundTime});
            if (this.maxTime <= 0){
                clearInterval(this.matchInterval);
                callback();
            }
            if (this.roundTime <= 0){
                this.roundCallBack();
            }
        }, 1000);
    }

    public stopGameEvents(): void {
        this.roundCallBack = () => {};
        clearInterval(this.virtualDrawingInterval);
    }

    public stopMatchEvents(): void {
        this.stopGameEvents();
        clearInterval(this.matchInterval);
    }

    abstract matchStateInfo(): any;
    abstract guess(io: SocketIO.Server, socket: SocketIO.Socket, guess: string): void;

    //for matchmaking only
    baseJSON(): any {
        let maxPlayers;
        let placesLeft;
        switch(this.type) {
            case MatchTypes.Classic: maxPlayers = 4; placesLeft = maxPlayers - (this.getPlayers().length + this.getAiPlayers().length); break; 
            case MatchTypes.Coop: maxPlayers = 3; placesLeft = maxPlayers - this.getPlayers().length ; break; 
            case MatchTypes.Duel: maxPlayers = 2; placesLeft = maxPlayers - this.getPlayers().length ; break;
            case MatchTypes.Solo: maxPlayers = 1; placesLeft = maxPlayers - this.getPlayers().length ; break;
        }
        return {
                    id: this.id,
                    creator: this.creator,
                    name: this.name,
                    nbRounds: this.nbRounds,
                    type: this.type,
                    playerCount: this.getPlayers().length,
                    placesLeft: placesLeft,
               } 
    }
    abstract userJoin(io: SocketIO.Server, socket: SocketIO.Socket, request: any): boolean;
    abstract canStart(): boolean;
    abstract editMatch(io: SocketIO.Server, socket: SocketIO.Socket, request: any): void;
    abstract userLeave(io: SocketIO.Server, socket: SocketIO.Socket, request: any): void;
    abstract getPlayers(): HPlayer[];
    abstract getAiPlayers(): AIPlayer[];
    abstract toJSON(matchmakingJSON?: boolean): any;
    
    protected matchmakingError(socket: SocketIO.Socket, message: string) {
        socket.emit("matchmakingError", { description: message } );
    }
}