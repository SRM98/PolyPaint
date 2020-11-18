using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PolyPaint.Modeles
{
    class EditRequest
    {
        private string username_;
        private object dataToEdit_;

        public EditRequest()
        {
        }
        public EditRequest(object data)
        {
            this.username_ = Account.Instance.username;
            this.dataToEdit_ = data;
        }

        public string username
        {
            get { return username_; }
        }

        public object dataToEdit
        {
            get { return dataToEdit_; }
        }
    }
}
