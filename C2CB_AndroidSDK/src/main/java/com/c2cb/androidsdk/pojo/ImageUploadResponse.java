package com.c2cb.androidsdk.pojo;

public class ImageUploadResponse {

    private String imageName;
    private String response;
    private String imageFolder;
    private String message;
    private int status;

    // Default constructor
    public ImageUploadResponse() {
    }

    // Parameterized constructor
    public ImageUploadResponse(String imageName, String response, String imageFolder, String message, int status) {
        this.imageName = imageName;
        this.response = response;
        this.imageFolder = imageFolder;
        this.message = message;
        this.status = status;
    }

    // Getters and Setters
    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getImageFolder() {
        return imageFolder;
    }

    public void setImageFolder(String imageFolder) {
        this.imageFolder = imageFolder;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ImageUploadResponse{" +
                "imageName='" + imageName + '\'' +
                ", response='" + response + '\'' +
                ", imageFolder='" + imageFolder + '\'' +
                ", message='" + message + '\'' +
                ", status=" + status +
                '}';
    }
}
