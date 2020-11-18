import { IMatch, MatchEditActions } from "../../../models/match.model";
import { AIPlayer, HPlayer } from "../../players/Player";
import { IMode, RoundEndInfo } from "./IMode";
import { PlayingTeam } from "../../../models/match.model";
import VirtuaDrawing from '../../virtualDrawing/virtualDrawing';
import {timeToGuessDifficultyMapper} from '../../../models/inmatchAttributs';
import { userSocketMap } from '../../userSocketsMap/userSocketsMap';
import { Game } from "../../../models/game.model";
import { matchHistoryDB } from '../../../database/matchHistoryDB';
import { gameDB } from '../../../database/gameDB';
import { DuelStatsService } from "../../Stats/DuelStatsService";

export class Duel extends IMode {

    private player1: HPlayer;
    private player1score: number;
    private player2: HPlayer;
    private player2score: number;
    private aiplayer: AIPlayer;
    private guessers: string[] = [];
    private drawerUsername: string;
    private virtualDrawer: VirtuaDrawing;
    private statsService: DuelStatsService
    private startingTime: Date;

    constructor(match: IMatch) {
        super(match);
        this.player1 =  new HPlayer(match.creator);
        this.player2 = new HPlayer("");
        this.player1score = 0;
        this.player2score = 0;
        this.aiplayer = new AIPlayer(this.type, this.id);
        this.currentRound = 0;
        this.virtualDrawer = new VirtuaDrawing();
        this.statsService = new DuelStatsService();
    }

    startMatch(io: SocketIO.Server, socket: SocketIO.Socket): void {
        super.startMatch(io , socket);
        this.startingTime = new Date();
        this.startRound(io, socket);
    }

    canStart(): boolean {
        return this.getPlayers().length == 2 && this.player1.ready && this.player2.ready;
    }

    matchStateInfo() {
        let winnerTeam: PlayingTeam = PlayingTeam.noTeam;
        if (this.player1score > this.player2score) winnerTeam = PlayingTeam.teamA;
        if (this.player2score > this.player1score) winnerTeam = PlayingTeam.teamB;

        return { currentRound: this.currentRound, maxRound: this.nbRounds,
                 guessers: this.guessers, winningTeam: winnerTeam };
    }

    startRound(io: SocketIO.Server, socket: SocketIO.Socket) {
        this.virtualDrawingInterval = this.virtualDrawer.draw(this.games[this.currentRound], this.id,
            io, socket);
        
        this.notifyRoundResponsabilities(io, socket);
        this.setRoundTimeout(() => { 
                this.nextRound(io, socket, { guessed: false, guesser: "",
                                            message: "the word to guess was: " + this.games[this.currentRound].word} ) 
                },
            timeToGuessDifficultyMapper(this.games[this.currentRound].difficulty)
        );
    }

    notifyRoundResponsabilities(io: SocketIO.Server, socket: SocketIO.Socket) {
        io.sockets.in(this.id).emit("roundStart", {
            drawer: this.drawerUsername,
            roundDuration: timeToGuessDifficultyMapper(this.games[this.currentRound].difficulty) / 1000,
            isGuessing: true,
            guessesLeft: 420,
        });
    }

    nextRound(io: SocketIO.Server, socket: SocketIO.Socket, roundEndMessage: RoundEndInfo) {
        this.stopGameEvents();
        if (roundEndMessage.guessed)
            roundEndMessage.guesser === this.player1.username ? this.player1score++ : this.player2score++;

        io.sockets.in(this.id).emit("roundEnd", { message: roundEndMessage.message, teamA: this.player1score, teamB: this.player2score } );

        super.nextRound(io ,socket, roundEndMessage);

        setTimeout(() => {
            this.currentRound++;
            if (this.currentRound >= this.nbRounds)
                this.endMatch(io, socket);
            else
                this.startRound(io, socket);
        }, 3000);
    }

    async endMatch(io: SocketIO.Server, socket: SocketIO.Socket, playerLeft?: string) {
        super.endMatch(io ,socket);

        let winnerAndLoser: any = this.getWinners(playerLeft);
        console.log(JSON.stringify(winnerAndLoser));
        
        if (winnerAndLoser.tied) {
            io.sockets.to(this.id).emit("matchEnd", {
                wins: false,
                reason: "The match is tied!"
            });
        } else {

            let additionalReasonWinner: string = !playerLeft ? winnerAndLoser.message : " Your opponent has left the match";
            
            io.to(userSocketMap.getSocketId(winnerAndLoser.winner)).emit("matchEnd", {
                wins: true,
                reason: "Victory!" + additionalReasonWinner,
            });
            if (!playerLeft)
                io.to(userSocketMap.getSocketId(winnerAndLoser.loser)).emit("matchEnd", {
                    wins: false,
                    reason: "Defeat!" + additionalReasonWinner,
                });
        }


        const matchDuration = (new Date().getTime() - this.startingTime.getTime())/1000;
        this.startingTime = null;

        await this.statsService.updateNbGame(winnerAndLoser.winner, winnerAndLoser.loser);
        if (!winnerAndLoser.tied)
            await this.statsService.updateWinRate(winnerAndLoser.winner, winnerAndLoser.loser);
        await this.statsService.updateAverageMatchesTime(winnerAndLoser.winner, winnerAndLoser.loser, matchDuration);
        await this.statsService.updateTotalMatchesTime(winnerAndLoser.winner, winnerAndLoser.loser, matchDuration);

    }

    getWinners(playerLeft?: string): any {
        if (playerLeft) {
            let winner: string = playerLeft === this.player1.username ? this.player2.username : this.player1.username; 
            return { winner: winner };
        } else {
            if (this.player1score > this.player2score)
                return { winner: this.player1.username, loser: this.player2.username, 
                         message: `The final score is ${this.player1score}-${this.player2score} in favor of ${this.player1.username}` };
            else if (this.player2score > this.player1score)
                return { winner: this.player2.username, loser: this.player1.username,
                         message: `The final score is ${this.player2score}-${this.player1score} in favor of ${this.player2.username}` };
            else
                return { tied: true };
        }   
    }

    guess(io: SocketIO.Server, socket: SocketIO.Socket, guess: string): void {
        try {
            if (guess === this.games[this.currentRound].word) {
                let guesser: string = userSocketMap.getUsernameBySocket(socket.id);
                this.guessers.push(guesser);
                this.nextRound(io, socket, { guessed: true, guesser: guesser, 
                                             message: "" + guesser + " has guessed the word" });
            } else {
                socket.emit("badGuess", { triesLeft: 69} );
            }
        } catch(e) {
            console.log(e);
        }            
    }

    userJoin(io: SocketIO.Server, socket: SocketIO.Socket, request: any): boolean {
        if ((this.player1.username === "")){
            this.player1 = new HPlayer(request.username);
            return true;
        }
        if ((this.player2.username === "")) {
            this.player2 = new HPlayer(request.username);
            return true;
        }

        return false;
    }

    userLeave(io: SocketIO.Server, socket: SocketIO.Socket, request: any): void {
        if (this.player1.username === request.username)
            this.player1 = new HPlayer("");
        else if (this.player2.username === request.username)
            this.player2 = new HPlayer("");;
    }

    toJSON(matchmakingJSON: boolean = false) {
        let baseJSON:any = this.baseJSON();
        baseJSON.aiplayer = this.aiplayer.username;
        baseJSON.players = [ this.player1, this.player2];
        baseJSON.player1 = this.player1.toJSON(matchmakingJSON);
        baseJSON.player2 = this.player2.toJSON(matchmakingJSON);
        baseJSON.scoreA = this.player1score;
        baseJSON.scoreB = this.player2score;
        return baseJSON; 
    }

    getPlayers(): HPlayer[] {
        let players: HPlayer[] = [];
        if ((this.player1.username !== "")) players.push(this.player1);
        if ((this.player2.username !== "")) players.push(this.player2);
        return players;
    }

    getAiPlayers(): AIPlayer[] {
        return [ this.aiplayer ];
    }

    editMatch(io: SocketIO.Server, socket: SocketIO.Socket, request: any): void {
        if (request.action === MatchEditActions.Ready) {
            let username: string = request.options;
            if (this.player1.username === username) this.player1.ready = !this.player1.ready;
            if (this.player2.username === username) this.player2.ready = !this.player2.ready;
        }
    }
}