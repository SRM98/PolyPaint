using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PolyPaint.Modeles
{
    class Credentials
    {
        private string username_;
        private string password_;
        private string socketId_;

        public Credentials()
        {
        }

        public Credentials(string username, string password, string socketId)
        {
            this.username_ = username;
            this.password_ = password;
            this.socketId_ = socketId;
        }

        public string username
        {
            get { return username_; }
        }

        public string password
        {
            get { return password_; }
        }
        public string socketId
        {
            get { return socketId_; }
        }
    }

    
}
