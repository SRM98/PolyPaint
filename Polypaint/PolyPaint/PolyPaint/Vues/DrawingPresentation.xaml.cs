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
using PolyPaint.VueModeles;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using System.Windows.Controls.Primitives;
using System.IO;
using System.Windows.Markup;
using System.Xml.Linq;
using PolyPaint.Convertisseurs;
using PolyPaint.DrawCom;
using System.Windows.Input.StylusPlugIns;
using System.Windows.Ink;
using PolyPaint.VuesModeles;

namespace PolyPaint.Vues
{

    /// <summary>
    /// Interaction logic for Page2.xaml
    /// </summary>
    /// 
    public partial class DrawingPresentation : Page
    {
        public DrawingPresentation()
        {
            InitializeComponent();
            DataContext = new VueModele();
        }

        // Pour gérer les points de contrôles.
        private void GlisserCommence(object sender, DragStartedEventArgs e) => (sender as Thumb).Background = Brushes.Black;
        private void GlisserTermine(object sender, DragCompletedEventArgs e) => (sender as Thumb).Background = Brushes.White;
        private void GlisserMouvementRecu(object sender, DragDeltaEventArgs e)
        {
            String nom = (sender as Thumb).Name;
            if (nom == "horizontal" || nom == "diagonal") colonne.Width = new GridLength(Math.Max(32, colonne.Width.Value + e.HorizontalChange));
            if (nom == "vertical" || nom == "diagonal") ligne.Height = new GridLength(Math.Max(32, ligne.Height.Value + e.VerticalChange));
        }

        // Pour la gestion de l'affichage de position du pointeur.
        private void surfaceDessin_MouseLeave(object sender, MouseEventArgs e) => textBlockPosition.Text = "";
        private void surfaceDessin_MouseMove(object sender, MouseEventArgs e)
        {
            Point p = e.GetPosition(surfaceDessin);
            textBlockPosition.Text = Math.Round(p.X) + ", " + Math.Round(p.Y) + "px";
        }

        private void event_DefaultDrawingAttributesReplaced(object sender, DrawingAttributesReplacedEventArgs e)
        {
            DrawEvents.myDrawingAttributes = e.NewDrawingAttributes;
        }

        private void SupprimerSelection(object sender, RoutedEventArgs e) => surfaceDessin.CutSelection();

        private void Save(object sender, RoutedEventArgs e)
        {
            using (FileStream fs = new FileStream("inkstrokes.isf", FileMode.Create))
            {
                surfaceDessin.Strokes.Save(fs);
                fs.Close();
            }
        }
        void StrokeDrawn(object sender, InkCanvasGestureEventArgs e)
        {
            Console.WriteLine("test");
            //Object message = InkCanvasConverter.ToSVG(e.Stroke);
            //DrawCommunication.draw(message);
        }
        private void Load(object sender, RoutedEventArgs e)
        {
            FileStream fs = new FileStream("inkstrokes.isf", FileMode.Open, FileAccess.Read);
            System.Windows.Ink.StrokeCollection strokes = new System.Windows.Ink.StrokeCollection(fs);
            surfaceDessin.Strokes = strokes;
            fs.Close();
        }
    }
}
