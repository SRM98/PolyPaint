using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PolyPaint.Modeles
{
    public enum Modes
    {
        Classic,
        Coop,
        Duel,
        Solo,
    }

    public class IModeInfo
    {
        public string id { get; set; } = "";
        public string creator { get; set; } = "";
        public string name { get; set; } = "";
        public int nbRounds { get; set; } = 0;
        public int maxTime { get; set; } = 0;
        public int playerCount { get; set; } = 1;
        public int placesLeft { get; set; }
        public Modes type { get; set; } = 0;
    }
    
    public class ClassicInfo : IModeInfo
    {
        public string[] teamA { get; set; } = {};
        public string[] teamB { get; set; } = { };
        public int maxPlayers { get; set; } = 4;
    }

    public class CoopInfo : IModeInfo
    {
        public string[] players { get; set; } = {};
        public string aiplayer { get; set; } = "";
        public int maxPlayers { get; set; } = 4;
    }

    public class DuelInfo : IModeInfo
    {
        public string player1 { get; set; }
        public string player2 { get; set; }
        public int maxPlayers { get; set; } = 2;
    }

    public class ClassicInfoMM : IModeInfo
    {
        public PlayerMM[] teamA { get; set; } = { };
        public PlayerMM[] teamB { get; set; } = { };
        public int maxPlayers { get; set; } = 8;
    }

    public class CoopInfoMM : IModeInfo
    {
        public PlayerMM[] players { get; set; } = { };
        public string aiplayer { get; set; } = "";
        public int maxPlayers { get; set; } = 4;
    }

    public class DuelInfoMM : IModeInfo
    {
        public PlayerMM player1 { get; set; }
        public PlayerMM player2 { get; set; }
        public int maxPlayers { get; set; } = 2;
    }

    public class PlayerMM
    {
        public string username { get; set; }
        public bool ready { get; set; }
    }

    public class JoinLeaveRoom
    {
        public string username { get; set; }
        public string id { get; set; }
    }

    public enum MatchEditActions
    {
        AddAI,
        RemoveAI,
        ChangeAI,
        SwitchTeam,
        Ready,
    }

    public class EditMatch
    {
        public string id { get; set; }
        public MatchEditActions action { get; set; }
        public object options { get; set; }
    }

    public class AIEditOption
    {
        public Team team { get; set; }

    }

    public enum Team
    {
        TeamA,
        TeamB,
    }

}
