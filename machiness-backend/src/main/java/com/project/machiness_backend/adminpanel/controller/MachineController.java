package com.project.machiness_backend.adminpanel.controller;

import com.project.machiness_backend.adminpanel.dto.MachineRequest;
import com.project.machiness_backend.adminpanel.dto.MachineResponse;
import com.project.machiness_backend.adminpanel.service.MachineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/machines")
@RequiredArgsConstructor
public class MachineController {

    private final MachineService machineService;

    // ── 1. Add Machine (JSON only) ─────────────────────────────────────────
    @PostMapping
    public ResponseEntity<MachineResponse> addMachine(
            @Valid @RequestBody MachineRequest request) {
        return ResponseEntity.ok(machineService.addMachine(request));
    }

    // ── 2. Upload Image for Machine ────────────────────────────────────────
    @PostMapping(value = "/{uuid}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MachineResponse> uploadImage(
            @PathVariable UUID uuid,
            @RequestPart("image") MultipartFile image) {
        return ResponseEntity.ok(machineService.uploadImage(uuid, image));
    }

    // ── 3. Update Machine Data (JSON only) ────────────────────────────────
    @PutMapping("/{uuid}")
    public ResponseEntity<MachineResponse> updateMachine(
            @PathVariable UUID uuid,
            @Valid @RequestBody MachineRequest request) {
        return ResponseEntity.ok(machineService.updateMachine(uuid, request));
    }

    // ── 4. Update Machine Image ────────────────────────────────────────────
    @PutMapping(value = "/{uuid}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MachineResponse> updateImage(
            @PathVariable UUID uuid,
            @RequestPart("image") MultipartFile image) {
        return ResponseEntity.ok(machineService.updateImage(uuid, image));
    }

    // ── 5. Delete Machine ──────────────────────────────────────────────────
    @DeleteMapping("/{uuid}")
    public ResponseEntity<String> deleteMachine(@PathVariable UUID uuid) {
        machineService.deleteMachine(uuid);
        return ResponseEntity.ok("Machine deleted successfully");
    }

    // ── 6. Get Single Machine ──────────────────────────────────────────────
    @GetMapping("/{uuid}")
    public ResponseEntity<MachineResponse> getMachine(@PathVariable UUID uuid) {
        return ResponseEntity.ok(machineService.getMachine(uuid));
    }

    // ── 7. Get All Machines ────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<MachineResponse>> getAllMachines() {
        return ResponseEntity.ok(machineService.getAllMachines());
    }

    // ── 8. Get Image ───────────────────────────────────────────────────────
    @GetMapping("/images/{imageUuid}")
    public ResponseEntity<Resource> getImage(@PathVariable UUID imageUuid) {
        Resource resource = machineService.getImage(imageUuid);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }
}