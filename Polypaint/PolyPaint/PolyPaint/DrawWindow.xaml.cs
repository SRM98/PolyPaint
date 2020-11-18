using PolyPaint.Vues;
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
    /// Logique d'interaction pour DrawWindow.xaml
    /// </summary>
    public partial class DrawWindow : Window
    {
        public DrawingPage drawingArea;
        public DrawWindow()
        {
            InitializeComponent();
            DrawingPage.instance.ClearCanvas();
            drawingArea = DrawingPage.instance;
            DrawingPage.instance.isDrawingAvatar = true;
            DrawCom.DrawCommunication.CommunicationEnabled = false;
            Content = drawingArea;
        }
    }
}
