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
using PolyPaint.Modeles;
using System.Windows.Input.StylusPlugIns;
using System.Windows.Ink;
using PolyPaint.VuesModeles;
using System.Drawing;
using Microsoft.Win32;
using System.Drawing.Imaging;

namespace PolyPaint.Vues
{

    /// <summary>
    /// Interaction logic for Page2.xaml
    /// </summary>
    /// 
    public partial class DrawingPage : Page
    {
        public bool isDrawingAvatar { get; set; }
        public static DrawingPage instance = new DrawingPage();
        public DrawingPage(bool isDrawingAvatar = false)
        {
            InitializeComponent();
            this.isDrawingAvatar = isDrawingAvatar;
            DataContext = new VueModele();
        }

        // Pour gérer les points de contrôles.
        private void GlisserCommence(object sender, DragStartedEventArgs e) => (sender as Thumb).Background = System.Windows.Media.Brushes.Black;
        private void GlisserTermine(object sender, DragCompletedEventArgs e) => (sender as Thumb).Background = System.Windows.Media.Brushes.White;
        private void GlisserMouvementRecu(object sender, DragDeltaEventArgs e)
        {
            String nom = (sender as Thumb).Name;
            if (nom == "horizontal" || nom == "diagonal") colonne.Width = new GridLength(Math.Max(32, colonne.Width.Value + e.HorizontalChange));
            if (nom == "vertical" || nom == "diagonal") ligne.Height = new GridLength(Math.Max(32, ligne.Height.Value + e.VerticalChange));
        }

        // Pour la gestion de l'affichage de position du pointeur.

        private void event_DefaultDrawingAttributesReplaced(object sender, DrawingAttributesReplacedEventArgs e)
        {
            DrawEvents.myDrawingAttributes = e.NewDrawingAttributes;
        }

        private void chooseEraser(object sender, RoutedEventArgs e)
        {
            this.surfaceDessin.UseCustomCursor = true;
        }

        private void choosePen(object sender, RoutedEventArgs e)
        {
            this.surfaceDessin.UseCustomCursor = false;
        }

        private void chooseStrokeEraser(object sender, RoutedEventArgs e)
        {
            this.surfaceDessin.UseCustomCursor = false;
        }

        private void SupprimerSelection(object sender, RoutedEventArgs e) => surfaceDessin.CutSelection();

        public byte[] getImageData()
        {
            //get the dimensions of the ink control
            int margin = (int)this.surfaceDessin.Margin.Left;
            int width = (int)this.surfaceDessin.ActualWidth - margin;
            int height = (int)this.surfaceDessin.ActualHeight - margin;
            //render ink to bitmap
            RenderTargetBitmap rtb =
            new RenderTargetBitmap(width, height, 96d, 96d, PixelFormats.Default);
            rtb.Render(surfaceDessin);
            //save the ink to a memory stream
            BmpBitmapEncoder encoder = new BmpBitmapEncoder();
            encoder.Frames.Add(BitmapFrame.Create(rtb));
            byte[] imageData;
            using (MemoryStream ms = new MemoryStream())
            {
                encoder.Save(ms);
                //get the bitmap bytes from the memory stream
                ms.Position = 0;
                imageData = ms.ToArray();
            }
            return imageData;
        }

        private void saveImage(object sender, RoutedEventArgs e)
        {
            if (!isDrawingAvatar)
            {
                byte[] imageData = getImageData();

                ImageConverter ic = new ImageConverter();

                System.Drawing.Image img = (System.Drawing.Image)ic.ConvertFrom(imageData);

                Bitmap bitmap = new Bitmap(img);

                SaveFileDialog dialog = new SaveFileDialog();
                dialog.FileName = "image";
                dialog.Filter = "JPeg Image|*.jpg";
                dialog.AddExtension = true;
                if (dialog.ShowDialog() == true)
                {
                    bitmap.Save(dialog.FileName, ImageFormat.Jpeg);
                }
                isDrawingAvatar = false;
            }
        }
           

        void StrokeDrawn(object sender, InkCanvasGestureEventArgs e)
        {
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

        public void ClearCanvas()
        {
            this.surfaceDessin.Strokes = new System.Windows.Ink.StrokeCollection();
            DrawCommunication.ZIndex = 0;
        }

        public void DisableDraw()
        {
            this.IsHitTestVisible = false;
            this.DrawPanel.Visibility = Visibility.Collapsed;
        }

        public void DisableComm()
        {
            DrawCommunication.CommunicationEnabled = false;
        }

        public void EnableComm()
        {
            DrawCommunication.CommunicationEnabled = false;
        }

        public void EnableDraw()
        {
            this.IsHitTestVisible = true;
            this.DrawPanel.Visibility = Visibility.Visible;
        }

        private void Page_Loaded(object sender, RoutedEventArgs e)
        {
            DrawEvents.width = (int)this.ActualWidth;
            DrawEvents.height = (int)this.ActualHeight;
        }
    }
}
