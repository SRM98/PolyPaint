using PolyPaint.ChatComm;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PolyPaint.VueModeles
{
    class RoomsViewModel: INotifyPropertyChanged
    {
        public ObservableCollection<string> rooms;
        public ObservableCollection<string> allRooms;
        public ObservableCollection<string> searchedRooms;
        public Dictionary<string, MessageInfo[]> historyMessages;
        private MessageInfo[] chosenHistoryMessages;
        private bool inGameMatch;
        private string gameId;

        public RoomsViewModel()
        {
            rooms = new ObservableCollection<string>();
            allRooms = new ObservableCollection<string>();
            InGameMatch = false;
            searchedRooms = new ObservableCollection<string>();
            historyMessages = new Dictionary<string, MessageInfo[]>();
            ChosenHistoryMessages = new MessageInfo[0];
        }
        private void NotifyPropertyChanged(string info)
        {
            if (PropertyChanged != null)
            {
                PropertyChanged(this, new PropertyChangedEventArgs(info));
            }
        }
        public event PropertyChangedEventHandler PropertyChanged;

        public bool InGameMatch
        {
            get
            {
                return inGameMatch;
            }

            set
            {
                inGameMatch = value;
                NotifyPropertyChanged("InGameMatch");
            }
        }

        public string GameId
        {
            get
            {
                return gameId;
            }

            set
            {
                gameId = value;
                NotifyPropertyChanged("GameId");
            }
        }

        public MessageInfo[] ChosenHistoryMessages
        {
            get
            {
                return chosenHistoryMessages;
            }

            set
            {
                foreach(MessageInfo messageInfo in value){
                    if (messageInfo.senderAvatarUrl == null)
                        messageInfo.senderAvatarUrl = "/Resources/defaultAvatar.png";
                    else
                    {
                        messageInfo.senderAvatarUrl = MainWindow.ServerIP + messageInfo.senderAvatarUrl;
                    }
                }
                chosenHistoryMessages = value;
                NotifyPropertyChanged("ChosenHistoryMessages");
            }
        }

        public void addHistory(string roomId, MessageInfo[] messages)
        {
            if(!historyMessages.ContainsKey(roomId)) historyMessages.Add(roomId, messages);
        }
    }
}
