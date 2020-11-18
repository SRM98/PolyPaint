using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using PolyPaint.SocketCom;
using Newtonsoft.Json;
using PolyPaint.Modeles;

namespace PolyPaint.ChatComm
{
    class RoomCommunication
    {
        private SocketCommunication socketCommunication;
        public RoomCommunication(List<SubscriptionInfo> subscriptions)
        {
            socketCommunication = SocketCommunication.Instance;
            subscriptions.ForEach((subscription) =>
            {
                socketCommunication.subscribe(subscription.messageType_, subscription.method_);
            });
        }

        public void getOwnRooms()
        {
            socketCommunication.emit("getOwnRooms", Account.Instance.username);
        }
        public void getRooms()
        {
            socketCommunication.emit("getAllRooms", null);
        }

        public void joinRoom(string username, string room)
        {
            RoomJoin roomJoin = new RoomJoin();
            roomJoin.username = username;
            roomJoin.room = room;
            socketCommunication.emit("join", JsonConvert.SerializeObject(roomJoin));
        }

        public void leaveRoom(string username, string room)
        {
            RoomJoin roomJoin = new RoomJoin();
            roomJoin.username = username;
            roomJoin.room = room;
            socketCommunication.emit("leave", JsonConvert.SerializeObject(roomJoin));
        }
    }
}
