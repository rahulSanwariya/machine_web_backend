package com.project.machiness_backend.adminpanel.service.serviceimpl;

import com.cloudinary.Cloudinary;
import com.project.machiness_backend.adminpanel.dto.MachineRequest;
import com.project.machiness_backend.adminpanel.dto.MachineResponse;
import com.project.machiness_backend.adminpanel.entity.Machine;
import com.project.machiness_backend.adminpanel.repo.MachineRepository;
import com.project.machiness_backend.adminpanel.service.MachineService;
import com.project.machiness_backend.auth.exception.AppException;
import com.project.machiness_backend.auth.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MachineServiceImpl implements MachineService {

    private final MachineRepository machineRepository;
    private final Cloudinary cloudinary;

    // ── Add Machine (JSON only, no image) ─────────────────────────────────
    @Override
    @Transactional
    public MachineResponse addMachine(MachineRequest request) {
        Machine machine = Machine.builder()
                .uuid(UUID.randomUUID())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .imageUuid(UUID.randomUUID())
                .imagePath(null)
                .build();

        machineRepository.save(machine);
        return buildResponse(machine);
    }

    // ── Upload Image for existing Machine ──────────────────────────────────
    @Override
    @Transactional
    public MachineResponse uploadImage(UUID machineUuid, MultipartFile image) {
        Machine machine = machineRepository.findByUuid(machineUuid)
                .orElseThrow(() -> new AppException(ErrorCode.MACHINE_NOT_FOUND));

        if (image == null || image.isEmpty())
            throw new AppException(ErrorCode.IMAGE_SAVE_FAILED);

        // Purani image Cloudinary se delete karo
        if (machine.getImagePath() != null) {
            deleteFromCloudinary(machine.getImagePath());
        }

        // Nayi image Cloudinary pe upload karo
        String imageUrl = uploadToCloudinary(image);

        machine.setImageUuid(UUID.randomUUID());
        machine.setImagePath(imageUrl); // Cloudinary URL save hoga
        machineRepository.save(machine);

        return buildResponse(machine);
    }

    // ── Update Machine Data (JSON only) ───────────────────────────────────
    @Override
    @Transactional
    public MachineResponse updateMachine(UUID uuid, MachineRequest request) {
        Machine machine = machineRepository.findByUuid(uuid)
                .orElseThrow(() -> new AppException(ErrorCode.MACHINE_NOT_FOUND));

        machine.setName(request.getName());
        machine.setDescription(request.getDescription());
        machine.setPrice(request.getPrice());
        machine.setStock(request.getStock());

        machineRepository.save(machine);
        return buildResponse(machine);
    }

    // ── Update Image only ─────────────────────────────────────────────────
    @Override
    @Transactional
    public MachineResponse updateImage(UUID machineUuid, MultipartFile image) {
        return uploadImage(machineUuid, image);
    }

    // ── Delete Machine ────────────────────────────────────────────────────
    @Override
    @Transactional
    public void deleteMachine(UUID uuid) {
        Machine machine = machineRepository.findByUuid(uuid)
                .orElseThrow(() -> new AppException(ErrorCode.MACHINE_NOT_FOUND));

        if (machine.getImagePath() != null) {
            deleteFromCloudinary(machine.getImagePath());
        }

        machineRepository.delete(machine);
    }

    // ── Get Single Machine ────────────────────────────────────────────────
    @Override
    public MachineResponse getMachine(UUID uuid) {
        Machine machine = machineRepository.findByUuid(uuid)
                .orElseThrow(() -> new AppException(ErrorCode.MACHINE_NOT_FOUND));
        return buildResponse(machine);
    }

    // ── Get All Machines ──────────────────────────────────────────────────
    @Override
    public List<MachineResponse> getAllMachines() {
        return machineRepository.findAll()
                .stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    // ── Get Image — Cloudinary ke baad zarurat nahi ───────────────────────
    @Override
    public Resource getImage(UUID imageUuid) {
        throw new AppException(ErrorCode.IMAGE_NOT_FOUND);
    }

    // ── Helper: Cloudinary Upload ─────────────────────────────────────────
    private String uploadToCloudinary(MultipartFile file) {
        try {
            Map<String, Object> options = new HashMap<>();
            options.put("folder", "machines");
            Map result = cloudinary.uploader().upload(file.getBytes(), options);
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new AppException(ErrorCode.IMAGE_SAVE_FAILED);
        }
    }

    // ── Helper: Cloudinary Delete ─────────────────────────────────────────
    private void deleteFromCloudinary(String imageUrl) {
        try {
            String publicId = extractPublicId(imageUrl);
            cloudinary.uploader().destroy(publicId, new HashMap<>());
        } catch (IOException ignored) {}
    }

    // ── Helper: Public ID extract karo Cloudinary URL se ─────────────────
    private String extractPublicId(String imageUrl) {
        // URL: https://res.cloudinary.com/{cloud}/image/upload/v123/machines/filename.jpg
        String[] parts = imageUrl.split("/");
        String filename = parts[parts.length - 1];
        String nameWithoutExt = filename.substring(0, filename.lastIndexOf("."));
        return "machines/" + nameWithoutExt;
    }

    // ── Helper: Build Response ────────────────────────────────────────────
    private MachineResponse buildResponse(Machine machine) {
        // imagePath mein ab Cloudinary URL hoga directly
        String imageUrl = machine.getImagePath();

        return MachineResponse.builder()
                .uuid(machine.getUuid())
                .name(machine.getName())
                .description(machine.getDescription())
                .price(machine.getPrice())
                .stock(machine.getStock())
                .isAvailable(machine.isAvailable())
                .imageUrl(imageUrl)
                .createdAt(machine.getCreatedAt())
                .updatedAt(machine.getUpdatedAt())
                .build();
    }
}