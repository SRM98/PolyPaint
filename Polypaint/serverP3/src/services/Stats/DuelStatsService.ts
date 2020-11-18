import { TeamInfos } from "../../models/match.model";
import { accountDB } from "../../database/accountDB";
import Stats from "../../models/stats.model"

export class DuelStatsService {

    public constructor(){
    }

    public async updateNbGame(winner: string, loser: string): Promise<void> {

        let winnerStats: Stats = (await accountDB.getStats((winner))).stats
        await accountDB.updateNbGames(winner, winnerStats.nbGames + 1);

        let loserStats: Stats = (await accountDB.getStats((loser))).stats
        await accountDB.updateNbGames(loser, loserStats.nbGames + 1);
    }

    public async updateWinRate(winner: string, loser: string): Promise<void> {

        let winnerStats: Stats = (await accountDB.getStats((winner))).stats
        winnerStats.nbVictories += 1;
        await accountDB.updateNbVictories(winner, winnerStats.nbVictories);
        await accountDB.updateWinRate(winner, this.calculeWinRate(winnerStats));
        
        let loserStats: Stats = (await accountDB.getStats((loser))).stats
        await accountDB.updateWinRate(loser, this.calculeWinRate(loserStats));
    }

    public async updateAverageMatchesTime(winner: string, loser: string, matchDuration: number): Promise<void> {

        let winnerStats: Stats = (await accountDB.getStats((winner))).stats;
        await accountDB.updateAverageMatchesTime(winner, this.calculeAverageGameTime(winnerStats.nbGames, matchDuration, 
                                                                                    winnerStats.averageMatchesTime));

        let loserStats: Stats = (await accountDB.getStats((loser))).stats;
        await accountDB.updateAverageMatchesTime(loser, this.calculeAverageGameTime(loserStats.nbGames, matchDuration, 
                                                                                    loserStats.averageMatchesTime));
    }

    public async updateTotalMatchesTime(winner: string, loser: string, matchDuration: number): Promise<void> {

        let winnerStats: Stats = (await accountDB.getStats((winner))).stats;
        await accountDB.updateTotalMatchesTime(winner, winnerStats.totalMatchesTime + matchDuration);

        let loserStats: Stats = (await accountDB.getStats((loser))).stats;
        await accountDB.updateTotalMatchesTime(loser, loserStats.totalMatchesTime + matchDuration);
    }

    

    public calculeWinRate(stats: Stats) {
        return (stats.nbVictories/(stats.nbGames - stats.nbGamesCoop - stats.nbGamesSolo))*100;
    }


    public calculeAverageGameTime(nbGamePlayed: number, matchDuration: number, currentAverage: number) {
        return ((currentAverage*(nbGamePlayed-1)) + (matchDuration))/nbGamePlayed;
    }
}