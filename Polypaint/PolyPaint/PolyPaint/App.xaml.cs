using System;
using System.Collections.Generic;
using System.Configuration;
using System.Data;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using System.Windows;
using PolyPaint.SocketCom;
using PolyPaint.Vues;

namespace PolyPaint
{
    /// <summary>
    /// Logique d'interaction pour App.xaml
    /// </summary>
    public partial class App : Application
    {
        public App()
        {
            SocketCommunication socket = SocketCommunication.Instance;
            socket.subscribe("disconnect", lostConnection);
            socket.subscribe("reconnect", reconnect);
            socket.subscribe("connection", setSocketId);
        }

        void setSocketId(object data)
        {
            SocketCommunication.Instance.socketId = (string)data;
        }

        void reconnect(object data)
        {
            SocketCommunication.isConnected = true;
        }

        void lostConnection(object data)
        {
            SocketCommunication.isConnected = false;
            Thread t = new Thread(new ThreadStart(checkReconnection));
            t.Start();
        }

        void checkReconnection()
        {
            Task.Delay(8000).Wait();

            if (!SocketCommunication.isConnected)
            {
                SocketCommunication.Instance.socketId = "";
                this.Dispatcher.Invoke(() => Application.Current.MainWindow.Content = new LostConnectionPage());
            }
        }
    }
}
