import { IMatch, TeamInfos, PlayingTeam} from "../../../models/match.model";
import { IMode, RoundEndInfo } from "./IMode";
import { IPlayer, HPlayer, AIPlayer } from "../../players/Player";
import { MatchEditActions } from '../../../models/match.model';
import VirtuaDrawing from '../../virtualDrawing/virtualDrawing';
import {timeToGuessDifficultyMapper, triesDifficulty} from '../../../models/inmatchAttributs';
import { userSocketMap } from '../../userSocketsMap/userSocketsMap';
import { accountDB } from "../../../database/accountDB";
import Stats from "../../../models/stats.model"
import { ClassicStatsService } from "../../Stats/ClassicStatsService";
import { gameDB } from '../../../database/gameDB';



export class Classic extends IMode {
    
    readonly replyTime: number = 15000;
    public teamA: TeamInfos;
    public teamB: TeamInfos;
    private guessers: string[] = [];
    private currentPlayingTeam: PlayingTeam;
    private drawerUsername: string;
    private virtualDrawer: VirtuaDrawing;
    private replyState: boolean;
    private statsService: ClassicStatsService
    private startingTime: Date;

    constructor(match: IMatch) {
        super(match);
        this.teamA = {
            teamName: "team A",
            team: [ new HPlayer(this.creator)],
            playerIndex: 0,
            tries: 0,
            points: 0,
        };
        this.teamB = {
            teamName: "team B",
            team: [],
            playerIndex: 0,
            tries: 0,
            points: 0,
        };
        this.currentRound = 0;
        this.virtualDrawer = new VirtuaDrawing();
        this.replyState = false;
        this.statsService = new ClassicStatsService();
    }
    
    startMatch(io: SocketIO.Server, socket: SocketIO.Socket): void {
        super.startMatch(io , socket);
        this.startingTime = new Date();
        this.currentPlayingTeam = PlayingTeam.teamA;
        this.startRound(io, socket);
    }

    canStart(): boolean {
        let teamAReady: boolean = (this.teamA.team.filter((player: IPlayer) => !player.ready).length === 0);
        let teamBReady: boolean = (this.teamB.team.filter((player: IPlayer) => !player.ready).length === 0);
        return this.teamA.team.length >= 2 && this.teamB.team.length >= 2 && teamAReady && teamBReady;
    }

    matchStateInfo() {
        this.getWinningTeam
        let winnerTeam: PlayingTeam = undefined;
        if (this.teamA.points > this.teamB.points) winnerTeam = PlayingTeam.teamA;
        if (this.teamB.points > this.teamA.points) winnerTeam = PlayingTeam.teamB;

        return { currentRound: this.currentRound, maxRound: this.nbRounds,
                 guessers: this.guessers, winningTeam: winnerTeam };
    }

    startRound(io: SocketIO.Server, socket: SocketIO.Socket) {
        console.log("start round");
        
        let currentTeam: TeamInfos = this.currentPlayingTeam === PlayingTeam.teamA ? this.teamA : this.teamB;
        console.log("playing team: " + this.currentPlayingTeam);
        
        currentTeam.tries = triesDifficulty(this.games[this.currentRound].difficulty); 
        this.drawerUsername = undefined;
        this.replyState = false;
        for (let i = 0; i < currentTeam.team.length; ++i) {
            if (!currentTeam.team[i].isHuman()) {
                this.drawerUsername = currentTeam.team[i].username;
            }
        }
        if (!this.drawerUsername) {
            console.log("human is drawing");
            currentTeam.playerIndex++;
            currentTeam.playerIndex %= currentTeam.team.length;
            this.drawerUsername = currentTeam.team[currentTeam.playerIndex].username;
        } else {
            console.log("bot is drawing");
            this.virtualDrawingInterval = this.virtualDrawer.draw(this.games[this.currentRound], this.id,
                io, socket);
        }
        this.notifyRoundResponsabilities(io, socket);
        this.setRoundTimeout(() => { this.reply(io, socket) },
            timeToGuessDifficultyMapper(this.games[this.currentRound].difficulty)
        );
    }

    notifyRoundResponsabilities(io: SocketIO.Server, socket: SocketIO.Socket) {
        if (this.drawerUsername.search("(Ai Player)") === -1 ) {
            console.log(this.drawerUsername + " is drawing");
            io.to(userSocketMap.getSocketId(this.drawerUsername)).emit("roundStartDrawer", {
                roundDuration: timeToGuessDifficultyMapper(this.games[this.currentRound].difficulty) / 1000,
                wordToDraw: this.games[this.currentRound].word
            });
        }
        let guessers: TeamInfos = this.teamA;
        let viewers: TeamInfos = this.teamB;
        if (this.currentPlayingTeam === PlayingTeam.teamB) {
            guessers = this.teamB;
            viewers = this.teamA;
        }
        let guessersUsernames: IPlayer[] = guessers.team.filter((value: IPlayer) => value.username !== this.drawerUsername);
        this.sendToUsers(io, guessersUsernames, "roundStart", {
            drawer: this.drawerUsername,
            roundDuration: timeToGuessDifficultyMapper(this.games[this.currentRound].difficulty) / 1000,
            isGuessing: true,
            guessesLeft: guessers.tries
        });
        this.sendToUsers(io, viewers.team, "roundStart", {
            drawer: this.drawerUsername,
            roundDuration: timeToGuessDifficultyMapper(this.games[this.currentRound].difficulty) / 1000,
            isGuessing: false,
            guessesLeft: viewers.tries
        })
    }

    reply(io: SocketIO.Server, socket: SocketIO.Socket) {
        this.stopGameEvents();
        
        this.replyState = true;
        let guessers: IPlayer[] = this.teamA.team;
        let viewers: IPlayer[] = this.teamB.team;
        if (this.currentPlayingTeam === PlayingTeam.teamA) {
            
            guessers = this.teamB.team;
            viewers = this.teamA.team;
        }
        
        this.sendToUsers(io, guessers, "reply", {
            roundDuration: this.replyTime / 1000,
            isGuessing: true
        });
        this.sendToUsers(io, viewers, "reply", {
            roundDuration: this.replyTime / 1000,
            isGuessing: false
        });

        this.setRoundTimeout(() => {
            this.nextRound(io, socket, { guessed: false, guesser: "", message: "the word to guess was: " + this.games[this.currentRound].word} );
        }, this.replyTime);
    }

    nextRound(io: SocketIO.Server, socket: SocketIO.Socket, roundEndMessage: RoundEndInfo) {
        this.stopGameEvents();
        
        this.replyState = false;
        io.sockets.in(this.id).emit("roundEnd", { message: roundEndMessage.message, teamA: this.teamA.points, teamB: this.teamB.points } );

        super.nextRound(io ,socket, roundEndMessage);
        setTimeout(() => {
            this.currentPlayingTeam = this.currentPlayingTeam === PlayingTeam.teamA ?
                                        PlayingTeam.teamB : PlayingTeam.teamA;
            
            this.currentRound++;
            if (this.currentRound >= this.nbRounds)
                this.endMatch(io, socket);
            else
                this.startRound(io, socket);
        }, 3000);
    }

    async endMatch(io: SocketIO.Server, socket: SocketIO.Socket, playerLeft?: string) {
        super.endMatch(io ,socket);
        
        let winnerTeam: PlayingTeam = this.getWinningTeam(playerLeft);

        const matchDuration = (new Date().getTime() - this.startingTime.getTime())/1000;
        console.log("le match a duree :" + matchDuration);
        this.startingTime = null;

        await this.statsService.updateNbGame(this.teamA, this.teamB);
        await this.statsService.updateWinRate(this.teamA, this.teamB, winnerTeam);
        await this.statsService.updateAverageMatchesTime(this.teamA, this.teamB, matchDuration);
        await this.statsService.updateTotalMatchesTime(this.teamA, this.teamB, matchDuration);
        this.notifyWinnersAndLoosers(io, socket, winnerTeam, playerLeft);
    }

    getWinningTeam(playerLeft?: string): PlayingTeam {
        if (playerLeft) {
            if (this.teamA.team.find((player: HPlayer) => player.username === playerLeft))
                return PlayingTeam.teamB
            else
                return PlayingTeam.teamA
        } else {
            if (this.teamB.points > this.teamA.points)
                return PlayingTeam.teamB
            else if (this.teamA.points > this.teamB.points)
                return PlayingTeam.teamA
            else
                return PlayingTeam.noTeam
        }   
    }

    notifyWinnersAndLoosers(io: SocketIO.Server, socket: SocketIO.Socket, winningTeam: PlayingTeam, playerLeft?: string) {
        let winners: TeamInfos = this.teamA;
        let loosers: TeamInfos = this.teamB;

        if (winningTeam === PlayingTeam.noTeam){
            this.notfifyTiedMatch(io, socket);
            return;
        } else if (winningTeam === PlayingTeam.teamB) {
            winners = this.teamB;
            loosers = this.teamA;
        }

        let additionalReasonWinner: string = !playerLeft ? `The final score is ${winners.points}-${loosers.points} in favor of ${winners.teamName}` : " An opponent has left the match";
        let additionalReasonLoser: string = !playerLeft ? `The final score is ${winners.points}-${loosers.points} in favor of ${winners.teamName}` : " Your teammate has left the match";

        for (let i = 0; i < winners.team.length; ++i) {
            const username = winners.team[i].username;
            io.to(userSocketMap.getSocketId(username)).emit("matchEnd", {
                wins: true,
                reason: "Victory!" + additionalReasonWinner,
            });
        }
        for (let j = 0; j < loosers.team.length; ++j) {
            const username = loosers.team[j].username;
            if (username !== playerLeft)
                io.to(userSocketMap.getSocketId(username)).emit("matchEnd", {
                    wins: false,
                    reason: "Defeat!" + additionalReasonLoser,
                });
        }
    }

    notfifyTiedMatch(io: SocketIO.Server, socket: SocketIO.Socket) {
        let allPlayers: HPlayer[] = this.getPlayers();
        for (let j = 0; j < allPlayers.length; ++j) {
            const username = allPlayers[j].username;
            io.to(userSocketMap.getSocketId(username)).emit("matchEnd", {
                wins: false,
                reason: `Tied! Final score is ${this.teamA.points}-${this.teamB.points}`
            });
        }
    }

    guess(io: SocketIO.Server, socket: SocketIO.Socket, guess: string): void {
        console.log("guessed: " + guess + ` (word is ${guess})`);
        
        if (guess === this.games[this.currentRound].word) {
            this.guessers.push(userSocketMap.getUsernameBySocket(socket.id));
            this.stopGameEvents();
            let winnerTeam: TeamInfos;
            if(this.replyState) 
                winnerTeam = this.currentPlayingTeam === PlayingTeam.teamA ? this.teamB : this.teamA; 
            else 
                winnerTeam = this.currentPlayingTeam === PlayingTeam.teamA ? this.teamA : this.teamB; 

            winnerTeam.points++;
            this.nextRound(io, socket, { guessed: true, guesser: userSocketMap.getUsernameBySocket(socket.id),
                                         message: winnerTeam.teamName + " has guessed the word"});
        } else {
            if(this.replyState) {
                this.nextRound(io, socket, {guessed: true, guesser: userSocketMap.getUsernameBySocket(socket.id),
                                            message: "the word to guess was: " + this.games[this.currentRound].word});
            } else {
                let currentTeam: TeamInfos = this.currentPlayingTeam === PlayingTeam.teamA ?
                                        this.teamA : this.teamB; 
                currentTeam.tries--;    
                if(currentTeam.tries <= 0)
                    this.reply(io ,socket);
                else 
                    io.sockets.in(this.id).emit("badGuess", { triesLeft: currentTeam.tries} );
            }
        }
    }
    
    userJoin(io: SocketIO.Server, socket: SocketIO.Socket, request: any): boolean {
        let smallerTeam: IPlayer[] = this.teamA.team.length < this.teamB.team.length ? this.teamA.team : this.teamB.team;
        let biggerTeam: IPlayer[] = this.teamA.team.length >= this.teamB.team.length ? this.teamA.team : this.teamB.team;

        return (this.joinTeam(smallerTeam, request.username) || this.joinTeam(biggerTeam, request.username));
    }

    private joinTeam(team: IPlayer[], username: string): boolean {
        if (team.length < 2){
            team.push(new HPlayer(username));
            return true;
        }
        
        return false;
    }

    userLeave(io: SocketIO.Server, socket: SocketIO.Socket, request: any): void {
        this.teamA.team = this.teamA.team.filter((value: IPlayer) => { return value.username != request.username} );
        this.teamB.team = this.teamB.team.filter((value: IPlayer) => { return value.username != request.username} );
    }

    toJSON(matchmakingJSON: boolean = false) {
        let baseJSON: any = this.baseJSON();
        baseJSON.scoreA = this.teamA.points;
        baseJSON.scoreB = this.teamB.points;
        baseJSON.teamA = this.teamA.team.map((player: IPlayer) => player.toJSON(matchmakingJSON));
        baseJSON.teamB = this.teamB.team.map((player: IPlayer) => player.toJSON(matchmakingJSON));
        return baseJSON;
    }

    getPlayers(): HPlayer[] {
        let players: HPlayer[] = [];
        
        this.teamA.team.forEach((player: IPlayer) => { 
            if (player.isHuman())
                players.push(player);
        });
        this.teamB.team.forEach((player: IPlayer) => { 
            if (player.isHuman())
                players.push(player);
        });
        return players;
    }

    getAiPlayers(): AIPlayer[] {
        let players: AIPlayer[] = [];
        
        this.teamA.team.forEach((player: IPlayer) => { 
            if (!player.isHuman())
                players.push(player as AIPlayer);
        });
        this.teamB.team.forEach((player: IPlayer) => { 
            if (!player.isHuman())
                players.push(player as AIPlayer);
        });
        return players;
    }

    editMatch(io: SocketIO.Server, socket: SocketIO.Socket, request: any): void {
        switch(request.action) {
            case MatchEditActions.Ready:
                {
                    let username: string = request.options;
                    this.teamA.team.forEach((player: IPlayer) => { if (player.username === username) player.ready = !player.ready });
                    this.teamB.team.forEach((player: IPlayer) => { if (player.username === username) player.ready = !player.ready });
                }
            break;
            case MatchEditActions.AddAI:
                if(request.options === PlayingTeam.teamA)
                    this.teamA.team.push(new AIPlayer(this.type, this.id, PlayingTeam.teamA, this.getAiPlayers()));
                else
                    this.teamB.team.push(new AIPlayer(this.type, this.id,  PlayingTeam.teamA, this.getAiPlayers()));
            break;
            case MatchEditActions.RemoveAI: 
                if(request.options === PlayingTeam.teamA)
                    this.teamA.team = this.teamA.team.filter((value: IPlayer) => value.isHuman());
                else
                    this.teamB.team = this.teamB.team.filter((value: IPlayer) => value.isHuman());
            break;
            case MatchEditActions.ChangeAI: 
                //TBM
            break;
            case MatchEditActions.SwitchTeam: 
                let playerIndex: number;
                if (this.teamA.team.some((value: IPlayer, index: number) => {playerIndex = index; return value.username === request.options})) {
                    this.teamB.team.push(this.teamA.team[playerIndex]);
                    this.teamA.team.splice(playerIndex, 1);
                }
                else if (this.teamB.team.some((value: IPlayer, index: number) => {playerIndex = index; return value.username === request.options})) {
                    this.teamA.team.push(this.teamB.team[playerIndex]);
                    this.teamB.team.splice(playerIndex, 1);
                }

            break;
        }
        
    }

}