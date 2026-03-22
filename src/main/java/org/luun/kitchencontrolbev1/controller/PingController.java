package org.luun.kitchencontrolbev1.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PingController {

    /**
     * Endpoint to check if the server is alive.
     * @return A response entity with the string "pong" and HTTP status 200 OK.
     */
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("Server still alive!!");
    }
}
