package mam3.ipa.projet;

//----------[Imports]----------
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
/*
	And now, all the imports that can be replaced by : 
		import javax.swing.*;
	To see all the classes imported with the line above, go to : 
		https://docs.oracle.com/javase/7/docs/api/javax/swing/package-summary.html
*/
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;


public class InterfaceUtilisateur extends JFrame implements ActionListener, ItemListener {

	//----------[Attributes]----------

	private int step;
	private JPanel pan = new JPanel();
	private JTextField textField = new JTextField("[Veuillez mettre ici le chemin absolu vers l'image en format PNG]");


	//----------[Class variables]----------

	private static int[] size = {1400, 700};			// global size
	private static int[] subSize = {1100, 650};			// image maximum size
	private static JFileChooser jfc;
	private static JToggleButton grayscale;
    private static JToggleButton contrast;
    private static JToggleButton darkening;
    private static JToggleButton lightening;
    private static JToggleButton histogram;
    private static LinkedHashMap<String, JToggleButton> toggledButtons = new LinkedHashMap<String, JToggleButton>();
    private static String file = "";
    private static TraitementImage tImage;
    private static ColorModel cm;
    private static String name = "";
    private static JLabel space = new JLabel("\t\t\t");
    /*
    The 'static' signature is often used because there will 
    always be one and only one InterfaceUtilisateur object.
    */

	//----------[Constructor]----------

	public InterfaceUtilisateur(int n) {
		super("Éditeur d'image");
		int[] cases = {0, 1, 2, 3, 4, 5, 6, 7};
		boolean isCase = TraitementImage.contains(cases, n);
		if ((isCase) && (n != cases[0])) {
			JLabel lbl = new JLabel(new ImageIcon(tImage.getBIBis()));	// where the loaded image will be printed
			this.add(lbl, BorderLayout.CENTER);
			this.pack();
			this.pan.add(lbl);
	   		this.pan.add(space);
		}
		switch (n) {
			case 0 :
				this.step = 0;
				this.addButton("...");
				if (file != "") {
					this.textField.setText(file);
					file = "";
					name = "";
				}
				this.pan.add(this.textField);
	   			this.pan.add(space);
				this.addButton("Insérer l'image");
				break;
			case 1 :
				this.step = 1;
				Box menu1 = Box.createVerticalBox();
				String[] t1 = {"Une seule option", "Plusieurs options", "Retour"};
				this.addButtons(t1, menu1);
				break;
			case 2 :
				this.step = 2;
				Box menu2 = Box.createVerticalBox();
				String[] t2 = {"Analyse", "Traitement", "Retour"};
				this.addButtons(t2, menu2);
				break;
			case 3 :
				this.step = 3;
				Box menu3 = Box.createVerticalBox();
				String[] t3 = {"Niveau de gris", "Contraste", "Assombrissement", "Éclairage", "Retour"};
				this.addButtons(t3, menu3);
				break;
			case 4 :
				this.step = 3;
				Box menu4 = Box.createVerticalBox();
				String[] t4 = {"Histogramme", "Retour"};
				this.addButtons(t4, menu4);
				break;
			case 5 :
				this.step = 2;
				Box menu5a = Box.createVerticalBox();
				JLabel options = new JLabel("Options");
				menu5a.add(options);
				grayscale = new JToggleButton("Niveau de gris");
				grayscale.addItemListener(this);
				menu5a.add(grayscale);
	   			contrast = new JToggleButton("Contraste");
	   			contrast.addItemListener(this);
	   			menu5a.add(contrast);
	   			darkening = new JToggleButton("Assombrissement");
	   			darkening.addItemListener(this);
	   			menu5a.add(darkening);
	   			lightening = new JToggleButton("Éclairage");
	   			lightening.addItemListener(this);
	   			menu5a.add(lightening);
	   			histogram = new JToggleButton("Histogramme");
	   			histogram.addItemListener(this);
	   			menu5a.add(histogram);
	   			this.pan.add(menu5a);
	   			this.pan.add(space);
	   			Box menu5b = Box.createVerticalBox();
	   			String[] t5b = {"Éditer", "Retour"};
				this.addButtons(t5b, menu5b);
				break;
			case 6 :
				Box menu6 = Box.createVerticalBox();
				String[] t6 = {"Retour à l'accueil", "Fermer"};
				this.addButtons(t6, menu6);
				break;
			case 7 :
				Box menu7 = Box.createVerticalBox();
				JLabel lbl7 = new JLabel("Vos valeurs pour l'histogramme sont prêtes, " + 
					"vous n'avez plus qu'à faire le graphique !");
				menu7.add(lbl7);
            	String[] t7 = {"Retour à l'accueil", "Fermer"};
            	this.addButtons(t7, menu7);
				break;
		}
		if (isCase) {
			setContentPane(this.pan);
			setSize(this.size[0], this.size[1]);
			setMinimumSize(new Dimension(this.size[0], this.size[1]));
			setLocationRelativeTo(null);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setVisible(true);
		}
	}


	//----------[Methods]----------

	/**
	 * Loads the image
	 */
	private void load(int n, boolean print) {
		try {
 			RenderedOp ro = JAI.create("fileload", file);
			cm = ro.getColorModel();
			int colorType = cm.getColorSpace().getType();
			switch (colorType) {
			case ColorSpace.TYPE_RGB :
				tImage = new TraitementCouleur(file);
				break;
			case ColorSpace.TYPE_GRAY :
				tImage = new TraitementGris(file);
				break;
     		}
     		name = tImage.getFilename();
     		tImage.load(ro);
     		tImage.resize(colorType, this.subSize[0], this.subSize[1]);
     		if (print) {
     			this.dispose();
     			InterfaceUtilisateur iu = new InterfaceUtilisateur(n);
     		}
 		} catch (Exception e) {
 			System.out.println(e);
 		}
	}

	/**
	 * Loads the image with print=true
	 */
	private void load(int n) {
		this.load(n, true);
	}

	/**
 	 * Lists all the possible effects
 	 */
	private void applyEffect(String effect, boolean print) {
		boolean possible = true;
		String suffix = "";
		switch (effect) {
			case "Niveau de gris" :
				if (cm.getColorSpace().getType() == ColorSpace.TYPE_RGB) {
					tImage = tImage.grayscale();
					suffix = "-gr";
				} else {
					possible = false;
					System.out.println("This is already a grayscale image !\n");
				}
				break;
			case "Contraste" :
				tImage.contrast();
				suffix = "-co";
				break;
			case "Assombrissement" :
				tImage.darkening();
				suffix = "-da";
				break;
			case "Éclairage" :
				tImage.lightening();
				suffix = "-li";
				break;
			case "Histogramme" :
				possible = false;
				tImage.histogram();
				this.dispose();
   				InterfaceUtilisateur iu12 = new InterfaceUtilisateur(7);
				break;
			default :
				possible = false;
				break;
		}
		if (possible) {
			this.addSuffix(suffix);
			tImage.save(name);
			file = tImage.getPath().toString();
			this.load(6, print);
		}
	}

	/**
 	 * Does applyEffect() with print=true
 	 */
	private void applyEffect(String effect) {
		this.applyEffect(effect, true);
	}

	/**
 	 * Checks if the last action made by the user is relevant, and does instructions if so
 	 */
	public void actionPerformed(ActionEvent acteve) {
		String action = acteve.getActionCommand();
		switch (action) {
			// the important actions...
			case "Insérer l'image" :
		     	file = this.textField.getText();
		     	boolean normal = (Files.exists(Paths.get(file)) && file.endsWith(".png"));
		     	if (normal || Files.exists(Paths.get(file + ".png"))) {
		     		if (!normal) {
		     			file = file + ".png";
		     		}
		     		this.load(1);
		     	} else {
		     		System.out.println("The file below couldn't be loaded :\n\t"
		     				+ file + "\nPlease try a valid file !\n");
		     	}
				break;
			case "Fermer" :
				this.dispose();
				break;
			case "Retour" :
				this.dispose();
				InterfaceUtilisateur iu2 = new InterfaceUtilisateur(this.step - 1);
				break;
			case "Retour à l'accueil" :
				this.dispose();
				InterfaceUtilisateur iu3 = new InterfaceUtilisateur(0);
				break;
			// ...followed by the others
			case "Une seule option" :
				this.dispose();
   				InterfaceUtilisateur iu4 = new InterfaceUtilisateur(2);
				break;
			case "Plusieurs options" :
				this.dispose();
   				InterfaceUtilisateur iu5 = new InterfaceUtilisateur(5);
				break;
			case "Analyse" :
				this.dispose();
   				InterfaceUtilisateur iu6 = new InterfaceUtilisateur(4);
				break;
			case "Traitement" :
				this.dispose();
   				InterfaceUtilisateur iu7 = new InterfaceUtilisateur(3);
				break;
			case "..." :
				UIManager.put("FileChooser.lookInLabelText", "Chercher dans :");
				UIManager.put("FileChooser.viewMenuLabelText", "Affichage");
				UIManager.put("FileChooser.listViewActionLabelText", "Liste");
				UIManager.put("FileChooser.detailsViewActionLabelText", "Détails");
				UIManager.put("FileChooser.refreshActionLabelText", "Actualiser");
				UIManager.put("FileChooser.newFolderActionLabelText", "Nouveau Dossier");
            	UIManager.put("FileChooser.fileNameLabelText", "Nom du Fichier");
            	UIManager.put("FileChooser.filesOfTypeLabelText", "Type de Fichiers");
            	UIManager.put("FileChooser.openButtonText", "Ouvrir");
            	UIManager.put("FileChooser.cancelButtonText", "Annuler");
				jfc = new JFileChooser();
				jfc.setAcceptAllFileFilterUsed(false);
 				jfc.setDialogTitle("Veuillez choisir une image en format png");
				FileNameExtensionFilter restrict = new FileNameExtensionFilter("Fichiers .png seulement", "png");
            	jfc.addChoosableFileFilter(restrict);
            	int a = jfc.showOpenDialog(null);
				if (a == JFileChooser.APPROVE_OPTION) {
					this.textField.setText(jfc.getSelectedFile().getAbsolutePath());
            	}
				break;
			case "Éditer" :
				int i = 1;
				boolean print = false;
				for (String s : toggledButtons.keySet()) {
					if (i == toggledButtons.size()) {
						print = true;
					}
					this.applyEffect(s, print);
					i++;
				}
				break;
			default :
				this.applyEffect(action);
				break;
		}
	}

	/**
 	 * Invoked when an item has been selected or deselected by the user
 	 */
	public void itemStateChanged(ItemEvent iteve) {
		toggledButtons.remove("Histogramme");
		if (grayscale.isSelected()) {
			toggledButtons.putIfAbsent("Niveau de gris", grayscale);
		} else {
			toggledButtons.remove("Niveau de gris");
			grayscale.setText("Niveau de gris");
		}
		if (contrast.isSelected()) {
			toggledButtons.putIfAbsent("Contraste", contrast);
		} else {
			toggledButtons.remove("Contraste");
			contrast.setText("Contraste");
		}
		if (darkening.isSelected()) {
			toggledButtons.putIfAbsent("Assombrissement", darkening);
		} else {
			toggledButtons.remove("Assombrissement");
			darkening.setText("Assombrissement");
		}
		if (lightening.isSelected()) {
			toggledButtons.putIfAbsent("Éclairage", lightening);
		} else {
			toggledButtons.remove("Éclairage");
			lightening.setText("Éclairage");
		}
		if (histogram.isSelected()) {
			toggledButtons.put("Histogramme", histogram);
		} else {
			toggledButtons.remove("Histogramme");
			histogram.setText("Histogramme");
		}
		int i = 1;
		for (String s : toggledButtons.keySet()) {
			toggledButtons.get(s).setText(s + " (" + i + ")");
			i++;
		}
		/*
			So now, when the histogram effect is selected,
			it will be the last applied effect !
		*/
	}

	/**
 	 * Creates a new button and adds it to the current pan
 	 */
	private void addButton(String s) {
		JButton b = new JButton(s);
		b.addActionListener(this);
		this.pan.add(b);
	}

	/**
 	 * Creates a new button and adds it to the Box m
 	 */
	private void addButton(String s, Box m) {
		JButton b = new JButton(s);
		b.addActionListener(this);
		m.add(b);
	}

	/**
 	 * Creates new buttons by calling addButton() multiple times
 	 */
	private void addButtons(String[] t) {
		int l = t.length;
		for (int i = 0; i < l; i++) {
			this.addButton(t[i]);
		}
	}

	/**
 	 * Creates new buttons by calling addButton() multiple times within a Box
 	 */
	private void addButtons(String[] t, Box m) {
		int l = t.length;
		for (int i = 0; i < l; i++) {
			this.addButton(t[i], m);
		}
		this.pan.add(m);
	}

	/**
 	 * Adds a suffix to the image's name, depending on the applied effect
 	 */
	private void addSuffix(String s) {
		if (!this.name.endsWith("-gr") || (s != "-gr")) {
			this.name = this.name + s;
		}
	}

}