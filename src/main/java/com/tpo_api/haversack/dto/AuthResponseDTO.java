package com.tpo_api.haversack.dto;

import com.tpo_api.haversack.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    private boolean success;
    private String message;
    private String token;
    private User user;
}
