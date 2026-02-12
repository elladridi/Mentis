package models;

import java.time.LocalDate;

public class Goal {
    private int id;
    private String description;
    private LocalDate deadline;
    private int progress; // de 0 à 100
    private String status; // ex: "En cours", "Terminé"

    // Constructeur vide
    public Goal() {}

    // Constructeur pour l'insertion (sans ID)
    public Goal(String description, LocalDate deadline, int progress, String status) {
        this.description = description;
        this.deadline = deadline;
        this.progress = progress;
        this.status = status;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "Goal{" + "id=" + id + ", desc='" + description + '\'' +
                ", progrès=" + progress + "%, statut='" + status + '\'' + '}';
    }
}