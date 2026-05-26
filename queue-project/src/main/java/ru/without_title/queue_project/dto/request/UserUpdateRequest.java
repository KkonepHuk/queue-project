package ru.without_title.queue_project.dto.request;

// import com.fasterxml.jackson.annotation.JsonProperty;

public class UserUpdateRequest {

    private String firstName;
    private String lastName;

    public UserUpdateRequest() {
    }

    public UserUpdateRequest(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
