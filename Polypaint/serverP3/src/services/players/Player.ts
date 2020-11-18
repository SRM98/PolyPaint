import { MatchTypes, PlayingTeam } from "../../models/match.model";
import { chatService, ChatService } from "../chat/chatservice";
import Message from "../../models/chat.model";
import { matchHistoryDB } from "../../database/matchHistoryDB";
import { accountDB, PlayerWinRate } from "../../database/accountDB";
import { resolve } from "dns";
import { rejects } from "assert";

export abstract class IPlayer {
    username: string;
    abstract isHuman(): boolean;
    ready: boolean;

    toJSON(matchmakingJSON: boolean = false): any {
        if (matchmakingJSON)
            return { username: this.username, ready: this.ready};
        return this.username;
    }
}

export class HPlayer implements IPlayer{
    ready: boolean;
    constructor(public username: string){}
    
    isHuman(): boolean {
        return true;
    }
    
    toJSON(matchmakingJSON: boolean = false): any {
        if (matchmakingJSON)
            return { username: this.username, ready: this.ready};
        return this.username;
    }
}

export class AIPersonnality {
    private matchStartQuerier: Array<Function>;
    private roundEndQuerier: Array<Function>;
    private matchEndQuerier: Array<Function>;
    private type: MatchTypes = 0;
    private aiTeam: PlayingTeam;

    constructor( public username: string, public url: string ,public matchStart: Map<string, Function>, public roundEnd: Map<string, Function>, 
                 public matchEnd: Map<string, Function>, public hint: Function) {
        this.matchStartQuerier= [this.msReferencePlayer(), this.msFindGoodPlayer(), this.msFindBadPlayer()];
    }

    setType(type: MatchTypes, team: PlayingTeam): void {
        this.aiTeam = team;
        this.type = type;
        this.roundEndQuerier = [this.reDefault()];
        this.matchEndQuerier = [this.meDefault()];
        switch (type) {
            case MatchTypes.Solo: this.roundEndQuerier.push(this.reTellTimeLeft());
            break;
            case MatchTypes.Coop: this.roundEndQuerier.push(this.reTellTimeLeft(), this.reTellBadPlayer(), this.reTellPlayerOnFire());
            break;
            case MatchTypes.Classic: this.matchEndQuerier.push(this.meWinMatch(), this.meLostMatch());
            case MatchTypes.Duel: this.roundEndQuerier.push(this.reTellBadPlayer(), this.reTellPlayerOnFire(), this.reTellRoundLeft());
            break;
        }
    }

    async randomStartComment(myTeam: HPlayer[], otherTeam: HPlayer[]): Promise<string> {
        let possibleIndexes: number[] = [...Array(this.matchStartQuerier.length).keys()];
        while(possibleIndexes.length > 0) {
            let randIndex: number = possibleIndexes[Math.floor(Math.random()*(possibleIndexes.length-1))];
            
            let randMessage = await this.matchStartQuerier[randIndex](myTeam, otherTeam);
            
            if (randMessage != "")
                return new Promise<string>( (resolve) => resolve(randMessage) );
            possibleIndexes.splice(possibleIndexes.indexOf(randIndex), 1);
        }
        return new Promise<string>( (resolve) => resolve(this.matchStart.get("default")()) );
    }

    msReferencePlayer() {
        return async (myTeam: HPlayer[], otherTeam: HPlayer[]): Promise<string> => {
            let players = [ ...myTeam, ...otherTeam].map((value: HPlayer) => value.username);
            
            return matchHistoryDB.referencePlayers(this.username, players)
                .then((foundPlayer: string) => {
                    
                    if ( foundPlayer === "")
                        return "";
                    return this.matchStart.get("referencePlayer")(foundPlayer);
                });
        }
    }

    msFindBadPlayer() {
        return async (myTeam: HPlayer[], otherTeam: HPlayer[]): Promise<string> => {
            let players = [ ...myTeam, ...otherTeam].map((value: HPlayer) => value.username);

            return accountDB.findBadPlayer(players).then((foundPlayer: PlayerWinRate) => {
                
                if ( foundPlayer.username === "")
                    return "";
                return this.matchStart.get("badPlayer")(foundPlayer);
            });

        }
    }

    msFindGoodPlayer() {
        return (myTeam: HPlayer[], otherTeam: HPlayer[]) => {
            let players = [ ...myTeam, ...otherTeam].map((value: HPlayer) => value.username);
            
            return accountDB.findGoodPlayer(players).then((foundPlayer: PlayerWinRate) => {
                if ( foundPlayer.username === "")
                    return "";
                return this.matchStart.get("goodPlayer")(foundPlayer);
            });

        }
    }

    randomRoundEndComment(myTeam: HPlayer[], otherTeam: HPlayer[], roundEndInfo: any, matchStateInfo: any): string {
        let possibleIndexes: number[] = [...Array(this.roundEndQuerier.length).keys()];
        while(possibleIndexes.length > 0) {
            let randIndex: number = possibleIndexes[Math.floor(Math.random()*(possibleIndexes.length-1))];
            
            let randMessage = this.roundEndQuerier[randIndex](myTeam, otherTeam,  roundEndInfo, matchStateInfo);
            if (randMessage != "")
                return randMessage;
            possibleIndexes.splice(possibleIndexes.indexOf(randIndex), 1);
        }
        return this.roundEnd.get("default")();
    }

    reDefault() {
        return (myTeam: HPlayer[], otherTeam: HPlayer[], roundEndInfo: any, matchStateInfo: any) => {
            return this.roundEnd.get("default" + Math.floor(Math.random()*4))();
        }
    }

    reTellRoundLeft() {
        return (myTeam: HPlayer[], otherTeam: HPlayer[], roundEndInfo: any, matchStateInfo: any) => {
            let roundsLeft = matchStateInfo.nbRounds - matchStateInfo.currentRound; 
            if (roundsLeft < matchStateInfo.nbRounds/2)
                return this.roundEnd.get("roundLeft")(roundsLeft);
            return "";
        }
    }

    reTellTimeLeft() {
        return (myTeam: HPlayer[], otherTeam: HPlayer[], roundEndInfo: any, matchStateInfo: any) => {
            if (matchStateInfo.timeLeft < 30)
                return this.roundEnd.get("timeLeft")(matchStateInfo.timeLeft);
            return "";
        }
    }

    reTellPlayerOnFire() {
        return (myTeam: HPlayer[], otherTeam: HPlayer[], roundEndInfo: any, matchStateInfo: any) => {

            let players: string[] = [ ...myTeam, ...otherTeam].map((player: HPlayer) => player.username);
            let last5Guessers = this.lastElements(matchStateInfo.guessers, 5);
            
            for (let player of players) {
                if (roundEndInfo.guesser === player && this.count(last5Guessers, player) >= 2)
                    return this.roundEnd.get("playeOnFire")(player);
            }
            return "";
        } 
        
    }

    reTellBadPlayer() {
        return (myTeam: HPlayer[], otherTeam: HPlayer[], roundEndInfo: any, matchStateInfo: any) => {
            
            let players: string[] = [ ...myTeam, ...otherTeam].map((player: HPlayer) => player.username);
            let last5Guessers = this.lastElements(matchStateInfo.guessers, 5);

            for (let player of players) {
                if (roundEndInfo.guesser === player && this.count(last5Guessers, player) === 0)
                    return this.roundEnd.get("noobPlayer")(player);
            }

            return "";
        }
        
    }

    private count(array: any[], element: any) {
        return array.filter((value: string) => value === element).length;
    }

    private lastElements(array: any[], nElements: number) {
        if (array.length < nElements)
            return array;
        return array.slice(array.length - nElements);
    }

    randomMatchEndComment(myTeam: HPlayer[], otherTeam: HPlayer[], matchStateInfo: any): string {
        let possibleIndexes: number[] = [...Array(this.matchEndQuerier.length).keys()];
        while(possibleIndexes.length > 0) {
            let randIndex: number = possibleIndexes[Math.floor(Math.random()*(possibleIndexes.length-1))];
            
            let randMessage = this.matchEndQuerier[randIndex](myTeam, otherTeam, matchStateInfo);
            if (randMessage != "")
                return randMessage;
            possibleIndexes.splice(possibleIndexes.indexOf(randIndex), 1);
        }
        return this.matchEnd.get("default")();
    }

    meLostMatch() {
        return (myTeam: HPlayer[], otherTeam: HPlayer[], matchStateInfo: any) => {
            if (matchStateInfo.winningTeam !== this.aiTeam && this.aiTeam !== PlayingTeam.noTeam)
                return this.matchEnd.get("lostMatch")();
            return "";
        }
    }

    meWinMatch() {
        return (myTeam: HPlayer[], otherTeam: HPlayer[], matchStateInfo: any) => {
            if (matchStateInfo.winningTeam === this.aiTeam)
                return this.matchEnd.get("wonMatch")();
            return "";
        }
    }

    meDefault() {
        return (myTeam: HPlayer[], otherTeam: HPlayer[], matchStateInfo: any) => {
            return this.matchEnd.get("default")();
        } 
    }
    
}


export let ai: Array<AIPersonnality> = [
    new AIPersonnality( "Bob (Ai Player)" , "imagesStatic/bob.jpg",
      new Map<string, Function>( [
            ["referencePlayer", (player: string) => `Hey ${player}, i'm happy to see you again`],
            ["badPlayer", (playerWinRate: PlayerWinRate) => `holly macaroni ${playerWinRate.username}, you can build a better winrate than ${playerWinRate.winrate.toFixed(2)}%`],
            ["goodPlayer", (playerWinRate: PlayerWinRate) => `${playerWinRate.username}'s winrate (${playerWinRate.winrate.toFixed(2)}%) is pretty good, hammer on!`],
            ["default", () => `Well, let's get building!`],
        ]),
     new Map<string, Function>( [
        ["roundLeft", (roundLeft: number) => `Come on guys! only ${roundLeft} rounds left`],
        ["timeLeft", (timeLeft: number) => `Faster! just ${timeLeft} seconds left!`],
        ["playeOnFire", (player: string) => `${player}, you're on fire! Let me get that firetruck`],
        ["noobPlayer", (player: string) => `i'm sure you can do better ${player}!`],
        ["default0", () => `Good round!`],
        ["default1", () => `Don't give up`],
        ["default2", () => `To the next round!`],
        ["default3", () => `Good job everybody`],
      ]),
      new Map<string, Function>( [
        ["lostMatch", () => `Shoots, we lost. We will do better next time!`],
        ["wonMatch", () => `Yay we won, friendship can get us really far!`],
        ["default", () => `Well that was a busy day! Let's get back home`],
      ]), 
      (hintInfo: any) => `${hintInfo.username}, there is no shame in asking a hint: ${hintInfo.hint}` ),
      new AIPersonnality( "Donald (Ai Player)" , "imagesStatic/donaldTrump.jpg",
      new Map<string, Function>( [
            ["referencePlayer", (player: string) => `${player}, I've seen you before, it seems you have'nt been deported yet`],
            ["badPlayer", (playerWinRate: PlayerWinRate) => `hey ${playerWinRate.username} your winrate, ${playerWinRate.winrate.toFixed(2)}%, is worse than Hillary's approval`],
            ["goodPlayer", (playerWinRate: PlayerWinRate) => `${playerWinRate.username}, me and your ${playerWinRate.winrate.toFixed(2)}% winrate are going to make Lascaux great again`],
            ["default", () => `Lets make america great again!`],
        ]),
     new Map<string, Function>( [
        ["roundLeft", (roundLeft: number) => `Only ${roundLeft} rounds left Let's win`],
        ["timeLeft", (timeLeft: number) => ` ${timeLeft} seconds left!`],
        ["playeOnFire", (player: string) => `${player}, you're pretty good, nobody has better respect for intelligent players than me!`],
        ["noobPlayer", (player: string) => `Hey ${player}, start winning, do like me`],
        ["default0", () => `You're doing a great job guys, really great job`],
        ["default1", () => `Vote for me! also, good job.`],
        ["default2", () => `Onto the next round...`],
        ["default3", () => `No russian participated in this round, None.`],
      ]),
      new Map<string, Function>( [
        ["lostMatch", () => `Im always winning`],
        ["wonMatch", () => `We lost.. I blame it on hillary`],
        ["default", () => `it was a good game, a great game, just the best`],
      ]), 
      (hintInfo: any) => `${hintInfo.username}, since you asked... : ${hintInfo.hint}` ),
      new AIPersonnality( "Darth Jar Binks (Ai Player)" , "imagesStatic/DarthJarBinks.png",
      new Map<string, Function>( [
            ["referencePlayer", (player: string) => `${player}, We meet again!`],
            ["badPlayer", (playerWinRate: PlayerWinRate) => `Come on ${playerWinRate.username}, with this terrible ${playerWinRate.winrate.toFixed(2)}% win rate, try at least to be funny!`],
            ["goodPlayer", (playerWinRate: PlayerWinRate) => `Wow ${playerWinRate.username}, your ${playerWinRate.winrate.toFixed(2)}% winrate makes me feel really weak!`],
            ["default", () => `Heya Missa on Dark Sida now!`],
        ]),
     new Map<string, Function>( [
        ["roundLeft", (roundLeft: number) => `Only ${roundLeft} rounds left Let's win`],
        ["timeLeft", (timeLeft: number) => `Hey, ${timeLeft} seconds left! We need to hurry.`],
        ["playeOnFire", (player: string) => `${player}, great kid you're amazing. Don't get cocky!`],
        ["noobPlayer", (player: string) => `Hey ${player}, weakness disgust me`],
        ["default0", () => `All too easy.`],
        ["default1", () => `Good job! Anakin would be proud.`],
        ["default2", () => `Another round. You will find that it is you who are mistaken… about a great many things.`],
        ["default3", () => `That round is our last hope.`],
      ]),
      new Map<string, Function>( [
        ["lostMatch", () => `Uhh! We'll be back stronger next time.`],
        ["wonMatch", () => `Only at the end do you realize the power of the Dark Side.`],
        ["default", () => `Everything is proceeding as I have foreseen.`],
      ]), 
      (hintInfo: any) => `${hintInfo.username}, since you asked... : ${hintInfo.hint}` )
]

export class AIPlayer implements IPlayer{
    ready: boolean = true;
    public username: string;
    public personnality: AIPersonnality;
    public myTeam: HPlayer[];
    public otherTeam: HPlayer[] = [];

    constructor(public type: MatchTypes, public matchId: string, public team: PlayingTeam = PlayingTeam.noTeam, notIn: AIPlayer[] = []) {
        do {
            let randIndex: number = Math.floor(Math.random()*ai.length);
            this.username = ai[randIndex].username;
            this.personnality = ai[randIndex];
            this.personnality.setType(type, team);
        } while(this.contains(this.username, notIn))
    }

    private contains(aiName: string, others: AIPlayer[]): boolean {
        return others.map((player: AIPlayer) => player.username ).includes(aiName);
    }

    toJSON(matchmakingJSON: boolean = false): any {
        if (matchmakingJSON)
            return { username: this.username, ready: this.ready};
        return this.username;
    }

    init(teamA: IPlayer[], teamB: IPlayer[] = []): void {
        if (this.type == MatchTypes.Classic) {
            if (teamA.find((value: IPlayer) => value.username == this.username)) {
                this.myTeam = teamA.filter((value: IPlayer) => value.isHuman());
                this.otherTeam = teamB.filter((value: IPlayer) => value.isHuman());;
            }
            else {
                this.myTeam = teamB.filter((value: IPlayer) => value.isHuman());;
                this.otherTeam = teamA.filter((value: IPlayer) => value.isHuman());;
            }

        } else {
            this.myTeam = teamA;
        }
    }

    startMatchMessage(io: SocketIO.Server, socket: SocketIO.Socket): void {
        this.personnality.randomStartComment(this.myTeam, this.otherTeam).then((text) => {
            if (text)
                chatService.incommingMessage(io,socket, {room: this.matchId, sender: this.username, text: text} as Message);
        });
    }

    endMatchMessage(io: SocketIO.Server, socket: SocketIO.Socket, matchStateInfo: any): void {
        let text: string = this.personnality.randomMatchEndComment(this.myTeam, this.otherTeam, matchStateInfo);
        if (text)
            chatService.incommingMessage(io,socket, {room: this.matchId, sender: this.username, text: text} as Message);
    }

    nextRoundMessage(io: SocketIO.Server, socket: SocketIO.Socket, roundEndInfo: any, matchStateInfo: any): void {
        let text: string = this.personnality.randomRoundEndComment(this.myTeam, this.otherTeam, roundEndInfo, matchStateInfo);
        if (text)
            chatService.incommingMessage(io,socket, {room: this.matchId, sender: this.username, text: text} as Message);
    }

    hintMessage(io: SocketIO.Server, socket: SocketIO.Socket, hintInfo: any): void {
        let text: string = this.personnality.hint(hintInfo);
        if (text)
            chatService.incommingMessage(io,socket, {room: this.matchId, sender: this.username, text: text} as Message);
    }

    isHuman(): boolean {
        return false;
    }
}
