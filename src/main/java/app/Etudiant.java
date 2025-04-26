package app;

import javafx.beans.property.*;

public class Etudiant {
    private final IntegerProperty NumEtudiant;
    private final StringProperty nom;
    private final StringProperty prenom;
    private final StringProperty email;
    private final StringProperty telephone;

    public Etudiant(int NumEtudiant, String nom, String prenom, String email, String telephone) {
        this.NumEtudiant = new SimpleIntegerProperty(NumEtudiant);
        this.nom = new SimpleStringProperty(nom);
        this.prenom = new SimpleStringProperty(prenom);
        this.email = new SimpleStringProperty(email);
        this.telephone = new SimpleStringProperty(telephone);
    }

    public IntegerProperty numEtudiantProperty() { return NumEtudiant; }
    public StringProperty nomProperty() { return nom; }
    public StringProperty prenomProperty() { return prenom; }
    public StringProperty emailProperty() { return email; }
    public StringProperty telephoneProperty() { return telephone; }
}