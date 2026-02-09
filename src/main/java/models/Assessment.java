package models;

public class Assessment {
    private int assessmentId;
    private String title;
    private String description;
    private String type;
    private String status;
    private String imagePath; // New field

    public Assessment() {}

    public Assessment(String title, String description, String type, String status) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.status = status;
    }

    public Assessment(String title, String description, String type, String status, String imagePath) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.status = status;
        this.imagePath = imagePath;
    }

    public int getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(int assessmentId) {
        this.assessmentId = assessmentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
