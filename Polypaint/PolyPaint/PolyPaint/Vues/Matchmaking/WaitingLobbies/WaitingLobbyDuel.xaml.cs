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
    public partial class WaitingLobbyDuel : Page, WaitingLobby
    {
        private Action LeaveFunction;
        public DuelInfoMM match;
        public WaitingLobbyDuel()
        {
            match = new DuelInfoMM();
            InitializeComponent();
        }

        public void MatchEdit(IModeInfo match) {
            this.match = (DuelInfoMM)match;
            if (this.match.playerCount > 1 )
                ReadyButton.Visibility = Visibility.Visible;
            else
                ReadyButton.Visibility = Visibility.Hidden;
            MatchView.DataContext = this.match;
            player1.Text = this.match.player1.username;
            player2.Text = this.match.player2.username;

            if (player2.Text.Length > 0)
                _readyIcon2.Visibility = Visibility.Visible;

            if (this.match.player1.ready)
            {
                _readyIcon1.Foreground = Brushes.LightGreen;
            } else
            {
                _readyIcon1.Foreground = Brushes.Red;
            }

            if (this.match.player2.ready)
            {
                _readyIcon2.Foreground = Brushes.LightGreen;
            }
            else
            {
                _readyIcon2.Foreground = Brushes.Red;
            }

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
