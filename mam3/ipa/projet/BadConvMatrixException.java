package mam3.ipa.projet;

public class BadConvMatrixException extends Exception {

    //----------[Constructor]----------

    public BadConvMatrixException() {

    }


    //----------[Methods]----------

    public String toString() {
        return "The matrix extracted from the file \"convmatrix.csv\" is not in the correct format";
    }

}
