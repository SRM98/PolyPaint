using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PolyPaint.Modeles
{
    enum Difficulty
    {
        Easy = 0,
        Intermediate = 1,
        Hard = 2,
    }

    enum DrawingMode
    {
        Replicated = 0,
        CenteredGoingIn = 1,
        CenteredGoingOut = 2,
        PanoramiqueTop = 3,
        PanoramiqueBottom = 4,
        PanoramiqueRight = 5,
        PanoramiqueLeft = 6,
        Random = 7
    }
    class Game
    {
        public bool assistedMode { get; set; }
        public string word { get; set; }
        public object image { get; set; }
        public DrawingMode mode { get; set; }
        public Difficulty difficulty { get; set; }
        public List<string> clues { get; set; }
        private static Game instance;
        public Game()
        {
            clues = new List<string>();
        }

        public static Game Instance
        {
            get
            {
                if (instance == null)
                    instance = new Game();
                return instance;
            }

            set { instance = value; }
        }
    }
}
