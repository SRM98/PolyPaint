import { TeamInfos } from "../../models/match.model";
import { accountDB } from "../../database/accountDB";
import Stats from "../../models/stats.model"

export class CoopStatsService {

    public constructor(){
    }

    public async updateNbGame(players: TeamInfos): Promise<void> {

        for (let i = 0; i < players.team.length; i++) {
            if (players.team[i].isHuman()) {
                let stats: Stats = (await accountDB.getStats((players.team[i].username))).stats
                await accountDB.updateNbGames(players.team[i].username, stats.nbGames + 1);
                await accountDB.updateNbGamesCoop(players.team[i].username, stats.nbGamesCoop + 1);
            }
        }
    }

    public async updateAverageMatchesTime(players: TeamInfos, matchDuration: number): Promise<void> {

        for (let i = 0; i < players.team.length; i++) {
            if (players.team[i].isHuman()) {
                let stats: Stats = (await accountDB.getStats((players.team[i].username))).stats
                await accountDB.updateAverageMatchesTime(players.team[i].username, this.calculeAverageGameTime(stats.nbGames, matchDuration, 
                                                                                                           stats.averageMatchesTime));
            }
        }
    }

    public async updateTotalMatchesTime(players: TeamInfos, matchDuration: number): Promise<void> {

        for (let i = 0; i < players.team.length; i++) {
            if (players.team[i].isHuman()) {
                let stats: Stats = (await accountDB.getStats((players.team[i].username))).stats
                await accountDB.updateTotalMatchesTime(players.team[i].username, stats.totalMatchesTime + matchDuration);
            }
        }
    }

    public calculeAverageGameTime(nbGamePlayed: number, matchDuration: number, currentAverage: number) {
        return ((currentAverage*(nbGamePlayed-1)) + (matchDuration))/nbGamePlayed;
    }
}