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
using System.Windows.Media.Imaging;
using System.IO;

public class MessageInfo
{
    public string room { get; set; }
    public string text { get; set; }
    public string sender { get; set; }
    public string senderIsMe { get; set; }
    public long date { get; set; }
    public string time { get; set; }
    public string senderAvatarUrl { get; set; }
}

public struct SubscriptionInfo
{
    public string messageType_;
    public Action<object> method_;
    public SubscriptionInfo(string messageType, Action<object> method)
    {
        messageType_ = messageType;
        method_ = method;
    }
}

namespace PolyPaint.Vues
{
    /// <summary>
    /// Interaction logic for Window1.xaml
    /// </summary>
    public partial class ChatBox : Page
    {
        private ChatCommunication chatCommunication;
        private string messageToSend;
        private Dictionary<string, ObservableCollection<MessageInfo>> roomsMessages;
        private string roomId = "lobby";
        private string senderId;
        public ChatBox(string room)
        {
            InitializeComponent();
            this.roomId = room;
            List<SubscriptionInfo> subscriptions = new List<SubscriptionInfo>();
            subscriptions.Add(new SubscriptionInfo("message", onNewMessage));
            //subscriptions.Add(new SubscriptionInfo("roomMessages", onChatHistory));
            subscriptions.Add(new SubscriptionInfo("badWord", onBadWordAlert));
            chatCommunication = new ChatCommunication(subscriptions);
            roomsMessages = new Dictionary<string, ObservableCollection<MessageInfo>>();
            roomsMessages.Add(room, new ObservableCollection<MessageInfo>());
            messagesList.ItemsSource = roomsMessages[this.roomId];
            senderId = Account.Instance.username;

            Console.WriteLine("chatCreated");
        }

        public void onChatHistory(object message)
        {
            Console.WriteLine("received history");
        }

        public void onNewMessage(object message)
        {
            MessageInfo dMessage = JsonConvert.DeserializeObject<MessageInfo>(message.ToString());
            if (dMessage.senderAvatarUrl == null)
                dMessage.senderAvatarUrl = "/Resources/defaultAvatar.png";
            else
            {
                dMessage.senderAvatarUrl = MainWindow.ServerIP + dMessage.senderAvatarUrl;
                Console.WriteLine(dMessage.senderAvatarUrl);
            }

            if (roomsMessages.ContainsKey(dMessage.room))
            {
                if (Account.Instance.username == dMessage.sender)
                {
                    dMessage.senderIsMe = "true";
                }
                else
                {
                    dMessage.senderIsMe = "false";
             
                }
                Console.WriteLine(dMessage.room);
                // Needs this weird invoking function so that i can create TextBlock...
                Application.Current.Dispatcher.Invoke((Action)delegate {
                    roomsMessages[dMessage.room].Add(dMessage);
                    Console.WriteLine(dMessage.date);
                    var datetime = new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc).AddMilliseconds(dMessage.date).ToLocalTime();
                    dMessage.time = datetime.ToString("HH:mm:ss");
                    _scrollViewer.ScrollToEnd();
                });
            }
        }

        private void enterMessage(object sender, KeyEventArgs keyEvent)
        {
            if (keyEvent.Key == Key.Return)
            {
                sendMessage(sender, keyEvent);
            }
        }
   
        private void sendMessage(object sender, RoutedEventArgs e)
        {
            var text = textBoxMessage as TextBox;
            messageToSend = text.Text;
            MessageInfo messageInfo = new MessageInfo();
            messageInfo.text = messageToSend;
            messageInfo.room = roomId;
            messageInfo.sender = senderId;
            chatCommunication.sendMessage(messageInfo);
            text.Text = "";
            text.Focus();
        }

        public void changeRoom(string roomId)
        {
            this.addRoom(roomId);
            this.roomId = roomId;
            messagesList.ItemsSource = roomsMessages[this.roomId];
        }

        public void quitRoom(string roomId)
        {
            if (roomsMessages.ContainsKey(roomId))
            {
                roomsMessages.Remove(roomId);
            }
        }

        public void addRoom(string roomId)
        {
            if (!roomsMessages.ContainsKey(roomId))
            {
                roomsMessages.Add(roomId, new ObservableCollection<MessageInfo>());
            }
        }

        private void onBadWordAlert(object message)
        {
            MessageBox.Show("Mauvais mot détecté.", "Mauvais mot détecté", MessageBoxButton.OK, MessageBoxImage.Warning);
        }
    }
}
