using System;
using System.Linq;
using System.Windows;
using System.Windows.Controls;
using PolyPaint.SocketCom;
using Newtonsoft.Json;
using PolyPaint.Modeles;
using System.Collections.ObjectModel;


namespace PolyPaint.Vues
{

    public class MatchMessage
    {
        public Modes type { get; set; }
        public Object content { get; set; }
    }

    public class MatchStartMessage
    {
        public Modes type { get; set; }
        public Object content { get; set; }
        public Object match { get; set; }
    }

    public class MatchMakingError
    {
        public string description { get; set; }
    }

    public interface WaitingLobby
    {
        void setLeaveFunction(Action action);
        void MatchEdit(IModeInfo match);
        IModeInfo GetMatch();
    }

    public enum MatchMakingState
    {
        MatchesLobby,
        JoiningMatch,
        InWaitingLobby,
    }

    
    public partial class MatchMakingLobby : Page
    {
        private ObservableCollection<CoopInfoMM> matchesCoop;
        private ObservableCollection<DuelInfoMM> matchesDuel;
        private ObservableCollection<ClassicInfoMM> matchesClassic;
        private Modes currentSelection = Modes.Classic;

        private string username;
        private WaitingLobby waitingLobby;
        public string waitingMatchID;
        private MatchMakingState state;
        private Action switchViewAction;

        public MatchMakingLobby()
        {
            InitializeComponent();
            SocketCommunication.Instance.subscribeOnce("getMatches", onGetMatches);
            SocketCommunication.Instance.subscribeOnce("matchCreated", onMatchCreate);
            SocketCommunication.Instance.subscribeOnce("matchEdit", onMatchEdit);
            SocketCommunication.Instance.subscribe("matchJoined", onMatchJoin);
            SocketCommunication.Instance.subscribeOnce("matchStarted", onMatchStarted);
            SocketCommunication.Instance.subscribeOnce("matchmakingError", onError);
            currentSelection = Modes.Classic;

            matchesCoop = new ObservableCollection<CoopInfoMM>();
            matchesDuel = new ObservableCollection<DuelInfoMM>();
            matchesClassic = new ObservableCollection<ClassicInfoMM>();
            matchList.ItemsSource = matchesClassic;

            username = Account.Instance.username;
            waitingMatchID = "";
            state = MatchMakingState.MatchesLobby;

            getMatches(Modes.Duel);
            getMatches(Modes.Coop);
            getMatches(Modes.Classic);
        }

        private void onMatchStarted(Object match)
        {
            Application.Current.Dispatcher.Invoke((Action)delegate {
                MatchStartMessage message = JsonConvert.DeserializeObject<MatchStartMessage>(match.ToString());
                if (message.content.ToString() == waitingMatchID)
                {
                    SocketCommunication.Instance.clearRoomCallbacks();
                    switch (message.type)
                    {
                        case Modes.Classic:
                            ClassicInfo matchInfoClassic = JsonConvert.DeserializeObject<ClassicInfo>(message.match.ToString());
                            Application.Current.MainWindow.Content = new InMatch(matchInfoClassic); break;
                        case Modes.Coop: 
                            CoopInfo matchInfoCoop = JsonConvert.DeserializeObject<CoopInfo>(message.match.ToString());
                            Application.Current.MainWindow.Content = new InMatch(matchInfoCoop); break;
                        case Modes.Duel: 
                            DuelInfo matchInfoDDuel = JsonConvert.DeserializeObject<DuelInfo>(message.match.ToString());
                            Application.Current.MainWindow.Content = new InMatch(matchInfoDDuel); break;
                    }
                }
            });
        }

        private void onMatchCreate(Object match)
        {
            Application.Current.Dispatcher.Invoke((Action)delegate {

                MatchMessage message = JsonConvert.DeserializeObject<MatchMessage>(match.ToString());

                switch (message.type)
                {
                    case Modes.Classic: matchesClassic.Add(JsonConvert.DeserializeObject<ClassicInfoMM>(message.content.ToString()));  break;
                    case Modes.Coop:    matchesCoop.Add(JsonConvert.DeserializeObject<CoopInfoMM>(message.content.ToString())); break;
                    case Modes.Duel:    matchesDuel.Add(JsonConvert.DeserializeObject<DuelInfoMM>(message.content.ToString())); break;
                }
            });
        }

        private void onMatchJoin(Object match)
        {
            Application.Current.Dispatcher.Invoke((Action)delegate {

                MatchMessage message = JsonConvert.DeserializeObject<MatchMessage>(match.ToString());

                switch (message.type)
                {
                    case Modes.Classic: joinMatch<ClassicInfoMM, WaitingLobbyClassic>
                                        (JsonConvert.DeserializeObject<ClassicInfoMM>(message.content.ToString()), matchesClassic); break;
                    case Modes.Coop: joinMatch<CoopInfoMM, WaitingLobbyCoop>
                                     (JsonConvert.DeserializeObject<CoopInfoMM>(message.content.ToString()), matchesCoop); break;
                    case Modes.Duel: joinMatch<DuelInfoMM, WaitingLobbyDuel>
                                     (JsonConvert.DeserializeObject<DuelInfoMM>(message.content.ToString()), matchesDuel); break;
                }
            });
        }

        private void onMatchEdit(Object match)
        {
            Application.Current.Dispatcher.Invoke((Action)delegate {

                MatchMessage message = JsonConvert.DeserializeObject<MatchMessage>(match.ToString());

                switch (message.type)
                {
                    case Modes.Classic:
                        editSingle<ClassicInfoMM>(JsonConvert.DeserializeObject<ClassicInfoMM>(message.content.ToString()), matchesClassic);
                        break;
                    case Modes.Coop:
                        editSingle<CoopInfoMM>(JsonConvert.DeserializeObject<CoopInfoMM>(message.content.ToString()), matchesCoop);
                        break;
                    case Modes.Duel:
                        editSingle<DuelInfoMM>(JsonConvert.DeserializeObject<DuelInfoMM>(message.content.ToString()), matchesDuel);
                        break;
                }
            });
        }

        private void onGetMatches(Object matchesJSON)
        {
            Application.Current.Dispatcher.Invoke((Action)delegate {
                Console.WriteLine(matchesJSON);
                MatchMessage message = JsonConvert.DeserializeObject<MatchMessage>(matchesJSON.ToString());

                switch (message.type)
                {
                    case Modes.Classic: editMatches<ClassicInfoMM>(JsonConvert.DeserializeObject<ClassicInfoMM[]>(message.content.ToString()), matchesClassic);
                                        matchList.ItemsSource = matchesClassic;
                                        break;
                    case Modes.Coop: editMatches<CoopInfoMM>(JsonConvert.DeserializeObject<CoopInfoMM[]>(message.content.ToString()), matchesCoop);
                                     matchList.ItemsSource = matchesCoop;
                                     break;
                    case Modes.Duel: editMatches<DuelInfoMM>(JsonConvert.DeserializeObject<DuelInfoMM[]>(message.content.ToString()), matchesDuel);
                                     matchList.ItemsSource = matchesDuel;
                                     break;
                }
            });
        }

        private void editMatches<T> (T[] newMatches, ObservableCollection<T> oldMatches) where T : IModeInfo
        {
            oldMatches.Clear();
            for (int i = 0; i < newMatches.Length; i++)
            {
                oldMatches.Add(newMatches[i]);
            }
        }

        private void editSingle<T>(T newMatch, ObservableCollection<T> oldMatches) where T : IModeInfo, new()
        {
            if ((state == MatchMakingState.InWaitingLobby) && (newMatch.id == waitingMatchID))
            {
                waitingLobby.MatchEdit(newMatch);
                return;
            }
            switch (currentSelection)
            {
                case Modes.Classic:
                    getMatches(Modes.Classic);
                    break;
                case Modes.Coop:
                    getMatches(Modes.Coop);
                    break;
                case Modes.Duel:
                    getMatches(Modes.Duel);
                    break;
            }
        }

        private void joinMatch<T, U>(T newMatch, ObservableCollection<T> oldMatches) where T : IModeInfo, new() where U : WaitingLobby, new()
        {
            waitingMatchID = newMatch.id;
            state = MatchMakingState.InWaitingLobby;

            waitingLobby = new U();
            MatchLobby.Content = waitingLobby;
            Panel.SetZIndex(MatchLobby, 3);
            waitingLobby.setLeaveFunction(backToMatchMaking);
            waitingLobby.MatchEdit(newMatch);
        }

        private void ListViewMenu_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            ListView list = (ListView)sender;
            ListViewItem item = (ListViewItem)list.SelectedItem;

            if (matchList == null)
                return;

            switch (item.Name)
            {
                case "Classic": matchList.ItemsSource = matchesClassic; currentSelection = Modes.Classic; getMatches(Modes.Classic); break;
                case "Coop": matchList.ItemsSource = matchesCoop; currentSelection = Modes.Coop; getMatches(Modes.Coop); break;
                case "Duel": matchList.ItemsSource = matchesDuel; currentSelection = Modes.Duel; getMatches(Modes.Duel); break;
            }
        }

        public void joinMatchButton(object sender, RoutedEventArgs e)
        {
            Thickness NewThickness = new Thickness(0, 0, 0, 0);
            mainGrid.Margin = NewThickness;
            var button = sender as Button;
            var matchID = (string)button.Tag;
            state = MatchMakingState.JoiningMatch;
            SocketCommunication.Instance.emit("joinMatch", JsonConvert.SerializeObject(new JoinLeaveRoom() { username = username, id = matchID }));
        }

        public void backToMatchMaking()
        {
            Thickness NewThickness = new Thickness(20, 20, 20, 10);
            mainGrid.Margin = NewThickness;
            if (waitingMatchID != "")
                SocketCommunication.Instance.emit("leaveMatch", JsonConvert.SerializeObject(new JoinLeaveRoom() { username = username, id = waitingMatchID }));
            state = MatchMakingState.MatchesLobby;
            waitingMatchID = "";
            MatchLobby.Content = null;
            Panel.SetZIndex(MatchLobby, -1);
            reload();
        }

        private void reload()
        {
            getMatches(Modes.Duel);
            getMatches(Modes.Coop);
            getMatches(Modes.Classic);
        }

        private void getMatches(Modes mode)
        {
            SocketCommunication.Instance.emit("getMatches", JsonConvert.SerializeObject( new MatchMessage() { content = null, type = mode }));
        }

        private void onError(Object match)
        {
            Application.Current.Dispatcher.Invoke((Action)delegate {
                MessageBox.Show(JsonConvert.DeserializeObject<MatchMakingError>(match.ToString()).description, "Error");
                state = MatchMakingState.MatchesLobby;
                waitingMatchID = "";
                MatchLobby.Content = null;
                Thickness NewThickness = new Thickness(20, 20, 20, 10);
                mainGrid.Margin = NewThickness;
                Panel.SetZIndex(MatchLobby, -1);
            });
        }

        private void CreateMatchButton(object sender, RoutedEventArgs e)
        {
            Thickness NewThickness = new Thickness(0, 0, 0, 0);
            mainGrid.Margin = NewThickness;
            state = MatchMakingState.MatchesLobby;
            CreateMatch matchCreation = new CreateMatch();
            MatchLobby.Content = matchCreation;
            Panel.SetZIndex(MatchLobby, 3);
            matchCreation.setLeaveFunction(backToMatchMaking);

            matchCreation.setCreateFunction(delegate (IModeInfo game) {
                state = MatchMakingState.JoiningMatch;
                SocketCommunication.Instance.emit("createMatch", JsonConvert.SerializeObject(game));
            });
            
        }

        private void Stay(object sender, RoutedEventArgs e)
        {
            Message.Visibility = Visibility.Collapsed;
        }

        private void Quit(object sender, RoutedEventArgs e)
        {
            SocketCommunication.Instance.emit("leaveMatch", JsonConvert.SerializeObject(new JoinLeaveRoom() { username = username, id = waitingMatchID }));
            this.switchViewAction();
            Message.Visibility = Visibility.Collapsed;
        }

        public void SwitchedView(Action switchViewAction) 
        {
            Message.Visibility = Visibility.Visible;
            this.switchViewAction = switchViewAction;
        }
    }
}
