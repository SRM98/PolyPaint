using PolyPaint.VueModeles;
using PolyPaint.Vues;
using PolyPaint.Modeles;
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
using System.Windows.Shapes;

namespace PolyPaint
{
    /// <summary>
    /// Logique d'interaction pour MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    { 
        //public static readonly string ServerIP = "http://localhost:4200/";
        public static readonly string ServerIP = "http://18.188.161.175:4200/";
        public static bool soundActivated { set; get; }

        public MainWindow()
        {
            InitializeComponent();
            soundActivated = true;
            Content = new LoginPage();
        }
    }
}
