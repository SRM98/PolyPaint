using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PolyPaint.Modeles
{
    class Account
    {
        public string firstName { get; set; }
        public string lastName { get; set; }
        public string username { get; set; }
        public string password { get; set; }
        public byte[] imageData { get; set; }
        public string avatarUrl { get; set; }
        private bool firstTimeUser_ { get; set; }
        public Stats stats;
        private static Account instance;
        public Account(string username, string password, string firstName = "", string lastName = "", byte[] imageData = null,
            string avatarUrl = null, Stats stats = null)
        {
            this.firstName = firstName;
            this.lastName = lastName;
            this.username = username;
            this.password = password;
            this.imageData = imageData;
            this.avatarUrl = avatarUrl;
            this.stats = stats;
        }

        public static Account Instance
        {
            get
            {
                if (instance == null)
                    instance = new Account("", "");
                return instance;
            }

            set { instance = value; }
        }
    }
}
