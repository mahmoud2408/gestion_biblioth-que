package app;

public class Etudiant {

    private int numEtudiant;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;

    // Constructor
    public Etudiant(int numEtudiant, String nom, String prenom, String email, String telephone) {
        this.numEtudiant = numEtudiant;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
    }

    // Getters
    public int getNumEtudiant() {
        return numEtudiant;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getEmail() {
        return email;
    }

    public String getTelephone() {
        return telephone;
    }
}
