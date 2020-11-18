using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Media;

namespace PolyPaint.Modeles
{
    public class SlideInfo: INotifyPropertyChanged
    {
        public ImageSource imageUrl { get; set; }
        private bool showImage { get; set; }
        public bool ShowImage
        {
            get
            {
                return showImage;
            }

            set
            {
                showImage = value;
                NotifyPropertyChanged("ShowImage");
            }
        }

        public SlideInfo(ImageSource imageUrl, bool showImage)
        {
            this.imageUrl = imageUrl;
            this.showImage = showImage;
        }

        private void NotifyPropertyChanged(string info)
        {
            if (PropertyChanged != null)
            {
                PropertyChanged(this, new PropertyChangedEventArgs(info));
            }
        }

        public event PropertyChangedEventHandler PropertyChanged;
    }
}
