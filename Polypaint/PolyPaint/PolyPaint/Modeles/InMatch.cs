using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PolyPaint.Modeles
{
    public class RoundStartDrawer
    {
        public int roundDuration {get; set;}
        public string wordToDraw { get; set;}
        public int guessesLeft { get; set; }
    }

    public class RoundStart
    {
        public string drawer { get; set; }
        public int roundDuration { get; set; }
        public bool isGuessing { get; set; }
        public int guessesLeft { get; set; }
    }


    public class GuessAnswer
    {
        public int triesLeft { get; set; }
    }

    public class GuessEmit
    {
        public string guess { get; set; }
        public string id { get; set; }
    }


    public class TimerUpdate
    {
        public int matchTime  { get; set; }
        public int roundTime { get; set; }
    }

    public class RoundEnd
    {
        public string message { get; set; }
        public int teamA { get; set; }
        public int teamB { get; set; }
    }

    public class MatchEnd
    {
        public string reason { get; set; }
        public bool wins { get; set; }
    }

    public class RoundInfo : INotifyPropertyChanged
    {

        private string _time;
        private string _wordToDraw;
        private int _tries;
        public string Time
        {
            get { return _time; }
            set { _time = value; NotifyPropertyChanged("Time"); }
        }

        public string Word
        {
            get { return _wordToDraw; }
            set { _wordToDraw = value; NotifyPropertyChanged("Word"); }
        }

        public int Tries
        {
            get { return _tries; }
            set { _tries = value; NotifyPropertyChanged("Tries"); }
        }

        public event PropertyChangedEventHandler PropertyChanged;
        private void NotifyPropertyChanged(String propertyName)
        {
            PropertyChangedEventHandler handler = PropertyChanged;
            if (null != handler)
            {
                handler(this, new PropertyChangedEventArgs(propertyName));
            }
        }
    }

    public class ObservableMatchInfo : INotifyPropertyChanged
    {

        private int _teamA;
        private int _teamB;
        private string _matchTimer;
        private int _roundNumber;
        private string _gameName;
        private int _maxRound;
        public int TeamAScore
        {
            get { return _teamA; }
            set { _teamA = value; NotifyPropertyChanged("TeamAScore"); }
        }

        public int TeamBScore
        {
            get { return _teamB; }
            set { _teamB = value; NotifyPropertyChanged("TeamBScore"); }
        }

        public string MatchTimer
        {
            get { return _matchTimer; }
            set { _matchTimer = value; NotifyPropertyChanged("MatchTimer"); }
        }

        public string GameName
        {
            get { return _gameName; }
            set { _gameName = value; NotifyPropertyChanged("GameName"); }
        }

        public int MaxRound
        {
            get { return _maxRound; }
            set { _maxRound = value; NotifyPropertyChanged("MaxRound"); }
        }

        public int RoundNumber
        {
            get { return _roundNumber; }
            set { _roundNumber = value; NotifyPropertyChanged("RoundNumber"); }
        }

        public event PropertyChangedEventHandler PropertyChanged;
        private void NotifyPropertyChanged(String propertyName)
        {
            PropertyChangedEventHandler handler = PropertyChanged;
            if (null != handler)
            {
                handler(this, new PropertyChangedEventArgs(propertyName));
            }
        }
    }

}


