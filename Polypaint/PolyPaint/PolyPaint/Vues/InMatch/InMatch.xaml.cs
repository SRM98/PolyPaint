using System;
using System.Collections.Generic;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using Newtonsoft.Json;
using PolyPaint.ChatComm;
using PolyPaint.Modeles;
using System.Windows.Media;
using System.Collections.ObjectModel;
using PolyPaint.SocketCom;
using System.Windows.Threading;
using System.Media;
using MaterialDesignThemes.Wpf;
using PolyPaint.Vues.Tutorial;
using System.Windows.Media.Imaging;

//struct MessageInfo
//{
//    public string room;
//    public string Text;
//    public string sender;
//    public long time;
//}

namespace PolyPaint.Vues
{

    class PlayerAvatarsRequest
    {
        public string[] url { get; set; }
        public string[] players { get; set; }
        public string matchID { get; set; }
    }
    /// <summary>
    /// Interaction logic for Window1.xaml
    /// </summary>
    public partial class InMatch : Page
    {
        public IModeInfo match;
        public DrawingPage drawPage;
        public RoundInfo roundInfo;
        public ObservableMatchInfo matchInfo;
        private Page chatRooms;
        public MediaPlayer mplayer;
        private bool clickToContinue;
        private PlayerAvatarsRequest playersAvatar;

        public InMatch(IModeInfo match)
        {
            SocketCommunication.Instance.subscribeOnce("roundStart", onRoundStart);
            SocketCommunication.Instance.subscribeOnce("roundStartDrawer", onRoundStartDrawer);
            SocketCommunication.Instance.subscribeOnce("badGuess", onGuess);
            SocketCommunication.Instance.subscribeOnce("roundEnd", onRoundEnd);
            SocketCommunication.Instance.subscribeOnce("matchEnd", onMatchEnd);
            SocketCommunication.Instance.subscribeOnce("reply", onReplyRight);
            SocketCommunication.Instance.subscribeOnce("timerUpdate", onTimerUpdate);
            SocketCommunication.Instance.subscribeOnce("giveAvatarUrl", onReceivedAvatars);

            this.match = match;
            roundInfo = new RoundInfo(){ Time = "0:00", Word = "", Tries = 420};
            matchInfo = new ObservableMatchInfo(){ GameName = match.name, MaxRound = match.nbRounds, TeamAScore = 0, TeamBScore = 0, MatchTimer = this.secondsToString(match.maxTime), RoundNumber = 1};
            InitializeComponent();
            RoundAttributes.DataContext = roundInfo;
            clickToContinue = false;

            GridContent.DataContext = this.match;
            ClassicInfos.DataContext = this.matchInfo;
            CoopInfos.DataContext = this.matchInfo;
            DuelInfos.DataContext = this.matchInfo;
            SoloInfos.DataContext = this.matchInfo;

            chatRooms = Rooms.instance;
            Chat.Content = chatRooms;

            DrawingPage.instance.ClearCanvas();
            drawPage = DrawingPage.instance;
            DrawCom.DrawCommunication.CommunicationEnabled = true;
            drawPage.surfaceDessin.setRoom(match.id);
            DrawingFrame.Content = drawPage;

            switch (this.match.type)
            {
                case Modes.Classic: this.setClassic(); break;
                case Modes.Coop: this.setCoop(); break;
                case Modes.Duel: this.setDuel(); break;
                case Modes.Solo: this.setSolo(); break;
            }

            this.clearRoundInfos();
            mplayer = new MediaPlayer();
            if (MainWindow.soundActivated)
            {
                soundCheck.Kind = PackIconKind.VolumeHigh;
            }

            SocketCommunication.Instance.emit("getAvatarUrl", JsonConvert.SerializeObject( new PlayerAvatarsRequest {matchID = this.match.id }));
        }

        private void changeSound(object sender, RoutedEventArgs e)
        {
            if (MainWindow.soundActivated)
            {
                MainWindow.soundActivated = false;
                soundCheck.Kind = PackIconKind.VolumeOff;
            }
            else
            {
                MainWindow.soundActivated = true;
                soundCheck.Kind = PackIconKind.VolumeHigh;
            }
        }

        private void quitApp(object sender, RoutedEventArgs e)
        {
            Application.Current.MainWindow.Close();
        }

        public void onReceivedAvatars(object Object)
        {
            Application.Current.Dispatcher.Invoke((Action)delegate {
                playersAvatar = JsonConvert.DeserializeObject<PlayerAvatarsRequest>(Object.ToString());
                switch (this.match.type)
                {
                    case Modes.Classic: this.setClassic(); break;
                    case Modes.Coop: this.setCoop(); break;
                    case Modes.Duel: this.setDuel(); break;
                    case Modes.Solo: this.setSolo(); break;
                }
            });
        }
        public void onReplyRight(object Object)
        {
            Application.Current.Dispatcher.Invoke((Action)delegate {
                RoundStart replyInfo = JsonConvert.DeserializeObject<RoundStart>(Object.ToString());
                roundInfo.Tries = 1;
                roundInfo.Time = this.secondsToString(replyInfo.roundDuration);
                if (replyInfo.isGuessing)
                {
                    setGuessingInfo();
                    this.showMessage("You have the right to reply!", "(Click anywhere to continue)");
                } else
                {
                    clearRoundInfos();
                    this.showMessage("Other team is guessing!", "(Click anywhere to continue)");
                }
                clickToContinue = true;
            });
        }

        public void onMatchEnd(object Object)
        {
            Application.Current.Dispatcher.Invoke((Action)delegate {
                MatchEnd matchEndInfo = JsonConvert.DeserializeObject<MatchEnd>(Object.ToString());
                string[] splitted = matchEndInfo.reason.Split('!');
                if (splitted.Length > 1)
                    this.showMessage(splitted[0] + "!", splitted[1]);
                else
                    this.showMessage(splitted[0] + "!");
                if (matchEndInfo.wins)
                {
                    if(MainWindow.soundActivated)
                    {
                        mplayer.Open(new Uri(@"../../Resources/Sounds/tadaSoundEffect.mp3", UriKind.Relative));
                        mplayer.Play();
                    }                 
                    Firework.startFirework();
                }
            });
        }

        public void onRoundEnd(object Object)
        {
            Application.Current.Dispatcher.Invoke((Action)delegate {
                RoundEnd roundEndInfo = JsonConvert.DeserializeObject<RoundEnd>(Object.ToString());
                this.matchInfo.TeamAScore = roundEndInfo.teamA;
                this.matchInfo.TeamBScore = roundEndInfo.teamB;
                this.drawPage.ClearCanvas();
                this.showMessage("The round has ended", roundEndInfo.message);
                if (this.matchInfo.RoundNumber < this.matchInfo.MaxRound)
                    this.matchInfo.RoundNumber++;
                clearRoundInfos();
            });
        }

        public void onRoundStart(object Object)
        {
            Application.Current.Dispatcher.Invoke((Action)delegate {
                Message.Visibility = Visibility.Collapsed;
                RoundStart roundStartInfo = JsonConvert.DeserializeObject<RoundStart>(Object.ToString());
                roundInfo.Time = this.secondsToString(roundStartInfo.roundDuration);
                roundInfo.Tries = roundStartInfo.guessesLeft;
                this.drawPage.ClearCanvas();

                drawPage.DisableDraw();
                if (roundStartInfo.isGuessing)
                    //Is a guesser
                    setGuessingInfo();
                else
                    // Is a viewer
                    setViewerInfo();
            });
        }

        public void onRoundStartDrawer(object Object)
        {
            Application.Current.Dispatcher.Invoke((Action)delegate {
                Message.Visibility = Visibility.Collapsed;
                RoundStartDrawer roundStartInfo = JsonConvert.DeserializeObject<RoundStartDrawer>(Object.ToString());
                roundInfo.Tries = roundStartInfo.guessesLeft;
                drawPage.EnableDraw();
                this.drawPage.ClearCanvas();
                setDrawingInfo();
                roundInfo.Word = roundStartInfo.wordToDraw;
            });
        }

        public void setDrawingInfo()
        {
            this.clearRoundInfos();
            drawWordLabel.Visibility = Visibility.Visible;
            drawWord.Visibility = Visibility.Visible;
        }

        public void setViewerInfo()
        {
            this.clearRoundInfos();
            viewerLabel.Visibility = Visibility.Visible;
        }

        public void setGuessingInfo()
        {
            this.clearRoundInfos();
            guessBoxLabel.Visibility = Visibility.Visible;
            guessPanel.Visibility = Visibility.Visible;
        }

        public void clearRoundInfos()
        {
            guessBoxLabel.Visibility = Visibility.Collapsed;
            guessPanel.Visibility = Visibility.Collapsed;
            drawWordLabel.Visibility = Visibility.Collapsed;
            drawWord.Visibility = Visibility.Collapsed;
            viewerLabel.Visibility = Visibility.Collapsed;
        }

        public void onGuess(object Object)
        {
            Application.Current.Dispatcher.Invoke((Action)delegate {
                GuessAnswer guessInfo = JsonConvert.DeserializeObject<GuessAnswer>(Object.ToString());
                if (guessInfo.triesLeft < roundInfo.Tries)
                {
                    
                    if (MainWindow.soundActivated && guessPanel.Visibility == Visibility.Visible)
                    {
                        mplayer.Open(new Uri(@"../../Resources/Sounds/wrongAnswerSound2.mp3", UriKind.Relative));
                        mplayer.Play();
                    }               
                }
                roundInfo.Tries = guessInfo.triesLeft;
            });
        }

        public void showMessage(string title, string message = "")
        {
            MessageTitle.Text = title;
            MessageTitle.Visibility = Visibility.Visible;
            if (message != "")
            {
                MessageText.Text = message;
                MessageText.Visibility = Visibility.Visible;
            } else
            {
                MessageText.Visibility = Visibility.Collapsed;
            }
            Message.Visibility = Visibility.Visible;
        }

        public void emitGuess(string word)
        {
            roundInfo.Word = word;
            SocketCommunication.Instance.emit("guess", JsonConvert.SerializeObject(new GuessEmit(){ guess = word, id = this.match.id }));
        }
        public void guessButton(object sender, RoutedEventArgs e)
        {
            emitGuess(guessBox.Text);
            guessBox.Clear();
        }

        private void OnKeyDownHandler(object sender, KeyEventArgs e)
        {
            if (e.Key == Key.Return)
            {
                guessButton(sender, e);
            }
        }

        public void onTimerUpdate(object Object)
        {
            Application.Current.Dispatcher.Invoke((Action)delegate {
                TimerUpdate timerUpdate = JsonConvert.DeserializeObject<TimerUpdate>(Object.ToString());

                if (timerUpdate.roundTime > 0)
                    roundInfo.Time = this.secondsToString(timerUpdate.roundTime);
                if (timerUpdate.matchTime > 0)
                    matchInfo.MatchTimer = this.secondsToString(timerUpdate.matchTime);
            });
        }

        public string secondsToString(int seconds)
        {
            if ( seconds % 60 < 10) 
                return (int)(seconds / 60) + ":0" + seconds % 60;
            return (int)(seconds / 60) + ":" + seconds % 60;
        }

        public void setSolo()
        {
            DuelInfos.Visibility = Visibility.Collapsed;
            CoopInfos.Visibility = Visibility.Collapsed;
            ClassicInfos.Visibility = Visibility.Collapsed;
            SoloInfos.Visibility = Visibility.Visible;
        }

        public void setDuel()
        {
            DuelName1.Text = ((DuelInfo)this.match).player1;
            DuelName2.Text = ((DuelInfo)this.match).player2;
            if (playersAvatar != null)
            {
                duelAvatar1.ImageSource = new BitmapImage(new Uri(MainWindow.ServerIP + playersAvatar.url[0]));
                duelAvatar2.ImageSource = new BitmapImage(new Uri(MainWindow.ServerIP + playersAvatar.url[1]));
            }
            DuelInfos.Visibility = Visibility.Visible;
            CoopInfos.Visibility = Visibility.Collapsed;
            ClassicInfos.Visibility = Visibility.Collapsed;
            SoloInfos.Visibility = Visibility.Collapsed;
            TriesLabel.Visibility = Visibility.Collapsed;
        }
        public void setCoop()
        {
            CoopTeamView.ItemsSource = this.PreProcessTeam(((CoopInfo)this.match).players);
            SoloInfos.Visibility = Visibility.Collapsed;
            DuelInfos.Visibility = Visibility.Collapsed;
            CoopInfos.Visibility = Visibility.Visible;
            ClassicInfos.Visibility = Visibility.Collapsed;
        }

        public void setClassic() {
            teamAView.ItemsSource = this.PreProcessTeam(((ClassicInfo)this.match).teamA);
            teamBView.ItemsSource = this.PreProcessTeam(((ClassicInfo)this.match).teamB);
            DuelInfos.Visibility = Visibility.Collapsed;
            SoloInfos.Visibility = Visibility.Collapsed;
            CoopInfos.Visibility = Visibility.Collapsed;
            ClassicInfos.Visibility = Visibility.Visible;
        }

        private ObservableCollection<MatchMakingPlayer> PreProcessTeam(string[] team)
        {
            ObservableCollection<MatchMakingPlayer> teamProcessed = new ObservableCollection<MatchMakingPlayer>();
            for(int i = 0; i < team.Length; i++)
            {
                string url = "";
                if (playersAvatar != null)
                {
                    url = MainWindow.ServerIP + playersAvatar.url[Array.IndexOf(playersAvatar.players, team[i])];
                } 
                teamProcessed.Add(new MatchMakingPlayer()
                {
                    name = team[i],
                    isBot = team[i].Contains("(Ai Player)"),
                    isCreator = team[i] == this.match.creator,
                    avatarurl = url,
                });
            }
            return teamProcessed;
        }

        private void LeaveButton(object sender, RoutedEventArgs e)
        {
            SocketCommunication.Instance.emit("leaveInMatch", JsonConvert.SerializeObject(new JoinLeaveRoom() { username = Account.Instance.username, id = this.match.id}));
            SocketCommunication.Instance.clearRoomCallbacks();
            Application.Current.MainWindow.Content = new Lobby();
        }

        private void onCollapseGrid(object sender, RoutedEventArgs e)
        {
            if (clickToContinue)
            {
                Message.Visibility = Visibility.Collapsed;
                clickToContinue = false;
            }
        }
        private void getHint(object sender, RoutedEventArgs e)
        {
            var text = "hint!";
            MessageInfo messageInfo = new MessageInfo();
            messageInfo.text = text;
            messageInfo.room = this.match.id;
            messageInfo.sender = Account.Instance.username;
            SocketCommunication.Instance.emit("send", JsonConvert.SerializeObject(messageInfo));
        }

        private void openTutorial(object sender, RoutedEventArgs e)
        {
            var tutorial = new TutorialView();
            tutorialView.Content = tutorial;
            tutorial.changeMode(match.type);
            tutorialPopup.Visibility = Visibility.Visible;
        }
    }
}
