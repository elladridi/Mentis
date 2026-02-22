package models;

import java.time.LocalDateTime;

public class Mood {
    // 1. Attributs (Private pour l'encapsulation comme le prof)
    private int id;
    private String feeling;
    private String note;
    private LocalDateTime date;

    // 2. Constructeur vide (Indispensable pour certaines bibliothèques)
    public Mood() {
    }

    // 3. Constructeur pour l'insertion (Sans l'ID car la base l'auto-incrémente)
    public Mood(String feeling, String note, LocalDateTime date) {
        this.feeling = feeling;
        this.note = note;
        this.date = date;
    }

    // 4. Getters : Ces méthodes permettent au Service de lire les données
    public int getId() {
        return id;
    }

    public String getFeeling() {
        return feeling;
    }

    public String getNote() {
        return note;
    }

    public LocalDateTime getDate() {
        return date;
    }

    // 5. Setters : Ces méthodes permettent de modifier les données
    public void setId(int id) {
        this.id = id;
    }

    public void setFeeling(String feeling) {
        this.feeling = feeling;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    // 6. Méthode toString (Très utile pour tes tests dans la console)
    @Override
    public String toString() {
        return "Mood{" +
                "id=" + id +
                ", feeling='" + feeling + '\'' +
                ", note='" + note + '\'' +
                ", date=" + date +
                '}';
    }
}