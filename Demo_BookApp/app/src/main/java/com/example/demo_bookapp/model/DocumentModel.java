package com.example.demo_bookapp.model;

public class DocumentModel {
    String title,linkDocument,subjectName;
    public DocumentModel(String title, String linkDocument, String subjectName) {
        this.title = title;
        this.linkDocument = linkDocument;
        this.subjectName = subjectName;
    }

    public DocumentModel() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLinkDocument() {
        return linkDocument;
    }

    public void setLinkDocument(String linkDocument) {
        this.linkDocument = linkDocument;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }
}
