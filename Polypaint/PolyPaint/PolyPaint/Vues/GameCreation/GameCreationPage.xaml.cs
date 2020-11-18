using PolyPaint.Modeles;
using PolyPaint.Vues.GameCreation;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;

namespace PolyPaint.Vues
{
    /// <summary>
    /// Interaction logic for GameCreationPage.xaml
    /// </summary>
    public partial class GameCreationPage : Page
    {
        public GameCreationPage()
        {
            InitializeComponent();
            Game.Instance = null;
        }

        private void drawChoice(object sender, RoutedEventArgs e)
        {
            NavigationService ns = NavigationService.GetNavigationService(this);
            ns.Navigate(new Uri("Vues/GameCreation/drawPageCreation.xaml", UriKind.Relative));
        }

        private void assistedChoice(object sender, RoutedEventArgs e)
        {
            NavigationService ns = NavigationService.GetNavigationService(this);
            ns.Navigate(new Uri("Vues/GameCreation/assistedDrawPage.xaml", UriKind.Relative));
        }
    }
}
