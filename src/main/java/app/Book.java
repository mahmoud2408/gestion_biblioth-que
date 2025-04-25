package app;

import javafx.beans.property.*;

public class Book {
    private final IntegerProperty code;
    private final StringProperty titre;
    private final StringProperty auteur;
    private final StringProperty categorie;
    private final IntegerProperty annee;
    private final IntegerProperty quantite;

    public Book(int code, String titre, String auteur, String categorie, int annee, int quantite) {
        this.code = new SimpleIntegerProperty(code);
        this.titre = new SimpleStringProperty(titre);
        this.auteur = new SimpleStringProperty(auteur);
        this.categorie = new SimpleStringProperty(categorie);
        this.annee = new SimpleIntegerProperty(annee);
        this.quantite = new SimpleIntegerProperty(quantite);
    }

    public IntegerProperty codeProperty() { return code; }
    public StringProperty titreProperty() { return titre; }
    public StringProperty auteurProperty() { return auteur; }
    public StringProperty categorieProperty() { return categorie; }
    public IntegerProperty anneeProperty() { return annee; }
    public IntegerProperty quantiteProperty() { return quantite; }
}
