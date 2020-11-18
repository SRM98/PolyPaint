using Newtonsoft.Json;
using PolyPaint.ChatComm;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using PolyPaint.Modeles;
using System.ComponentModel;
using PolyPaint.VueModeles;

namespace PolyPaint.Vues
{
    /// Interaction logic for Rooms.xaml
    public partial class Rooms : Page
    {
        private RoomCommunication roomCommunication;
        private ChatBox chatbox_;
        private RoomsViewModel viewModel;
        public static Rooms instance;

        public Rooms()
        {
            InitializeComponent();

            List<SubscriptionInfo> subscriptions = new List<SubscriptionInfo>();
            subscriptions.Add(new SubscriptionInfo("allRooms", onAllRooms));
            subscriptions.Add(new SubscriptionInfo("ownRooms", onOwnRooms));
            subscriptions.Add(new SubscriptionInfo("validRoom", onValidRoom));
            subscriptions.Add(new SubscriptionInfo("invalidRoom", onInvalidRoom));
            subscriptions.Add(new SubscriptionInfo("matchJoined", onMatchJoined));
            subscriptions.Add(new SubscriptionInfo("leftMatch", onLeftMatch));
            subscriptions.Add(new SubscriptionInfo("roomMessages", onChatHistory));
            roomCommunication = new RoomCommunication(subscriptions);

            viewModel = new RoomsViewModel();
            //chatview.Height = System.Windows.SystemParameters.PrimaryScreenHeight/2;
            roomCommunication.getOwnRooms();
            chatbox_ = new ChatBox("lobby");
            chatBox.Content = chatbox_;
            roomsList.ItemsSource = viewModel.rooms;
            chatroomName.Text = "lobby";
            searchedRoomsList.ItemsSource = viewModel.searchedRooms;
            DataContext = viewModel;
        }

        public void triggerJoinRoom(object sender, RoutedEventArgs e)
        {
            if (joinRoomView.IsVisible)
            {
                roomsArea.Visibility = Visibility.Visible;
                joinRoomView.Visibility = Visibility.Hidden;
                chatArea.Visibility = Visibility.Visible;
                createRoomViewButton.Visibility = Visibility.Hidden;
                joinRoomViewButton.Content = "+ room";
                chatroomTitle.Visibility = Visibility.Visible;
                quitButton.Visibility = chatroomName.Text != "lobby" ? Visibility.Visible : Visibility.Hidden;
            }
            else
            {
                roomsArea.Visibility = Visibility.Hidden;
                chatroomTitle.Visibility = Visibility.Hidden;
                joinRoomView.Visibility = Visibility.Visible;
                chatArea.Visibility = Visibility.Hidden;
                createRoomViewButton.Visibility = Visibility.Visible;
                quitButton.Visibility = Visibility.Hidden;
                joinRoomViewButton.Content = "←";
                roomCommunication.getRooms();
            }
        }

        public void triggerCreateRoom(object sender, RoutedEventArgs e)
        {
            if (createRoomView.IsVisible)
            {
                joinRoomView.Visibility = Visibility.Visible;
                createRoomView.Visibility = Visibility.Hidden;
                joinRoomViewButton.Visibility = Visibility.Visible;
                createRoomViewButton.Content = "Create room";
            }
            else
            {
                joinRoomView.Visibility = Visibility.Hidden;
                createRoomView.Visibility = Visibility.Visible;
                joinRoomViewButton.Visibility = Visibility.Hidden;
                createRoomViewButton.Content = "Cancel";
            }
        }

        private void changeGameRoom(object sender, RoutedEventArgs e)
        {
            string roomName = ((Button)sender).Tag.ToString();
            chatbox_.changeRoom(roomName);
            chatroomName.Text = "Game chat";
            viewHistoryButton.Visibility = Visibility.Hidden;
            quitButton.Visibility = Visibility.Hidden;
        }

        private void changeRoom(object sender, RoutedEventArgs e)
        {
            string roomName = ((DockPanel)sender).Tag.ToString();
            chatroomName.Text = roomName;
            chatbox_.changeRoom(roomName);
            viewHistoryButton.Content = "View Entire Chat History";
            viewHistoryButton.Visibility = Visibility.Visible;
            historyChatbox.Visibility = Visibility.Hidden;
            chatBox.Visibility = Visibility.Visible;
            quitButton.Visibility = roomName != "lobby" ? Visibility.Visible : Visibility.Hidden;
        }

        public void createRoom(object sender, RoutedEventArgs e)
        {
            var text = roomName as TextBox;
            string roomId = text.Text;
            //if (!viewModel.rooms.Contains(roomId))
            //{
            //    viewModel.rooms.Add(roomId);
            //}
            roomCommunication.joinRoom(Account.Instance.username, roomId);
            //chatbox_.changeRoom(roomId);
            //chatroomName.Text = roomId;
            text.Text = "";
            //roomsList.SelectedIndex = roomsList.Items.Count - 1;
        }

        public void joinRoom(object sender, RoutedEventArgs e)
        {
            var roomId = ((Button)sender).Tag.ToString();
            if (!viewModel.rooms.Contains(roomId))
            {
                viewModel.rooms.Add(roomId);
                viewModel.searchedRooms.Remove(roomId);
                chatbox_.addRoom(roomId);
            }
            quitButton.Visibility = roomId != "lobby" ? Visibility.Visible : Visibility.Hidden;
            roomCommunication.joinRoom(Account.Instance.username, roomId);
        }

        public void leaveRoom(object sender, RoutedEventArgs e)
        {
            //leave room
            string roomId = ((TextBlock)chatroomName).Text;
            if (viewModel.rooms.Contains(roomId))
            {
                viewModel.rooms.Remove(roomId);
                viewModel.searchedRooms.Add(roomId);
            }
            roomCommunication.leaveRoom(Account.Instance.username, roomId);
            chatbox_.quitRoom(roomId);
            chatbox_.changeRoom("lobby");
            chatroomName.Text = "lobby";
            quitButton.Visibility = Visibility.Hidden;
        }

        private void onSearchRooms(object sender, KeyEventArgs keyEvent)
        {
            roomCommunication.getRooms();
            var text = searchedRoomName as TextBox;
            string searchedExp = text.Text;
            viewModel.searchedRooms.Clear();
            //new list created, because sometimes allRooms changes and foreach loop blocks
            List<string> allStaticRooms = new List<string>(viewModel.allRooms);
            foreach (string room in allStaticRooms)
            {
                if (room.Contains(searchedExp))
                {
                    viewModel.searchedRooms.Add(room);
                    Console.WriteLine(room);
                }
            }
        }
        public void onOwnRooms(object message)
        {
            try
            {
                String[] ownRooms = JsonConvert.DeserializeObject<string[]>(message.ToString());

                Application.Current.Dispatcher.Invoke((Action)delegate
                {
                    foreach (string room in ownRooms)
                    {
                        if (!viewModel.rooms.Contains(room))
                        {
                            viewModel.rooms.Add(room);
                            chatbox_.addRoom(room);
                            roomCommunication.joinRoom(Account.Instance.username, room);
                        }
                    }
                });
            }
            catch
            {

            }
        }

        public void onAllRooms(object message)
        {
            string[] allExistingRooms = JsonConvert.DeserializeObject<string[]>(message.ToString());
            viewModel.allRooms.Clear();
            foreach (string room in allExistingRooms)
            {
                if (!viewModel.rooms.Contains(room))
                {
                    viewModel.allRooms.Add(room);
                }
            }
        }

        public void onValidRoom(object message)
        {
            string roomId = (string)message;
            Application.Current.Dispatcher.Invoke((Action)delegate
            {
                if (!viewModel.rooms.Contains(roomId))
                {
                    viewModel.rooms.Add(roomId);
                }

                chatbox_.changeRoom(roomId);
                chatbox_.addRoom(roomId);
                chatroomName.Text = roomId;
                roomsList.SelectedIndex = roomsList.Items.Count - 1;

                roomsArea.Visibility = Visibility.Visible;
                joinRoomView.Visibility = Visibility.Hidden;
                chatArea.Visibility = Visibility.Visible;
                createRoomView.Visibility = Visibility.Hidden;
                createRoomViewButton.Visibility = Visibility.Hidden;
                joinRoomViewButton.Visibility = Visibility.Visible;
                joinRoomViewButton.Content = "+ room";
                createRoomViewButton.Content = "Create room";

                quitButton.Visibility = roomId != "lobby" ? Visibility.Visible : Visibility.Hidden;
            });
        }

        public void onInvalidRoom(object message)
        {
            Console.WriteLine("invalid");
        }

        public void onMatchJoined(object message)
        {
            Application.Current.Dispatcher.Invoke((Action)delegate
            {
                // create game room
                MatchMessage matchMessage = JsonConvert.DeserializeObject<MatchMessage>(message.ToString());
                IModeInfo modeInfo = JsonConvert.DeserializeObject<IModeInfo>(matchMessage.content.ToString());
                string roomId = modeInfo.id;
                //if (!viewModel.rooms.Contains(roomId))
                //{
                //    viewModel.rooms.Add(roomId);
                //}
                chatbox_.changeRoom(roomId);
                viewModel.InGameMatch = true;
                Console.WriteLine(viewModel.InGameMatch);
                viewModel.GameId = roomId;
                chatroomName.Text = "Game chat";
                viewHistoryButton.Visibility = Visibility.Hidden;
                quitButton.Visibility = Visibility.Hidden;
                roomsList.UnselectAll();
            });
        }

        public void onLeftMatch(object message)
        {
            Application.Current.Dispatcher.Invoke((Action)delegate
            {
                string roomId = message.ToString();
                // remove game room
                chatbox_.quitRoom(roomId);
                chatbox_.changeRoom("lobby");
                viewModel.InGameMatch = false;
                chatroomName.Text = "lobby";
                viewModel.GameId = "";
                viewHistoryButton.Visibility = Visibility.Visible;
            });
        }

        public void onChatHistory(object message)
        {
            MessageInfo[] matchMessages = JsonConvert.DeserializeObject<MessageInfo[]>(message.ToString());
            if (matchMessages.Length > 0)
            {
                string roomId = matchMessages[0].room;
                Console.WriteLine(roomId);

                viewModel.addHistory(roomId, matchMessages);
            }
            
        }

        private void onViewChatHistory(object sender, RoutedEventArgs keyEvent)
        {
            if(historyChatbox.Visibility == Visibility.Hidden)
            {

                viewModel.ChosenHistoryMessages = viewModel.historyMessages[chatroomName.Text];
                viewHistoryButton.Content = "Return to chat";
                historyChatbox.Visibility = Visibility.Visible;
                chatBox.Visibility = Visibility.Hidden;
            }
            else
            {
                viewHistoryButton.Content = "View Entire Chat History";
                historyChatbox.Visibility = Visibility.Hidden;
                chatBox.Visibility = Visibility.Visible;
            }
        }
    }
}
