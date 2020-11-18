import { IMatch, TeamInfos, MatchEditActions } from "../../../models/match.model";
import { IMode, RoundEndInfo } from "./IMode";
import { AIPlayer, HPlayer, IPlayer } from "../../players/Player";
import { Game } from "../../../models/game.model";
import { PlayingTeam } from "../../../models/match.model";
import VirtuaDrawing from '../../virtualDrawing/virtualDrawing';
import {timeToGuessDifficultyMapper, bonusTimeByDifficulty, triesDifficulty} from '../../../models/inmatchAttributs';
import { userSocketMap } from '../../userSocketsMap/userSocketsMap';
import { matchHistoryDB } from '../../../database/matchHistoryDB';
import { gameDB } from '../../../database/gameDB';
import { CoopStatsService } from "../../Stats/CoopStatsService";



export class Coop extends IMode {
    
    readonly maxTries: number = 3;

    private players: TeamInfos;
    private aiplayer: AIPlayer;
    private guessers: string[] = [];
    private drawerUsername: string;
    private virtualDrawer: VirtuaDrawing;
    private statsService: CoopStatsService
    private startingTime: Date;
    
    constructor(match: IMatch) {
        super(match);

        this.players = { team: [ new HPlayer(match.creator) ], tries: 3, playerIndex: 0, points: 0} as TeamInfos;
        this.aiplayer = new AIPlayer(this.type, this.id);
        this.currentRound = 0;
        this.virtualDrawer = new VirtuaDrawing();
        this.statsService = new CoopStatsService();
    }
    
    startMatch(io: SocketIO.Server, socket: SocketIO.Socket): void {
        super.startMatch(io , socket);
        this.startingTime = new Date();
        this.startRound(io, socket);
    }

    canStart(): boolean {
        return this.getPlayers().length >= 2 && (this.players.team.filter((player: IPlayer) => !player.ready).length === 0);
    }

    matchStateInfo() {
        return { timeLeft: this.maxTime,
                 guessers: this.guessers };
    }

    startRound(io: SocketIO.Server, socket: SocketIO.Socket) {
        this.players.tries = triesDifficulty(this.games[this.currentRound%this.nbRounds].difficulty); 
        this.drawerUsername = null;

        this.virtualDrawingInterval = this.virtualDrawer.draw(this.games[this.currentRound%this.nbRounds], this.id,
                io, socket);
        
        this.notifyRoundResponsabilities(io, socket);
        this.setRoundTimeout(() => { 
            this.nextRound(io, socket, { guessed: false, guesser: "", 
                                         message: "the word to guess was: " + this.games[this.currentRound%this.nbRounds].word });
        }, timeToGuessDifficultyMapper(this.games[this.currentRound%this.nbRounds].difficulty)

        );
    }

    notifyRoundResponsabilities(io: SocketIO.Server, socket: SocketIO.Socket) {
        io.sockets.in(this.id).emit("roundStart", {
            drawer: this.drawerUsername,
            roundDuration: timeToGuessDifficultyMapper(this.games[this.currentRound%this.nbRounds].difficulty) / 1000,
            isGuessing: true,
            guessesLeft: this.players.tries
        });
    }


    nextRound(io: SocketIO.Server, socket: SocketIO.Socket, roundEndMessage: RoundEndInfo) {
        this.stopGameEvents();

        io.sockets.in(this.id).emit("roundEnd", { message: roundEndMessage.message, teamA: this.players.points } );

        super.nextRound(io ,socket, roundEndMessage);

        setTimeout(() => {
            this.currentRound++;
            this.startRound(io, socket);
        }, 3000);
    }

    async endMatch(io: SocketIO.Server, socket: SocketIO.Socket, playerLeft?: string) {
        super.endMatch(io ,socket);
        
        const matchDuration = (new Date().getTime() - this.startingTime.getTime())/1000;
        console.log("le match a duree :" + matchDuration);
        this.startingTime = null;
        
        await this.statsService.updateNbGame(this.players);
        await this.statsService.updateAverageMatchesTime(this.players, matchDuration);
        await this.statsService.updateTotalMatchesTime(this.players, matchDuration);

        io.sockets.in(this.id).emit("matchEnd", {
            wins: false,
            reason: "The match has ended!" + `Your final score is ${this.players.points}`
        });
    }

    guess(io: SocketIO.Server, socket: SocketIO.Socket, guess: string): void {
        if (guess === this.games[this.currentRound%this.nbRounds].word) {
            this.maxTime += bonusTimeByDifficulty(this.games[this.currentRound%this.nbRounds].difficulty);
            this.guessers.push(userSocketMap.getUsernameBySocket(socket.id));
            this.players.points++;
            this.nextRound(io, socket, { guessed: true, guesser: userSocketMap.getUsernameBySocket(socket.id),
                                         message: this.guessers[this.guessers.length - 1] + " have have guessed the word" });
        } else {
            this.players.tries--;    
            if(this.players.tries <= 0)
                this.nextRound(io, socket, { guessed: false, guesser: "", 
                                             message: "the word to guess was: " + this.games[this.currentRound%this.nbRounds].word });

            else 
                io.sockets.in(this.id).emit("badGuess", { triesLeft: this.players.tries} );
        }
    }
    
    userJoin(io: SocketIO.Server, socket: SocketIO.Socket, request: any): boolean {
        if (this.players.team.length < 3){
            this.players.team.push(new HPlayer(request.username));
            return true;
        } 
        return false;
    }

    userLeave(io: SocketIO.Server, socket: SocketIO.Socket, request: any): void {
        this.players.team = this.players.team.filter((value: HPlayer) => { return value.username != request.username} );
    }

    getPlayers(): HPlayer[] {
        return this.players.team;
    }

    getAiPlayers(): AIPlayer[] {
        return [this.aiplayer];
    }

    toJSON(matchmakingJSON: boolean = false) {
        let baseJSON:any = this.baseJSON();
        baseJSON.players = this.players.team.map((player: HPlayer) => player.toJSON(matchmakingJSON));
        baseJSON.aiplayer = this.aiplayer.username;
        baseJSON.scoreA = this.players.points;
        return baseJSON; 
    }
    
    editMatch(io: SocketIO.Server, socket: SocketIO.Socket, request: any): void {
        if (request.action === MatchEditActions.Ready) {
            let username: string = request.options;
            this.players.team.forEach((player: IPlayer) => { if (player.username === username) player.ready = !player.ready });
        }
    }
}