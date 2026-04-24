package com.eco.alert.ecoAlert.controller;

import com.eco.alert.ecoAlert.service.UserService;
import com.ecoalert.api.AuthApi;
import com.ecoalert.model.LoginInput;
import com.ecoalert.model.LoginOutput;
import com.ecoalert.model.UtenteInput;
import com.ecoalert.model.UtenteOutput;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController implements AuthApi {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ResponseEntity<LoginOutput> login(@RequestBody LoginInput loginInput) {
        return ResponseEntity.ok(userService.login(loginInput));
    }

    @Override
    public ResponseEntity<UtenteOutput> signIn(@RequestBody UtenteInput utenteInput) {
        return ResponseEntity.status(201).body(userService.creaUtente(utenteInput));
    }
}

