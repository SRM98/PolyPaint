using PolyPaint.Modeles;
using PolyPaint.VueModeles;
using System;
using System.Windows.Controls;


namespace PolyPaint.Vues.Tutorial
{
    /// <summary>
    /// Interaction logic for Page1.xaml
    /// </summary>
    public partial class TutorialView : Page
    {
        public TutorialViewModel viewModel { get; set; }
        public TutorialView()
        {

            InitializeComponent();
            viewModel = new TutorialViewModel();
            this.DataContext = viewModel;
        }
        private void onPreviousSlide(object sender, EventArgs e)
        {
            viewModel.onPreviousSlide();
        }

        private void onNextSlide(object sender, EventArgs e)
        {
            viewModel.onNextSlide();
        }
  
        private void onExit(object sender, EventArgs e)
        {
            this.Visibility = System.Windows.Visibility.Hidden;
        }

        public void changeMode(Modes mode)
        {
            viewModel.changeMode(mode);
        }
        
    }
}
