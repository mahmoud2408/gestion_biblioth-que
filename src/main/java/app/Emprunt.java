package app;

import javafx.beans.property.*;
import java.sql.Date;

public class Emprunt {
    private final IntegerProperty numEmprunt;
    private final StringProperty nomEtudiant;
    private final StringProperty titreLivre;
    private final ObjectProperty<Date> dateEmprunt;
    private final ObjectProperty<Date> dateRetourPrevu;
    private final ObjectProperty<Date> dateRetourReel;
    private final StringProperty statut;

    public Emprunt(int numEmprunt, String nomEtudiant, String titreLivre,
                   Date dateEmprunt, Date dateRetourPrevu, Date dateRetourReel, String statut) {
        this.numEmprunt = new SimpleIntegerProperty(numEmprunt);
        this.nomEtudiant = new SimpleStringProperty(nomEtudiant);
        this.titreLivre = new SimpleStringProperty(titreLivre);
        this.dateEmprunt = new SimpleObjectProperty<>(dateEmprunt);
        this.dateRetourPrevu = new SimpleObjectProperty<>(dateRetourPrevu);
        this.dateRetourReel = new SimpleObjectProperty<>(dateRetourReel);
        this.statut = new SimpleStringProperty(statut);
    }

    // Getters pour les propriétés
    public IntegerProperty numEmpruntProperty() { return numEmprunt; }
    public StringProperty nomEtudiantProperty() { return nomEtudiant; }
    public StringProperty titreLivreProperty() { return titreLivre; }
    public ObjectProperty<Date> dateEmpruntProperty() { return dateEmprunt; }
    public ObjectProperty<Date> dateRetourPrevuProperty() { return dateRetourPrevu; }
    public ObjectProperty<Date> dateRetourReelProperty() { return dateRetourReel; }
    public StringProperty statutProperty() { return statut; }
}