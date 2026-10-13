package mam3.ipa.projet;

//----------[Imports]----------
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.swing.JFrame;


public abstract class TraitementImage {

	//----------[Attributes]----------

	/**
 	 * The file name
 	 */
	private String filename;

	/**
 	 * The path to the file
 	 */
	private Path path;

	/**
 	 * The original image
 	 */
	private BufferedImage bI;

	/**
 	 * The resized image for display
 	 */
	private BufferedImage bIBis;

	/**
 	 * The image's width
 	 */
	private int width;

	/**
 	 * The image's height
 	 */
    private int height;

    /**
     * Either a 1024-long int[] instead of a 256-by-4-long int[][]
 	 * or a 512-long int[] instead of a 256-by-2-long int[][],
 	 * depends on the image's color type
 	 */
    private int[] matHistogram;


    //----------[Constructor]----------

	public TraitementImage(String s) {
		this.setPath(Paths.get(s));
		this.setFilename();
	}


	//----------[Methods]----------

	// Getters

    /**
 	 * @return filename
 	 */
    public String getFilename() {
    	return this.filename;
    }

    /**
 	 * @return path
 	 */
    public Path getPath() {
    	return this.path;
    }

    /**
 	 * @return the String representation of the path to the folder that contains the file
 	 */
    public String getParentPath() {
        String s = "/";
        if (isOnWindows()) {
            s = "\\";
        }
        return adaptedPath(this.path.getParent()).toString() + s;
    }

    /**
 	 * @return bI
 	 */
    public BufferedImage getBI() {
    	return this.bI;
    }

    /**
 	 * @return bIBis
 	 */
    public BufferedImage getBIBis() {
    	return this.bIBis;
    }

    /**
 	 * @return width
 	 */
    public int getWidth() {
    	return this.width;
    }

    /**
 	 * @return height
 	 */
    public int getHeight() {
    	return this.height;
    }

    // Setters

    /**
 	 * Sets file's name
 	 */
    public void setFilename() {
    	String s = this.path.toString();
    	String[] t = s.split("/");
    	if (isOnWindows()) {
    		t = s.split("\\\\");
    	}
    	int l = t.length;
    	this.filename = t[l-1].substring(0, t[l-1].length()-4);
    }

    /**
 	 * Sets file's path
 	 */
    public void setPath(Path p) {
    	this.path = null;
    	if (adaptedPath(p).toFile().exists()) {
    		this.path = adaptedPath(p);
    	}
    }

    /**
 	 * Changes file's name and then file's path 
 	 */
    public void changePathWithFileName(String s) {
    	this.filename = s;
    	setPath(Paths.get(getParentPath() + s + ".png"));
    }

    /**
 	 * Sets bI
 	 */
    public void setBI(BufferedImage a) {
    	this.bI = a;
    }

    /**
 	 * Sets bIBis
 	 */
    public void setBIBis(BufferedImage a) {
    	this.bIBis = a;
    }

    /**
 	 * Sets image's width
 	 */
    public void setWidth(int w) {
    	this.width = w;
    }

    /**
 	 * Sets image's height
 	 */
    public void setHeight(int h) {
    	this.height = h;
    }

    // Others

    /**
 	 * @return true if the program is currently running on Windows and false otherwise
 	 */
    public static boolean isOnWindows() {
    	return System.getProperty("os.name").startsWith("Windows");
    }

    /**
     * @return a String array containing every possible root of the computer on which the program is currently running
     */
    public static String[] getRoots(Path p) {
        File[] r = File.listRoots();
        int l = r.length;
        String[] rBis = new String[l];
        for (int i = 0; i < l; i++) {
            rBis[i] = r[i].toString().substring(0, 1);
        }
        return rBis;
    }

    /**
     * @return the path p adapted to the operating system on which the program is currently running
     */
    public static Path adaptedPath(Path p) {
        String s = p.toString();
        Pattern pattern = Pattern.compile("[A-Z]:");
        if (isOnWindows()) {
            s.replaceAll("/", "\\\\");
            Matcher matcher = pattern.matcher(s);
            if (!matcher.find(0)) {
                String[] r = getRoots(p);
                Path pBis = Paths.get(r[0] + ":" + s);
                for (int i = 1; ((!pBis.toFile().exists()) && (i < r.length)); i++) {
                    pBis = Paths.get(r[i] + ":" + s);
                }
                s = pBis.toString();
            }
        } else {
            s.replaceAll("\\\\", "/");
            Matcher matcher = pattern.matcher(s);
            if (matcher.find(0)) {
                s = s.substring(2, s.length());
            }
        }
        return Paths.get(s);
    }

    /**
 	 * @return true if the int e is in the array of integers t and false otherwise
 	 */
    public static boolean contains(int[] t, int e) {
        for (int i : t) {
        	if (i == e) {
            	return true;
        	}
    	}
    	return false;
    }

    /**
 	 * @return a representation of the array of integers t
 	 */
    public static String rpz(int[] t) {
        String s = "[";
        for (int i = 0; i < t.length; i++) {
            s += t[i];
            if (i < t.length - 1) {
                s += ", ";
            } else if (i == t.length - 1) {
                s += "]";
            }
        }
        return s;
    }

    /**
     * @return a matrix-type representation of the array of integers t
     */
    public static String rpzBis(int[] t, int width, int height) {
        String s = "";
        if (t.length == width * height) {
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    s += String.format("%x", t[i*width+j]);
                    if (j < width - 1) {
                        s += "\t";
                    }
                }
                if (i < width - 1) {
                    s += "\n";
                }
            }
        }
        return s;
    }

    /**
	 * Transforms a signed byte into an unsigned integer
	 */
	public static int byteToInt(byte b) {
		return Byte.toUnsignedInt(b);
	}

    /**
	 * Transforms a byte[] into an int[] with only unsigned integers
	 */
	public static int[] bytesToInts(byte[] b) {
		int l = b.length;
		int[] t = new int[l];
		for (int i = 0; i < l; i++) {
			t[i] = byteToInt(b[i]);
		}
		return t;
	}

	/**
	 * Transforms an int into a signed byte
	 */
	public static byte intToByte(int i) {
		if (i < 0) {
			return 0;
		}
		if (i > 255) {
			return -1;
		}
		return (byte) i;
	}

	/**
	 * Transforms an int[] into a byte[]
	 */
	public static byte[] intsToBytes(int[] t) {
		int l = t.length;
		byte[] b = new byte[l];
		for (int i = 0; i < l; i++) {
			b[i] = intToByte(t[i]);
		}
		return b;
	}

	/**
	 * @return the closest ten-power number of an int
	 */
	public static int roundTens(int a) {
		return (int) Math.pow(10, Math.round(Math.log10((double) a)));
	}

	/**
 	 * Loads the image
 	 */
	protected abstract void load(RenderedOp ro);

	/**
	 * Saves the image with its original size
	 */
	protected abstract void save(String name);

	/**
	 * Resizes the image to fit the interface
	 */
	protected void resize(int colorType, int maxWidth, int maxHeight) {
		int w = this.bI.getWidth();
        int h = this.bI.getHeight();
        int wBis = w;
        int hBis = h;
        while ((wBis > maxWidth) || (hBis > maxHeight)) {
            wBis = (int) (0.98 * wBis);
            hBis = (int) (0.98 * hBis);
        }
        BufferedImage bufIm = new BufferedImage(wBis, hBis, colorType);
        Graphics2D g = bufIm.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(this.bI, 0, 0, wBis, hBis, 0, 0, w, h, null);
        g.dispose();
    	this.bIBis = bufIm;
	}

	/**
	 * Turns the colored image into a grayscale one
	 * (the "transformation en niveau de gris" effect)
	 */
	protected abstract TraitementImage grayscale();

	/**
	 * Contrasts the image by finding the complement of each color channel
	 * (the "contraste" effect, more known as the negative effect)
	 */
	protected abstract void contrast();

	/**
	 * Darkens the image
	 * (the "assombrissement" effect)
	 */
	protected abstract void darkening();

	/**
	 * Illuminates the image
	 * (the "éclairage" effect)
	 */
	protected abstract void lightening();

	/**
	 * Creates a text file with image values
	 * (the "analyse par histogramme")
	 */
	protected abstract void histogram();


	// the class MatrixOperator will deal with all the things related to convolution

}