import { TeamInfos, PlayingTeam } from "../../models/match.model";
import { accountDB } from "../../database/accountDB";
import Stats from "../../models/stats.model"
import { IPlayer } from "../players/Player";

export class ClassicStatsService {

    public constructor(){
    }

    public async updateNbGame(teamA: TeamInfos, teamB: TeamInfos): Promise<void> {

        for (let i = 0; i < teamA.team.length; i++) {
            console.log(teamA.team[i].isHuman(), teamA.team[i].username)
            if (teamA.team[i].isHuman()) {
                let stats: Stats = (await accountDB.getStats((teamA.team[i].username))).stats
                await accountDB.updateNbGames(teamA.team[i].username, stats.nbGames + 1);
            }
        }

        for (let i = 0; i < teamB.team.length; i++) {
            console.log(teamB.team[i].isHuman(), (teamB.team[i].username))
            if (teamB.team[i].isHuman()) {
                let stats: Stats = (await accountDB.getStats((teamB.team[i].username))).stats
                await accountDB.updateNbGames(teamB.team[i].username, stats.nbGames + 1);
            }
        }
    }

    public async updateWinRate(teamA: TeamInfos, teamB: TeamInfos, winningTeam: PlayingTeam): Promise<void> {

        if (winningTeam === PlayingTeam.teamA) {
            for (let i = 0; i < teamA.team.length; i++) {
                if (teamA.team[i].isHuman()) {
                    let stats: Stats = (await accountDB.getStats((teamA.team[i].username))).stats
                    stats.nbVictories += 1;
                    await accountDB.updateNbVictories(teamA.team[i].username, stats.nbVictories);
                    await accountDB.updateWinRate(teamA.team[i].username, this.calculeWinRate(stats));
                }
            }

            for (let i = 0; i < teamB.team.length; i++) {
                if (teamB.team[i].isHuman()) {
                    let stats: Stats = (await accountDB.getStats((teamB.team[i].username))).stats
                    await accountDB.updateWinRate(teamB.team[i].username, this.calculeWinRate(stats));
                }
            }
        } else if (winningTeam === PlayingTeam.teamB) {
            for (let i = 0; i < teamB.team.length; i++) {
                if (teamB.team[i].isHuman()) {
                    let stats: Stats = (await accountDB.getStats((teamB.team[i].username))).stats
                    stats.nbVictories += 1;
                    await accountDB.updateNbVictories(teamB.team[i].username, stats.nbVictories);
                    await accountDB.updateWinRate(teamB.team[i].username, this.calculeWinRate(stats));
                }
            }

            for (let i = 0; i < teamA.team.length; i++) {
                if (teamA.team[i].isHuman()) {
                    let stats: Stats = (await accountDB.getStats((teamA.team[i].username))).stats
                    await accountDB.updateWinRate(teamA.team[i].username, this.calculeWinRate(stats));
                }
            }
        }
    }

    public async updateAverageMatchesTime(teamA: TeamInfos, teamB: TeamInfos, matchDuration: number): Promise<void> {
        for (let i = 0; i < teamA.team.length; i++) {
            if (teamA.team[i].isHuman()) {
                let stats: Stats = (await accountDB.getStats((teamA.team[i].username))).stats
                await accountDB.updateAverageMatchesTime(teamA.team[i].username, this.calculeAverageGameTime(stats.nbGames, matchDuration, 
                                                                                                           stats.averageMatchesTime));
            }
        }

        for (let i = 0; i < teamB.team.length; i++) {
            if (teamB.team[i].isHuman()) {
                let stats: Stats = (await accountDB.getStats((teamB.team[i].username))).stats
                await accountDB.updateAverageMatchesTime(teamB.team[i].username, this.calculeAverageGameTime(stats.nbGames, matchDuration, 
                                                                                                             stats.averageMatchesTime));
            }
        }
    }

    public async updateTotalMatchesTime(teamA: TeamInfos, teamB: TeamInfos, matchDuration: number): Promise<void> {
        for (let i = 0; i < teamA.team.length; i++) {
            if (teamA.team[i].isHuman()) {
                let stats: Stats = (await accountDB.getStats((teamA.team[i].username))).stats
                await accountDB.updateTotalMatchesTime(teamA.team[i].username, stats.totalMatchesTime + matchDuration);
            }
        }

        for (let i = 0; i < teamB.team.length; i++) {
            if (teamB.team[i].isHuman()) {
                let stats: Stats = (await accountDB.getStats((teamB.team[i].username))).stats
                await accountDB.updateTotalMatchesTime(teamB.team[i].username, stats.totalMatchesTime + matchDuration);
            }
        }
    }

    public calculeWinRate(stats: Stats) {
        return (stats.nbVictories/(stats.nbGames - stats.nbGamesCoop - stats.nbGamesSolo))*100;
    }

    public calculeAverageGameTime(nbGamePlayed: number, matchDuration: number, currentAverage: number) {
        return ((currentAverage*(nbGamePlayed-1)) + (matchDuration))/nbGamePlayed;
    }
}