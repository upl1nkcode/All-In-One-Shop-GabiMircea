package com.allinoneshop.dto.auth;

import com.allinoneshop.dto.UserDTO;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private UserDTO user;
}
