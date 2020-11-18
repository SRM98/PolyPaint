import { TeamInfos } from "../../models/match.model";
import { accountDB } from "../../database/accountDB";
import Stats from "../../models/stats.model"

export class SoloStatsService {

    public constructor(){
    }

    public async updateNbGame(player: string): Promise<void> {

        let stats: Stats = (await accountDB.getStats((player))).stats
        await accountDB.updateNbGames(player, stats.nbGames + 1);
        await accountDB.updateNbGamesSolo(player, stats.nbGamesSolo + 1);

    }

    public async updateAverageMatchesTime(player: string, matchDuration: number): Promise<void> {

        let stats: Stats = (await accountDB.getStats((player))).stats
        await accountDB.updateAverageMatchesTime(player, this.calculeAverageGameTime(stats.nbGames, matchDuration, 
                                                                                                    stats.averageMatchesTime));
    }

    public async updateTotalMatchesTime(player: string, matchDuration: number): Promise<void> {

        let stats: Stats = (await accountDB.getStats((player))).stats
        await accountDB.updateTotalMatchesTime(player, stats.totalMatchesTime + matchDuration);
        
    }

    public calculeAverageGameTime(nbGamePlayed: number, matchDuration: number, currentAverage: number) {
        return ((currentAverage*(nbGamePlayed-1)) + (matchDuration))/nbGamePlayed;
    }
}