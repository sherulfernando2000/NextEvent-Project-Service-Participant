package lk.ijse.eca.participantservice.mapper;

import lk.ijse.eca.participantservice.dto.ParticipantRequestDTO;
import lk.ijse.eca.participantservice.dto.ParticipantResponseDTO;
import lk.ijse.eca.participantservice.entity.Participant;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class ParticipantMapper {

    @Mapping(target = "picture", expression = "java(buildPictureUrl(participant))")
    public abstract ParticipantResponseDTO toResponseDto(Participant participant);

    @Mapping(target = "picture", ignore = true)
    public abstract Participant toEntity(ParticipantRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "nic", ignore = true)
    @Mapping(target = "picture", ignore = true)
    public abstract void updateEntity(ParticipantRequestDTO dto, @MappingTarget Participant participant);

    protected String buildPictureUrl(Participant participant) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/participants/{nic}/picture")
                .buildAndExpand(participant.getNic())
                .toUriString();
    }
}
