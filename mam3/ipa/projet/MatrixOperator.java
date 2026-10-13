package mam3.ipa.projet;

//----------[Imports]----------
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatrixOperator {

    //----------[Methods]----------

	/**
 	 * If there is no error, creates the file convmatrix.csv 
 	 */
    public static void createMatrix(String[] lines, String p) {
    	File f = new File(p + "convmatrix.csv");
		Path pBis = Paths.get(f.getAbsolutePath());
		try (BufferedWriter bfwt = Files.newBufferedWriter(pBis, StandardOpenOption.CREATE, 
				StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
			for (int i = 0; i < lines.length; i++) {
				bfwt.write(lines[i]);
				bfwt.newLine();
			}
			bfwt.close();
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}

	// Other possibility
	public static void createMatrix(String[] lines, Path p) {
		createMatrix(lines, p.toString());
	}

    /**
 	 * Checks if the String parameter can be an Integer 
 	 */
    public static void readInt(String s) throws BadConvMatrixException {
    	Pattern pattern = Pattern.compile("\\d+");
    	Matcher matcher = pattern.matcher(s);
    	if (!matcher.find(0)) {
    		throw new BadConvMatrixException();
    	}
    }

    /**
 	 * Checks if the n-long array have m elements 
 	 */
    public static void checkSize(int n, int m) throws BadConvMatrixException {
    	if (n != m) {
    		throw new BadConvMatrixException();
    	}
    }

	/**
	 * Does the convolution
	 */
	public static int[] convolution(String p, int[] t, int width, int height, int divisor, int offset) {
		// Loads the matrix...
		int[] matConv = {0, 0, 0, 0, 0, 0, 0, 0, 0};
		try { 					
			List<String> lines = Files.readAllLines(Paths.get(p + "convmatrix.csv"));
			int l = lines.size();
			matConv = new int[l*l];				// a l-times-l-long int[] instead of a l-by-l-long int[][]
			for (int i = 0; i < l; i++) {
				String[] split = lines.get(i).split("\t");
				checkSize(split.length, l);
				for (int j = 0; j < l; j++) {
					readInt(split[j]);
		        	matConv[i*l+j] = Integer.valueOf(split[j]);
		        	//System.out.println(matConv[i*l+j]);
		        }
		    }
		} catch (IOException ioe) {
			System.out.println(ioe);
		} catch (BadConvMatrixException bcme) {
			System.out.println(bcme);
		}
		// ...and then proceeds with the calculations
		int[] tBis = new int[width * height];
		if (matConv.length == 9) {
			if (divisor == 0) {
				divisor = 1;
			}
            int sum = 0;
            int indexMul = 0;
            int result = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    sum = 0;
                    result = 0;
                    for (int i = 0; i < 9; i++) {
                        indexMul = indexForMultiplication(x, y, width, height, i);
                        if (indexMul >= 0) {
                            sum += t[indexMul] * matConv[i];
                        }
                    }
                    result = sum / divisor + offset;
                    if (result < 0) {
                    	result = 0;
                    } else if (result > 255) {
                    	result = 255;
                    }
                    // result is now with certainty between 0 and 255
                    tBis[y*width+x] = result;
                }
            }
        }
		/*
			We don't deal with the case where matConv is a 25-long array of integers,
			which represents a 5-by-5 matrix, or other cases, because the requested 
			program only deals with a representation of a 3-by-3 matrix.
		*/
		return tBis;
	}

	/**
	 * Does the convolution with divisor=1 and offset=0
	 */
	public static int[] convolution(String p, int[] t, int width, int height) {
		return convolution(p, t, width, height, 1, 0);
	}

	/**
	 * @return the correct index for the multiplication if it's possible and -1 if it's not
	 */
	public static int indexForMultiplication(int x, int y, int wMax, int hMax, int i) {
    	int indexMul = -1;
    	int tIndex = y * wMax + x;
    	if ((tIndex >= 0) && (tIndex < wMax * hMax)) {
    		// correspondence between matConv and a 3-by-3 matrix, depending on which case
	    	switch (i) {
	    		case 0 :		// left and up
	    			if (((tIndex - 1) / wMax == y) && (tIndex - wMax - 1 >= 0)) {
	    				indexMul = tIndex - wMax - 1;
	    			}
	    			break;
	    		case 1 :		// up
	    			if (tIndex - wMax >= 0) {
	    				indexMul = tIndex - wMax;
	    			}
	    			break;
	    		case 2 :		// right and up
	    			if (((tIndex + 1) / wMax == y) && (tIndex - wMax + 1 >= 0)) {
	    				indexMul = tIndex - wMax + 1;
	    			}
	    			break;
	    		case 3 :		// left
	    			if (((tIndex - 1) / wMax == y) && (tIndex - 1 >= 0)) {
	    				indexMul = tIndex - 1;
	    			}
	    			break;
	    		case 4 :		// center
	    			indexMul = tIndex;
	    			break;
	    		case 5 :		// right
	    			if (((tIndex + 1) / wMax == y) && (tIndex + 1 < wMax * hMax)) {
	    				indexMul = tIndex + 1;
	    			}
	    			break;
	    		case 6 :		// left and down
	    			if ((tIndex - 1 >= 0) && ((tIndex - 1) / wMax == y) && (tIndex + wMax - 1 < wMax * hMax)) {
	    				indexMul = tIndex + wMax - 1;
	    			}
	    			break;
	    		case 7 :		// down
	    			if (tIndex + wMax < wMax * hMax) {
	    				indexMul = tIndex + wMax;
	    			}
	    			break;
	    		case 8 :		// right and down
	    			if (((tIndex + 1) / wMax == y) && (tIndex + wMax + 1 < wMax * hMax)) {
	    				indexMul = tIndex + wMax + 1;
	    			}
	    			break;
	    	}
    	}
    	return indexMul;
    }

} 