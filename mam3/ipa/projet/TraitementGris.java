package mam3.ipa.projet;

//----------[Imports]----------
import java.awt.Point;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.media.jai.RasterFactory;
import javax.media.jai.RenderedOp;
import javax.media.jai.JAI;


public class TraitementGris extends TraitementImage {

	//----------[Attributes]----------

    private int[] matHistogram = new int[512];

    /**
 	 * pxgs will contain the grayscale value of every pixel
 	 */
	private byte[] pxgs;


	//----------[Constructor]----------

	public TraitementGris(String s) {
		super(s);
	}


	//----------[Methods]----------

	// Getter

	/**
 	 * @return pxgs
 	 */
	public byte[] getPXGS() {
		return this.pxgs;
	}

	// Setter

	/**
 	 * Sets pxgs
 	 */
	public void setPXGS(byte[] b) {
		this.pxgs = b;
	}

	// Others

	public void load(RenderedOp ro) {
		super.setBI(ro.getAsBufferedImage());
		super.setWidth(ro.getWidth());
		super.setHeight(ro.getHeight());
		ColorModel cm = ro.getColorModel();
		if ((super.getBI().getType() == BufferedImage.TYPE_BYTE_GRAY) && 
        		(cm.getColorSpace().getType() == ColorSpace.TYPE_GRAY)) {
    		Raster r = ro.getData();
    		DataBufferByte db = (DataBufferByte) (r.getDataBuffer());
    		this.pxgs = db.getData();
		}
	}

	public void save(String name) {
		SampleModel sm = RasterFactory.createBandedSampleModel(
				DataBuffer.TYPE_BYTE, super.getWidth(), super.getHeight(), 1);
		BufferedImage image = new BufferedImage(super.getWidth(), super.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		image.setData(Raster.createRaster(sm, new DataBufferByte(this.pxgs, this.pxgs.length), new Point()));
		JAI.create("filestore", image, super.getParentPath()+name+".png", "PNG"); 
		super.changePathWithFileName(name);
	}

	public TraitementImage grayscale() {
		return this;
	}

	public void contrast() {
		String[] lines = {"0\t0\t0", "0\t-1\t0", "0\t0\t0"};
		MatrixOperator.createMatrix(lines, super.getParentPath());
		this.setPXGS(TraitementImage.intsToBytes(MatrixOperator.convolution(
			super.getParentPath(), 
			TraitementImage.bytesToInts(this.getPXGS()), 
			super.getWidth(), 
			super.getHeight(), 
			1, 
			255)));
	}

	public void darkening() {
		String[] lines = {"0\t0\t0", "0\t4\t0", "0\t0\t0"};
		MatrixOperator.createMatrix(lines, super.getParentPath());
		this.setPXGS(TraitementImage.intsToBytes(MatrixOperator.convolution(
			super.getParentPath(), 
			TraitementImage.bytesToInts(this.getPXGS()), 
			super.getWidth(), 
			super.getHeight(), 
			5, 
			0)));
	}

	public void lightening() {
		String[] lines = {"0\t0\t0", "0\t5\t0", "0\t0\t0"};
		MatrixOperator.createMatrix(lines, super.getParentPath());
		this.setPXGS(TraitementImage.intsToBytes(MatrixOperator.convolution(
			super.getParentPath(), 
			TraitementImage.bytesToInts(this.getPXGS()), 
			super.getWidth(), 
			super.getHeight(), 
			4, 
			0)));
	}

	public void histogram() {
		for (int i = 0; i < 256; i++) {
			this.matHistogram[2*i] = i;
			this.matHistogram[2*i+1] = 0;
		}
		int c = 0;
		int l = this.pxgs.length;
		for (int j = 0; j < l; j++) {
			c = TraitementImage.byteToInt(this.pxgs[j]);
			this.matHistogram[2*c+1]++;
		}
		String f = super.getParentPath() + super.getFilename() + "-h.txt";
		try (BufferedWriter bfwt = new BufferedWriter(new FileWriter(new File(f)))) {
			for (int k = 0; k < 256; k++){
				bfwt.write(String.valueOf(this.matHistogram[2*k]));
				bfwt.write("\t");
				bfwt.write(String.valueOf(this.matHistogram[2*k+1]));
				bfwt.newLine();
			}
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}

} 