import { IPlayer } from "../services/players/Player";
import { gameDB } from "../database/gameDB";

export class IMatch {
    id: string;
    creator: string;
    name: string;
    nbRounds: number;
    type: MatchTypes;
    maxTime: number;
    constructor(match: IMatch) {
        this.name = match.name;
        this.nbRounds = match.nbRounds;
        this.type = match.type;
        this.creator = match.creator;
        this.id = match.name + match.type;
        
        switch(this.type) {
            case MatchTypes.Classic: this.maxTime = 900; break; 
            case MatchTypes.Coop: this.nbRounds = gameDB.getNbGames() > 15 ? 15 : gameDB.getNbGames();
                                  this.maxTime = 120; break; 
            case MatchTypes.Duel: this.maxTime = 900; break; 
            case MatchTypes.Solo: this.nbRounds = gameDB.getNbGames() > 15 ? 15 : gameDB.getNbGames();
                                  this.maxTime = 120; break; 
        }
    }
}

export enum MatchTypes{
    Classic,
    Coop,
    Duel,
    Solo,
}

export enum MatchEditActions{
    AddAI,
    RemoveAI,
    ChangeAI,
    SwitchTeam,
    Ready,
}

export enum PlayingTeam {
    teamA,
    teamB,
    noTeam,
}

export interface TeamInfos {
    teamName: string;
    team: IPlayer[];
    playerIndex: number;
    tries: number;
    points: number;
}