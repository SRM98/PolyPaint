using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PolyPaint.Modeles
{
    class Message
    {
        private string text_;
        private string sender_;
        private string time_;
        private string room_;

        public Message()
        {
        }

        public Message(string text, string sender, string time, string room)
        {
            this.text_ = text;
            this.sender_ = sender;
            this.time_ = time;
            this.room_ = room;
        }

        public string text
        {
            get { return text_; }
        }

        public string sender
        {
            get { return sender_; }
        }

        public string time
        {
            get { return time_; }
        }

        public string room
        {
            get { return room_; }
        }





    }
}
