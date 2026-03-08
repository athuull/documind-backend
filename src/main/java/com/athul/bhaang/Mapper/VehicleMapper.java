package com.athul.bhaang.Mapper;

import com.athul.bhaang.DTO.VehicleCreateRequestDTO;
import com.athul.bhaang.DTO.VehicleResponseDTO;
import com.athul.bhaang.Entity.Vehicle;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface VehicleMapper {

    // Create DTO → Entity
    Vehicle toEntity(VehicleCreateRequestDTO dto);

    // Entity → Response DTO
    @Mapping(source = "client.name", target = "clientName")
    VehicleResponseDTO toResponse(Vehicle vehicle);

    // Update existing entity (important!)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateVehicleFromDto(
            com.athul.bhaang.DTO.VehicleUpdateRequestDTO dto,
            @MappingTarget Vehicle entity
    );
}