package com.upc.divinasbackend.serviceinterfaces;

import com.upc.divinasbackend.dtos.LoginRequestDTO;
import com.upc.divinasbackend.dtos.LoginResponseDTO;

public interface AuthService {
    public LoginResponseDTO login(LoginRequestDTO dto);
}
