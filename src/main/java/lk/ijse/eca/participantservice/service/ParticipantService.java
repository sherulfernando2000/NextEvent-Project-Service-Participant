package lk.ijse.eca.participantservice.service;

import lk.ijse.eca.participantservice.dto.ParticipantRequestDTO;
import lk.ijse.eca.participantservice.dto.ParticipantResponseDTO;

import java.util.List;

public interface ParticipantService {

    ParticipantResponseDTO createParticipant(ParticipantRequestDTO dto);

    ParticipantResponseDTO updateParticipant(String nic, ParticipantRequestDTO dto);

    void deleteParticipant(String nic);

    ParticipantResponseDTO getParticipant(String nic);

    List<ParticipantResponseDTO> getAllParticipants();

    byte[] getParticipantPicture(String nic);
}
