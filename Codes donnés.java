/*========================================================================
    Voici comment ouvrir une image PNG et garder dans un tableau int[] 
      les pixels de toute l'image s'il s'agit d'une image couleur :
========================================================================*/

String fn;                                          // fn is a String with the filename
RenderedOp ropimage;                                // a Rendered Operation object will contain the metadata and data
ropimage = JAI.create("fileload", fn);              // open the file
BufferedImage bi = ropimage.getAsBufferedImage();   // BufferedImage will contain the data
IMG_WIDTH = ropimage.getWidth();
IMG_HEIGHT = ropimage.getHeight();
ColorModel cm = ropimage.getColorModel();           // get the color mode
if (cm.getColorSpace().getType() == ColorSpace.TYPE_RGB) {
    int[] pxc = bi.getRGB(0, 0, IMG_WIDTH, IMG_HEIGHT, null, 0, IMG_WIDTH);
}




/*=========================================================================
    Voici comment ouvrir une image PNG et garder dans un tableau byte[] 
  les pixels de toute l'image s'il s'agit d'une image en niveau de gris :
=========================================================================*/

if ((bi.getType() == BufferedImage.TYPE_BYTE_GRAY) && 
        (cm.getColorSpace().getType() == ColorSpace.TYPE_GRAY)) {
    Raster r = ropimage.getData();                              // from the Raster object we retrieve first a DataBufferByte
    DataBufferByte db = (DataBufferByte) (r.getDataBuffer());   // then the real array of bytes
    byte[] pxgs = db.getData();
}




/*=========================================================================
          Voici comment sauvegarder une image PNG en couleurs :
=========================================================================*/

int[] pxc;
DataBufferInt dataBuffer = new DataBufferInt(pxc, pxc.length);
int samplesPerPixel = 4;                                                                // we have 4 channels: aRGB
ColorModel colorModel = new DirectColorModel(32, 0xFF0000, 0xFF00, 0xFF, 0xFF000000);   // pixels written 32 bits (R G B a)
WritableRaster raster = Raster.createPackedRaster(dataBuffer, IMG_WIDTH, IMG_HEIGHT, 
    IMG_WIDTH, ((DirectColorModel) colorModel).getMasks(), null);                       // start writing the data at the beginning
BufferedImage image = new BufferedImage(colorModel, raster, 
    colorModel.isAlphaPremultiplied(), null);                                           // combine all in a BufferedImage object
RenderedOp op1 = JAI.create("filestore", image, "res.png", "png");                      // saves the image in a file called res-c.png




/*=========================================================================
        Voici comment sauvegarder une image PNG en niveau de gris :
=========================================================================*/

byte[] pxgs;
SampleModel sm = RasterFactory.createBandedSampleModel(DataBuffer.TYPE_BYTE, IMG_WIDTH, IMG_HEIGHT, 1); // only one band (no big surprise here)
BufferedImage image = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_BYTE_GRAY);           // it is a TYPE_BYTE_GRAY image
image.setData(Raster.createRaster(sm, new DataBufferByte(pxgs, pxgs.length), new Point()));             // take the data from pxg
JAI.create("filestore", image, "res-gs.png", "PNG");                                                    // saves the image in a file called res-gs.png




/*=========================================================================
                Voici un test pour le décalage de bits :
=========================================================================*/

short x = 258;
short res = (short) (x & 0xFF00);
System.out.println("res = " + res);
System.out.printf("res = %d%n", res);       // res en décimal
System.out.printf("res = %04X%n", res);     // res en hexadecimal avec autant de zéros que nécessaire pour l'avoir sur 4 chiffres
res = (short) (res >> 8);                   // déplacement vers la droite (pour un déplacement vers la gauche, utilisez "<<")
System.out.println("res = " + res);
System.out.printf("res = %d%n", res);
System.out.printf("res = %04X%n", res);

/* Résultat attendu :
        res = 256
        res = 256
        res = 0100
        res = 1
        res = 1
        res = 0001
/*