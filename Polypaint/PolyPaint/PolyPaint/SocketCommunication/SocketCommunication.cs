using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Quobject.SocketIoClientDotNet.Client;
using Newtonsoft.Json;

namespace PolyPaint.SocketCom
{
    public class SocketCommunication
    {
        public Socket socket;
        public string socketId;
        private static SocketCommunication instance;
        public static bool isConnected { get; set; }
        private Dictionary<string, List<Action<object>>> roomCallbacks;
        private SocketCommunication() {
            this.socket = IO.Socket(MainWindow.ServerIP);
            roomCallbacks = new Dictionary<string, List<Action<object>>>();
        }

        public static SocketCommunication Instance
        {
            get
            {
                if (instance == null)
                    instance = new SocketCommunication();
                return instance;
            }
        }

        public void subscribe(string messageType, Action<object> callback)
        {
            if (!roomCallbacks.ContainsKey(messageType))
            {
                roomCallbacks.Add(messageType, new List<Action<object>>());
            }
            List<Action<object>> allCallbacks = roomCallbacks[messageType];
            if (!allCallbacks.Contains(callback))
            {
                allCallbacks.Add(callback);
                roomCallbacks[messageType] = allCallbacks;
            }
            this.socket.On(messageType, (data) => {
                callCallbacks(data, allCallbacks);
            });
        }

        public void subscribeOnce(string messageType, Action<object> callback)
        {

            roomCallbacks.Remove(messageType);
            roomCallbacks.Add(messageType, new List<Action<object>>() { callback });

            this.socket.Off(messageType);
            this.socket.On(messageType, (data) => {
                callback(data);
            });
        }

        public void callCallbacks(object data, List<Action<object>> callbacks)
        {
            callbacks.ForEach((call) =>
            {
                if (call != null)
                    call(data);
            });
        }

        public void emit(string messageType, object data) {
                socket.Emit(messageType, data);
        }

        public void clearRoomCallbacks()
        {
            roomCallbacks.Clear();
        }
    }
}
