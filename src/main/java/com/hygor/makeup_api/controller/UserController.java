package com.hygor.makeup_api.controller;

import com.hygor.makeup_api.dto.address.AddressRequest;
import com.hygor.makeup_api.dto.address.AddressResponse;
import com.hygor.makeup_api.dto.auth.ChangePasswordRequest;
import com.hygor.makeup_api.dto.user.UserResponse;
import com.hygor.makeup_api.service.AddressService;
import com.hygor.makeup_api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AddressService addressService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile() {
        return ResponseEntity.ok(userService.getCurrentUserProfile());
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok().build();
    }

    // --- MFA SETTINGS ---

    @PostMapping("/mfa/generate")
    public ResponseEntity<Map<String, String>> generateMfa() {
        String qrCodeUrl = userService.generateMfaQrCode();
        return ResponseEntity.ok(Map.of("qr_code_url", qrCodeUrl));
    }

    @PostMapping("/mfa/enable")
    public ResponseEntity<Void> enableMfa(@RequestParam int code) {
        userService.enableMfa(code);
        return ResponseEntity.ok().build();
    }

    // --- ENDEREÇOS ---

    @GetMapping("/addresses")
    public ResponseEntity<List<AddressResponse>> getMyAddresses() {
        return ResponseEntity.ok(addressService.getMyAddresses());
    }

    @PostMapping("/addresses")
    public ResponseEntity<AddressResponse> createAddress(@RequestBody @Valid AddressRequest request) {
        return ResponseEntity.ok(addressService.createAddress(request));
    }

    @PutMapping("/addresses/{id}/default")
    public ResponseEntity<AddressResponse> setDefaultAddress(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.setDefaultAddress(id));
    }
}