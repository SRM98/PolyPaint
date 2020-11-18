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
using System.Linq;

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
    /// 
    public class SoloMatchMessage
    {
        public Modes type { get; set; }
        public Object content { get; set; }
        public Object match { get; set; }
    }


    public partial class CreateSolo : Page
    {
        public string matchCreatedId = "";
        public CreateSolo()
        {
            InitializeComponent();
            SocketCommunication.Instance.subscribe("matchStarted", onMatchStarted);
            LabelWait.Visibility = Visibility.Collapsed;
            ButtonStart.Visibility = Visibility.Visible;
        }

        private void StartSoloMatch(object sender, RoutedEventArgs e)
        {
            LabelWait.Visibility = Visibility.Visible;
            ButtonStart.Visibility = Visibility.Collapsed;

            IModeInfo game = new IModeInfo (){};
            game.name = Account.Instance.username + "'s game";
            game.creator = Account.Instance.username;
            game.type = Modes.Solo;
            matchCreatedId = game.name + (int)game.type;
            SocketCommunication.Instance.emit("createMatch", JsonConvert.SerializeObject(game));
        }

        private void onMatchStarted(Object messageJson)
        {
            Application.Current.Dispatcher.Invoke((Action)delegate {
                SoloMatchMessage message = JsonConvert.DeserializeObject<SoloMatchMessage>(messageJson.ToString());
                IModeInfo match = JsonConvert.DeserializeObject<IModeInfo>(message.match.ToString());

                if (match.id == matchCreatedId)
                    Application.Current.MainWindow.Content = new InMatch(match);
            });
        }
    }
}
