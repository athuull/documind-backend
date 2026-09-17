package com.athul.documind.Mapper;

import com.athul.documind.DTO.VehicleCreateRequestDTO;
import com.athul.documind.DTO.VehicleResponseDTO;
import com.athul.documind.Entity.Vehicle;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface VehicleMapper {

    // Create DTO → Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Vehicle toEntity(VehicleCreateRequestDTO dto);

    // Entity → Response DTO
    @Mapping(source = "client.name", target = "clientName")
    @Mapping(source = "client.id", target = "clientId")
    VehicleResponseDTO toResponse(Vehicle vehicle);

    // Update existing entity (important!)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateVehicleFromDto(
            com.athul.documind.DTO.VehicleUpdateRequestDTO dto,
            @MappingTarget Vehicle entity
    );
}