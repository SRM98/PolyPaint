using PolyPaint.Modeles;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PolyPaint.VueModeles
{
    public class TutorialViewModel : INotifyPropertyChanged
    {
        private bool showNextButton;
        private bool showPreviousButton;
        private int slideNumber;
        private static int nbOfSlides;
        public const int CLASSIC_INDEX = 9;
        public const int COOP_INDEX = 10;
        public const int DUEL_INDEX = 11;
        public const int SOLO_INDEX = 13;
        private TutorialModel model;

        public TutorialViewModel()
        {
            slideNumber = 0;
            showNextButton = true;
            showPreviousButton = false;
            model = new TutorialModel();
            nbOfSlides = model.images.Count();
        }

        public bool ShowNextButton
        {
            get
            {
                return showNextButton;
            }

            set
            {
                showNextButton = value;
                NotifyPropertyChanged("ShowNextButton");
            }
        }

        public bool ShowPreviousButton
        {
            get
            {
                return showPreviousButton;
            }

            set
            {
                showPreviousButton = value;
                NotifyPropertyChanged("ShowPreviousButton");
            }
        }

        public ObservableCollection<SlideInfo> Images
        {
            get { return model.images; }
            set { model.images = value; NotifyPropertyChanged("Images"); }
        }

        public void onPreviousSlide()
        {
            Images[slideNumber].ShowImage = false;
            slideNumber--;
            Images[slideNumber].ShowImage = true;
            ShowNextButton = true;
            if (slideNumber == 0) ShowPreviousButton = false;
            NotifyPropertyChanged("Images");
        }

        public void onNextSlide()
        {
            Images[slideNumber].ShowImage = false;
            slideNumber++;
            Images[slideNumber].ShowImage = true;
            ShowPreviousButton = true;
            if (slideNumber == nbOfSlides - 1) ShowNextButton = false;
            NotifyPropertyChanged("Images");
        }

        public void changeMode(Modes mode)
        {
            Images[slideNumber].ShowImage = false;
            switch (mode)
            {
                case Modes.Classic:
                    slideNumber = CLASSIC_INDEX;
                    break;
                case Modes.Coop:
                    slideNumber = COOP_INDEX;
                    break;
                case Modes.Duel:
                    slideNumber = DUEL_INDEX;
                    break;
                case Modes.Solo:
                    slideNumber = SOLO_INDEX;
                    break;
            }
            Images[slideNumber].ShowImage = true;
            ShowPreviousButton = true;
            if (slideNumber == 0)
            {
                ShowPreviousButton = false;
                showNextButton = true;
            }
            if (slideNumber == nbOfSlides - 1)
            {
                ShowNextButton = false;
                showPreviousButton = true;
            }
            NotifyPropertyChanged("Images");
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
