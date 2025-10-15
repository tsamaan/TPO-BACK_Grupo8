package com.tpo_api.haversack.dto;

import lombok.Data;

@Data
public class UserRegistrationDTO {
    private String email;
    private String password;
    private String confirmPassword;
    private String nombre;
    private String apellido;
    private String usuario;
    private String name;
    private String address;
    private String phone;
}
