using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PolyPaint.Modeles
{
    class Stats
    {
        public int nbGames { get; set; }
        public int nbVictories { get; set; }
        public double victoryPercentage { get; set; }
        public double averageMatchesTime { get; set; }
        public double totalMatchesTime { get; set; }

        public Stats(int nbGames, int nbVictories, double victoryPercentage, double averageMatchesTime, double totalMatchesTime)
        {
            this.nbGames = nbGames;
            this.nbVictories = nbVictories;
            this.victoryPercentage = victoryPercentage;
            this.averageMatchesTime = averageMatchesTime;
            this.totalMatchesTime = totalMatchesTime;
        }
    }
}
