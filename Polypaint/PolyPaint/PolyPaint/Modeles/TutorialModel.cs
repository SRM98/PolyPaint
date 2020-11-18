using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Media;
using System.Windows.Media.Imaging;

namespace PolyPaint.Modeles
{
    class TutorialModel
    {
        public ObservableCollection<SlideInfo> images;
        public TutorialModel()
        {
            string[] files = Directory.GetFiles("..\\..\\Modeles\\TutorialResources", "*.jpg");
            images = new ObservableCollection<SlideInfo>();
            foreach(string file in files){
                string fullPath = Path.GetFullPath(file);
                Uri uri = new Uri(fullPath, UriKind.Absolute);
                ImageSource imgSource = new BitmapImage(uri);

                if (images.Count() == 0)
                {
                    images.Add(new SlideInfo(imgSource, true));
                }
                else
                {
                    images.Add(new SlideInfo(imgSource, false));
                }
            }

        }

    }
}
