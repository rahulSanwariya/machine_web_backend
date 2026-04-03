package com.project.machiness_backend.adminpanel.service;

import com.project.machiness_backend.adminpanel.dto.MachineRequest;
import com.project.machiness_backend.adminpanel.dto.MachineResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface MachineService {

    MachineResponse addMachine(MachineRequest request);

    MachineResponse uploadImage(UUID machineUuid, MultipartFile image);

    MachineResponse updateMachine(UUID uuid, MachineRequest request);

    MachineResponse updateImage(UUID machineUuid, MultipartFile image);

    void deleteMachine(UUID uuid);

    MachineResponse getMachine(UUID uuid);

    List<MachineResponse> getAllMachines();

    Resource getImage(UUID imageUuid);
}