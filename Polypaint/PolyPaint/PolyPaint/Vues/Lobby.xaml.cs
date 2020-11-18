using MaterialDesignThemes.Wpf;
using PolyPaint.LobbyComm;
using PolyPaint.Modeles;
using PolyPaint.SocketCom;
using PolyPaint.Vues.Tutorial;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Windows;
using System.Windows.Controls;

namespace PolyPaint.Vues
{
    /// <summary>
    /// Interaction logic for Lobby.xaml
    /// </summary>
    public partial class Lobby : Page
    {
        private ChatWindow chatWindow;
        private Page chatRooms;
        private LobbyCommunication lobbyCommunication;
        private bool hasConfirmed = false;

        public Lobby()
        {
            List<SubscriptionInfo> subscriptions = new List<SubscriptionInfo>();
            subscriptions.Add(new SubscriptionInfo("isFirstTimeUserFatClient", onFirstTimeUser));
            lobbyCommunication = new LobbyCommunication(subscriptions);
            lobbyCommunication.checkFirstTimeUser();

            InitializeComponent();
            chatRooms = Rooms.instance;
            Chat.Content = chatRooms;
            MainWindow mainWindow = Application.Current.Windows.OfType<MainWindow>().FirstOrDefault();
            mainWindow.SizeToContent = SizeToContent.Manual;
            //full screen
            mainWindow.Visibility = Visibility.Collapsed;
            mainWindow.WindowStyle = WindowStyle.None;
            mainWindow.MaxHeight = SystemParameters.WorkArea.Height;
            mainWindow.WindowState = WindowState.Maximized;
            mainWindow.ResizeMode = ResizeMode.NoResize;
            mainWindow.Visibility = Visibility.Visible;
            CurrentSection.Content = "Game Modes";
            MainView.Content = new MatchMakingLobby();
            if (MainWindow.soundActivated) {
                soundCheck.Kind = PackIconKind.VolumeHigh;
            }

        }

        private void changeSound(object sender, RoutedEventArgs e)
        {
            if (MainWindow.soundActivated)
            {
                MainWindow.soundActivated = false;
                soundCheck.Kind = PackIconKind.VolumeOff;
            } else
            {
                MainWindow.soundActivated = true;
                soundCheck.Kind = PackIconKind.VolumeHigh;
            }
        }

        private void ListViewMenu_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            this.changedView(sender, e);
        }

        public void changedView(object sender, SelectionChangedEventArgs e, bool confirmQuit = false, int itemIndex = -1)
        {

            DrawCom.DrawCommunication.stopPreview();
            ListView list = (ListView)sender;
            ListViewItem item = (ListViewItem)list.SelectedItem;

            if (MainView != null && MainView.Content.GetType().ToString() == "PolyPaint.Vues.MatchMakingLobby" && !confirmQuit && 
                !hasConfirmed && ((MatchMakingLobby)MainView.Content).waitingMatchID != "")
            {
                if (((ListView)sender).SelectedIndex != 0)
                {
                    int selectedIndex = (((ListView)sender).SelectedIndex);
                    ((MatchMakingLobby)MainView.Content).SwitchedView(() => this.changedView(sender, e, true, selectedIndex));
                    ((ListView)sender).SelectedIndex = 0;
                }
                return;
            } else if (confirmQuit)
            {
                hasConfirmed = true;
                ((ListView)sender).SelectedIndex = itemIndex;
                return;
            }
            hasConfirmed = false;


            Console.WriteLine(item.Name);
            if (MainView != null)
            {
                Console.WriteLine(MainView.Content.GetType().ToString());

                switch (item.Name)
                {
                    case "Multiplayer": MainView.Content = new MatchMakingLobby(); CurrentSection.Content = "Multiplayer"; break;
                    case "Singleplayer": MainView.Content = new CreateSolo(); CurrentSection.Content = "Singleplayer"; break;
                    case "FreeDraw": MainView.Content = DrawingPage.instance; DrawingPage.instance.EnableDraw(); DrawingPage.instance.DisableComm();
                                                        DrawingPage.instance.ClearCanvas(); CurrentSection.Content = "Free Draw"; break;
                    case "GameCreation": MainView.Content = new GameCreationPage(); CurrentSection.Content = "Game Creation"; break;
                    case "MyAccount": MainView.Content = new AccountInfos(); CurrentSection.Content = "My Account"; break;
                }
            }
        }

        private void Chat_Navigated()
        {

        }

        private void viewGameMode()
        {

        }

        private void SignOut(object sender, RoutedEventArgs e)
        {
            Account.Instance.username = "";
            Account.Instance.password = "";
            SocketCommunication.Instance.socket.Emit("disconnectUser");
            SocketCommunication.Instance.clearRoomCallbacks();
            Application.Current.MainWindow.Content = new LoginPage();
            
        }

        private void quitApp(object sender, RoutedEventArgs e)
        {
            Application.Current.MainWindow.Close();

        }

        private void ChangeViewChat(object sender, RoutedEventArgs e)
        {

            if (Chat.IsVisible)
            {
                makeChatInvisible();
            }
            else
            {
                makeChatVisible();
            }
        }

        private void makeChatVisible()
        {
            Chat.Visibility = Visibility.Visible;
            //ChatVisibilityButton.Content = "Hide Chat";
        }

        private void makeChatInvisible()
        {

            Chat.Visibility = Visibility.Collapsed;
            //ChatVisibilityButton.Visibility = Visibility.Collapsed;
            //ChatVisibilityButton.Visibility = Visibility.Visible;
            //ChatVisibilityButton.Content = "Show Chat";
        }

        private void DetachChat(object sender, RoutedEventArgs e)
        {
            MainView.SetValue(Grid.ColumnSpanProperty, 2);
            chatWindow = new ChatWindow();
            Chat.Content = null;
            chatWindow.Content = chatRooms;
            chatWindow.Show();
            makeChatInvisible();
            hideChatButtons();

            chatWindow.Closed += ChatWindow_Closed;    // ChatWindow_Closed function is called when the chat window is closed
        }

        private void ChatWindow_Closed(object sender, EventArgs e)
        {
            chatWindow.Content = null;
            Chat.Content = chatRooms;
            makeChatVisible();
            showChatButtons();
            MainView.SetValue(Grid.ColumnSpanProperty, 1);
        }

        private void showChatButtons()
        {
            //ChatVisibilityButton.Visibility = Visibility.Visible;
            ChatDetachedButton.Visibility = Visibility.Visible;
        }

        private void hideChatButtons()
        {
            //ChatVisibilityButton.Visibility = Visibility.Hidden;
            ChatDetachedButton.Visibility = Visibility.Hidden;
        }

        private void openTutorial(object sender, EventArgs e)
        {
            Application.Current.Dispatcher.Invoke((Action)delegate {
                tutorialView.Content = new TutorialView();
                tutorialPopup.Visibility = Visibility.Visible;
                //haze.Visibility = Visibility.Visible;
            });
        }

        public void closeTutorial()
        {
            haze.Visibility = Visibility.Hidden;
        }

        private void onFirstTimeUser(object message)
        {
            openTutorial(null, null);
        }

    }
}
