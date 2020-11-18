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

//struct MessageInfo
//{
//    public string room;
//    public string Text;
//    public string sender;
//    public long time;
//}

namespace PolyPaint.Vues
{
    public class MatchMakingPlayer {
        public string name { get; set; }   
        public string avatarurl { get; set; }
        public bool isCreator { get; set; }       
        public bool isBot { get; set; }
        public bool isReady { get; set; }
    }

    public class StartMatchRequest { 
        public string id { get; set; }
    }


    /// <summary>
    /// Interaction logic for Window1.xaml
    /// </summary>
    public partial class WaitingLobbyClassic : Page, WaitingLobby
    {
        private Action LeaveFunction;
        public ClassicInfoMM match;
        public WaitingLobbyClassic()
        {
            match = new ClassicInfoMM();
            InitializeComponent();
        }

        public void MatchEdit(IModeInfo match) {
            this.match = (ClassicInfoMM)match;
            if (this.match.teamA.Length == 2 && this.match.teamB.Length == 2)
                ReadyButton.Visibility = Visibility.Visible;
            else
                ReadyButton.Visibility = Visibility.Hidden;
            MatchView.DataContext = this.match;
            teamAView.ItemsSource = this.PreProcessTeam(this.match.teamA, AddBotA);
            teamBView.ItemsSource = this.PreProcessTeam(this.match.teamB, AddBotB);

        }

        private ObservableCollection<MatchMakingPlayer> PreProcessTeam(PlayerMM[] team, Button addBotButton)
        {
            addBotButton.Visibility = Visibility.Visible;
            ObservableCollection<MatchMakingPlayer> teamProcessed = new ObservableCollection<MatchMakingPlayer>();
            for(int i = 0; i < team.Length; i++)
            {
                teamProcessed.Add(new MatchMakingPlayer()
                {
                    name = team[i].username,
                    isBot = team[i].username.Contains("(Ai Player)"),
                    isCreator = team[i].username == this.match.creator,
                    isReady = team[i].ready
                });
                if (team[i].username.Contains("(Ai Player)") || team.Length >= 2)
                    addBotButton.Visibility = Visibility.Collapsed;

            }
            return teamProcessed;
        }
        public void setLeaveFunction(Action action)
        {
            this.LeaveFunction = action;
        }

        private void LeaveButton(object sender, RoutedEventArgs e)
        {
            this.LeaveFunction();
        }

        private void ReadyButtonAction(object sender, RoutedEventArgs e)
        {
            if (readyTxt.Text == "Ready")
            {
                readyTxt.Text = "Not ready";
                readyIconBtn.Foreground = Brushes.Red;
            } else
            {
                readyTxt.Text = "Ready";
                readyIconBtn.Foreground = Brushes.LightGreen;
            }
            
            SocketCommunication.Instance.emit("editMatch", JsonConvert.SerializeObject(
                new EditMatch() { id = match.id, action = MatchEditActions.Ready, options = Account.Instance.username
            }) );
        }

        public IModeInfo GetMatch()
        {
            return match;
        }

        private void AddAITeamA(object sender, RoutedEventArgs e)
        {
            AddBotA.Visibility = Visibility.Collapsed;
            EditMatch r = new EditMatch() { 
                id = match.id,
                action = MatchEditActions.AddAI,
                options = Team.TeamA
            };
            SocketCommunication.Instance.emit("editMatch", JsonConvert.SerializeObject(r));
        }

        private void AddAITeamB(object sender, RoutedEventArgs e)
        {
            AddBotB.Visibility = Visibility.Collapsed;
            EditMatch r = new EditMatch()
            {
                id = match.id,
                action = MatchEditActions.AddAI,
                options = Team.TeamB
            };
            SocketCommunication.Instance.emit("editMatch", JsonConvert.SerializeObject(r));
        }

        private void RemoveBotA(object sender, RoutedEventArgs e)
        {
            EditMatch r = new EditMatch()
            {
                id = match.id,
                action = MatchEditActions.RemoveAI,
                options = Team.TeamA
            };
            SocketCommunication.Instance.emit("editMatch", JsonConvert.SerializeObject(r));
            AddBotA.Visibility = Visibility.Visible;
        }

        private void RemoveBotB(object sender, RoutedEventArgs e)
        {
            EditMatch r = new EditMatch()
            {
                id = match.id,
                action = MatchEditActions.RemoveAI,
                options = Team.TeamB
            };
            SocketCommunication.Instance.emit("editMatch", JsonConvert.SerializeObject(r));
            AddBotB.Visibility = Visibility.Visible;
        }

        private void SwitchTeam(object sender, RoutedEventArgs e)
        {
            EditMatch r = new EditMatch()
            {
                id = match.id,
                action = MatchEditActions.SwitchTeam,
                options = Account.Instance.username,
            };
            SocketCommunication.Instance.emit("editMatch", JsonConvert.SerializeObject(r));
        }
    }
}
