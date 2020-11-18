import { IMatch, TeamInfos } from "../../../models/match.model";
import { IMode, RoundEndInfo } from "./IMode";
import { AIPlayer, HPlayer } from "../../players/Player";
import { Game } from "../../../models/game.model";
import { PlayingTeam } from "../../../models/match.model";
import VirtuaDrawing from '../../virtualDrawing/virtualDrawing';
import {timeToGuessDifficultyMapper, bonusTimeByDifficulty, triesDifficulty} from '../../../models/inmatchAttributs';
import { userSocketMap } from '../../userSocketsMap/userSocketsMap';
import { gameDB } from '../../../database/gameDB';
import { SoloStatsService } from "../../Stats/SoloStatsService";


export class Solo extends IMode {

    readonly maxTries: number = 3;

    private players: TeamInfos;
    private aiplayer: AIPlayer;
    private drawerUsername: string;
    private virtualDrawer: VirtuaDrawing;
    private statsService: SoloStatsService
    private startingTime: Date;
    
    constructor(match: IMatch) {
        super(match);

        this.players = { team: [ new HPlayer(match.creator) ], tries: 3, playerIndex: 0, points: 0} as TeamInfos;
        this.aiplayer = new AIPlayer(this.type, this.id);
        this.games = gameDB.getRandomGames(this.nbRounds);
        this.currentRound = 0;
        this.virtualDrawer = new VirtuaDrawing();
        this.statsService = new SoloStatsService();        
    }
    
    startMatch(io: SocketIO.Server, socket: SocketIO.Socket): void {
        super.startMatch(io , socket);
        this.startingTime = new Date();
        this.startRound(io, socket);
    }

    canStart(): boolean {
        return true;
    }

    matchStateInfo() {
        return { timeLeft: this.maxTime };
    }

    startRound(io: SocketIO.Server, socket: SocketIO.Socket) {
        this.players.tries = triesDifficulty(this.games[this.currentRound%this.nbRounds].difficulty); 
        this.drawerUsername = null;

        this.virtualDrawingInterval = this.virtualDrawer.draw(this.games[this.currentRound%this.nbRounds], this.id,
                io, socket);
        
        this.notifyRoundResponsabilities(io, socket);
        this.setRoundTimeout(() => { this.nextRound(io, socket, { guessed: false, guesser: "",
                                                                            message: "The word to guess was: " + this.games[this.currentRound%this.nbRounds].word}); },
            timeToGuessDifficultyMapper(this.games[this.currentRound%this.nbRounds].difficulty)
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

        io.sockets.in(this.id).emit("matchEnd", {
            wins: false,
            reason: "The match has ended!" + `Your final score is ${this.players.points}`
        });

        const matchDuration = (new Date().getTime() - this.startingTime.getTime())/1000;
        console.log("le match a duree :" + matchDuration);
        this.startingTime = null;
        
        await this.statsService.updateNbGame(this.players.team[0].username);
        await this.statsService.updateAverageMatchesTime(this.players.team[0].username, matchDuration);
        await this.statsService.updateTotalMatchesTime(this.players.team[0].username, matchDuration);
    }

    guess(io: SocketIO.Server, socket: SocketIO.Socket, guess: string): void {
        if (guess === this.games[this.currentRound%this.nbRounds].word) {
            this.maxTime += bonusTimeByDifficulty(this.games[this.currentRound%this.nbRounds].difficulty);
            this.players.points++;
            this.nextRound(io, socket, { guessed: true, guesser: this.creator,
                                         message: "You have have guessed the word"});
        } else {
            this.players.tries--;    
            if(this.players.tries <= 0)
                this.nextRound(io, socket, { guessed: false, guesser: "",
                                             message: "The word to guess was: " + this.games[this.currentRound%this.nbRounds].word})
            else 
                io.sockets.in(this.id).emit("badGuess", { triesLeft: this.players.tries} );
        }
    }
    userJoin(io: SocketIO.Server, socket: SocketIO.Socket, request: any): boolean {
        throw new Error("Cant join in Solo");
    }
    userLeave(io: SocketIO.Server, socket: SocketIO.Socket, request: any): void {
        this.onMachDestroy();
    }
    
    toJSON(matchmakingJSON: boolean = false): any {
        let baseJSON: any = this.baseJSON();
        baseJSON.players = [this.creator];
        baseJSON.aiplayer = this.aiplayer.username;
        return baseJSON;
    }

    editMatch(io: SocketIO.Server, socket: SocketIO.Socket, request: any): void {
        throw new Error("Method not implemented.");
    }
    
    getPlayers(): HPlayer[] {
        return [new HPlayer(this.creator)];
    }

    getAiPlayers(): AIPlayer[] {
        return [this.aiplayer];
    }

}