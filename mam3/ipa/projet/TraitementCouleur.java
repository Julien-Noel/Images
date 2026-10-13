package mam3.ipa.projet;

//----------[Imports]----------
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.ParameterBlock;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;


public class TraitementCouleur extends TraitementImage {

	//----------[Attributes]----------
	
    private int[] matHistogram = new int[1024];

    /**
 	 * pxc will contain the αRGB values of every pixel
 	 */
	private int[] pxc;


	//----------[Constructor]----------

	public TraitementCouleur(String s) {
		super(s);
	}


	//----------[Methods]----------

	// Getters

	/**
 	 * @return pxc
 	 */
	public int[] getPXC() {
		return this.pxc;
	}

	/**
	 * @return the red value of the (i+1)-th pixel of pxc
	 */
	public int getRC(int i) {
		return ((int) ((this.pxc[i] & 0xFF0000) >> 16));
	}

	/**
	 * @return an array of integers containing only red values
	 */
	public int[] getRC() {
		int l = this.pxc.length;
		int[] t = new int[l];
		for (int i = 0; i < l; i++) {
			t[i] = this.getRC(i);
		}
		return t;
	}

	/**
	 * @return the green value of the (i+1)-th pixel of pxc
	 */
	public int getGC(int i) {
		return ((int) ((this.pxc[i] & 0xFF00) >> 8));
	}

	/**
	 * @return an array of integers containing only green values
	 */
	public int[] getGC() {
		int l = this.pxc.length;
		int[] t = new int[l];
		for (int i = 0; i < l; i++) {
			t[i] = this.getGC(i);
		}
		return t;
	}

	/**
	 * @return the blue value of the (i+1)-th pixel of pxc
	 */
	public int getBC(int i) {
		return ((int) (this.pxc[i] & 0xFF));
	}

	/**
	 * @return an array of integers containing only blue values
	 */
	public int[] getBC() {
		int l = this.pxc.length;
		int[] t = new int[l];
		for (int i = 0; i < l; i++) {
			t[i] = this.getBC(i);
		}
		return t;
	}

	// Setters

	/**
 	 * Sets pxc
 	 */
	public void setPXC(int[] t) {
		this.pxc = t;
	}

	/**
	 * Sets the red values only
	 */
	public void setRC(int[] t) {
		for (int i = 0; i < Math.min(t.length, this.pxc.length); i++) {
			this.pxc[i] = (int) ((this.pxc[i] & 0xFF00FFFF) + ((t[i] & 0xFF) << 16));
		}
	}

	/**
	 * Sets the green values only
	 */
	public void setGC(int[] t) {
		for (int i = 0; i < Math.min(t.length, this.pxc.length); i++) {
			this.pxc[i] = (int) ((this.pxc[i] & 0xFFFF00FF) + ((t[i] & 0xFF) << 8));
		}
	}

	/**
	 * Sets the blue values only
	 */
	public void setBC(int[] t) {
		for (int i = 0; i < Math.min(t.length, this.pxc.length); i++) {
			this.pxc[i] = (int) ((this.pxc[i] & 0xFFFFFF00) + (t[i] & 0xFF));
		}
	}

	// Others

	public void load(RenderedOp ro) {
		super.setBI(ro.getAsBufferedImage());
		super.setWidth(ro.getWidth());
		super.setHeight(ro.getHeight());
		ColorModel cm = ro.getColorModel();
		if (cm.getColorSpace().getType() == ColorSpace.TYPE_RGB) {
    		this.pxc = super.getBI().getRGB(0, 0, super.getWidth(), super.getHeight(), null, 0, super.getWidth());
    	}
	}

	public void save(String name) {
		DataBufferInt dataBuffer = new DataBufferInt(this.pxc, this.pxc.length);
		ColorModel cm = new DirectColorModel(32, 0xFF0000, 0xFF00, 0xFF, 0xFF000000);
		WritableRaster raster = Raster.createPackedRaster(dataBuffer, super.getWidth(), 
			super.getHeight(), super.getWidth(), ((DirectColorModel) cm).getMasks(), null);
		BufferedImage image = new BufferedImage(cm, raster, cm.isAlphaPremultiplied(), null);
		RenderedOp ro = JAI.create("filestore", image, super.getParentPath()+name+".png", "png");
		super.changePathWithFileName(name);
	}

	public TraitementImage grayscale(int cR, int cG, int cB) {
		int sum = cR + cG + cB;
		int sumRounded = TraitementImage.roundTens(sum);
		if ((sum != sumRounded) || (sum == 0) || (sumRounded == 0)) {
			cR = 30;
			cG = 59;
			cB = 11;
			sum = 100;
		}
		int l = this.pxc.length;
		byte[] b = new byte[l];
		for (int i = 0; i < l; i++) {
			b[i] = TraitementImage.intToByte((int) ((cR * this.getRC(i) + cG * this.getGC(i) + cB * this.getBC(i)) / sum));
		}
		TraitementGris tg = new TraitementGris(super.getPath().toString());
		tg.setWidth(super.getWidth());		// That's all you need 
		tg.setHeight(super.getHeight());	// to save the new 
		tg.setPXGS(b);						// grayscale image !
		return tg;
	}

	public TraitementImage grayscale() {
		return this.grayscale(0, 0, 0);
	}

	public void contrast() {
		String[] lines = {"0\t0\t0", "0\t-1\t0", "0\t0\t0"};
		MatrixOperator.createMatrix(lines, super.getParentPath());
		this.setRC(MatrixOperator.convolution(
			super.getParentPath(), 
			this.getRC(), 
			super.getWidth(), 
			super.getHeight(), 
			1, 
			255));
		this.setGC(MatrixOperator.convolution(
			super.getParentPath(), 
			this.getGC(), 
			super.getWidth(), 
			super.getHeight(), 
			1, 
			255));
		this.setBC(MatrixOperator.convolution(
			super.getParentPath(), 
			this.getBC(), 
			super.getWidth(), 
			super.getHeight(), 
			1, 
			255));
		super.getBI().setRGB(0, 0, super.getWidth(), super.getHeight(), this.getPXC(), 0, super.getWidth());
	}

	public void darkening() {
		String[] lines = {"0\t0\t0", "0\t4\t0", "0\t0\t0"};
		MatrixOperator.createMatrix(lines, super.getParentPath());
		this.setRC(MatrixOperator.convolution(
			super.getParentPath(), 
			this.getRC(), 
			super.getWidth(), 
			super.getHeight(), 
			5, 
			0));
		this.setGC(MatrixOperator.convolution(
			super.getParentPath(), 
			this.getGC(), 
			super.getWidth(), 
			super.getHeight(), 
			5, 
			0));
		this.setBC(MatrixOperator.convolution(
			super.getParentPath(), 
			this.getBC(), 
			super.getWidth(), 
			super.getHeight(), 
			5, 
			0));
		super.getBI().setRGB(0, 0, super.getWidth(), super.getHeight(), this.getPXC(), 0, super.getWidth());
	}

	public void lightening() {
		String[] lines = {"0\t0\t0", "0\t5\t0", "0\t0\t0"};
		MatrixOperator.createMatrix(lines, super.getParentPath());
		this.setRC(MatrixOperator.convolution(
			super.getParentPath(), 
			this.getRC(), 
			super.getWidth(), 
			super.getHeight(), 
			4, 
			0));
		this.setGC(MatrixOperator.convolution(
			super.getParentPath(), 
			this.getGC(), 
			super.getWidth(), 
			super.getHeight(), 
			4, 
			0));
		this.setBC(MatrixOperator.convolution(
			super.getParentPath(), 
			this.getBC(), 
			super.getWidth(), 
			super.getHeight(), 
			4, 
			0));
		super.getBI().setRGB(0, 0, super.getWidth(), super.getHeight(), this.getPXC(), 0, super.getWidth());
	}

	public void histogram() {
		for (int i = 0; i < 256; i++) {
			this.matHistogram[4*i] = i;
			this.matHistogram[4*i+1] = 0;
			this.matHistogram[4*i+2] = 0;
			this.matHistogram[4*i+3] = 0;
		}
		int cR = 0;
		int cG = 0;
		int cB = 0;
		int l = this.pxc.length;
		for (int j = 0; j < l; j++) {
			cR = this.getRC(j);
			cG = this.getGC(j);
			cB = this.getBC(j);
			this.matHistogram[4*cR+1]++;
			this.matHistogram[4*cG+2]++;
			this.matHistogram[4*cB+3]++;
		}
		String f = super.getParentPath() + super.getFilename() + "-h.txt";
		try (BufferedWriter bfwt = new BufferedWriter(new FileWriter(new File(f)))) {
			for (int j = 0; j < 256; j++){
				bfwt.write(String.valueOf(this.matHistogram[4*j]));
				bfwt.write("\t");
				bfwt.write(String.valueOf(this.matHistogram[4*j+1]));
				bfwt.write("\t");
				bfwt.write(String.valueOf(this.matHistogram[4*j+2]));
				bfwt.write("\t");
				bfwt.write(String.valueOf(this.matHistogram[4*j+3]));
				bfwt.newLine();
			}
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}

} 