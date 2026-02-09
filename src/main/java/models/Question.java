package models;

public class Question {

    private int questionId;
    private int assessmentId;
    private String text;
    private String scale;

    public Question() {}

    public Question(int assessmentId, String text, String scale) {
        this.assessmentId = assessmentId;
        this.text = text;
        this.scale = scale;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public int getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(int assessmentId) {
        this.assessmentId = assessmentId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getScale() {
        return scale;
    }

    public void setScale(String scale) {
        this.scale = scale;
    }


}
