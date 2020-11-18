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

    /// <summary>
    /// Interaction logic for Window1.xaml
    /// </summary>
    public partial class WaitingLobbyCoop : Page, WaitingLobby
    {
        private Action LeaveFunction;
        public CoopInfoMM match;
        public WaitingLobbyCoop()
        {
            match = new CoopInfoMM();
            InitializeComponent();
        }

        public void MatchEdit(IModeInfo match) {
            this.match = (CoopInfoMM)match;
            if (this.match.placesLeft <= 2)
                ReadyButton.Visibility = Visibility.Visible;
            else
                ReadyButton.Visibility = Visibility.Hidden;
            MatchView.DataContext = this.match;
            players.ItemsSource = this.PreProcessTeam(this.match.players);
        }

        private ObservableCollection<MatchMakingPlayer> PreProcessTeam(PlayerMM[] team)
        {
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

        public IModeInfo GetMatch()
        {
            return match;
        }

        private void ReadyButtonAction(object sender, RoutedEventArgs e)
        {

            if (readyTxt.Text == "Ready")
            {
                readyTxt.Text = "Not ready";
                readyIconBtn.Foreground = Brushes.Red;
            }
            else
            {
                readyTxt.Text = "Ready";
                readyIconBtn.Foreground = Brushes.LightGreen;
            }

            SocketCommunication.Instance.emit("editMatch", JsonConvert.SerializeObject(
            new EditMatch()
            {
                id = match.id,
                action = MatchEditActions.Ready,
                options = Account.Instance.username
            }));
        }
    }
}
