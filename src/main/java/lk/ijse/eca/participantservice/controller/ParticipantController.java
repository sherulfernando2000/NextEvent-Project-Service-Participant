package lk.ijse.eca.participantservice.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.groups.Default;
import lk.ijse.eca.participantservice.dto.ParticipantRequestDTO;
import lk.ijse.eca.participantservice.dto.ParticipantResponseDTO;
import lk.ijse.eca.participantservice.service.ParticipantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/participants")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ParticipantController {

    private final ParticipantService participantService;

    private static final String NIC_REGEXP = "^\\d{9}[vV]$";

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ParticipantResponseDTO> createParticipant(
            @Validated({Default.class, ParticipantRequestDTO.OnCreate.class}) @ModelAttribute ParticipantRequestDTO dto) {
        log.info("POST /api/v1/participants - NIC: {}", dto.getNic());
        ParticipantResponseDTO response = participantService.createParticipant(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping(value = "/{nic}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ParticipantResponseDTO> updateParticipant(
            @PathVariable @Pattern(regexp = NIC_REGEXP, message = "NIC must be 9 digits followed by V or v") String nic,
            @Valid @ModelAttribute ParticipantRequestDTO dto) {
        log.info("PUT /api/v1/participants/{}", nic);
        ParticipantResponseDTO response = participantService.updateParticipant(nic, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{nic}")
    public ResponseEntity<Void> deleteParticipant(
            @PathVariable @Pattern(regexp = NIC_REGEXP, message = "NIC must be 9 digits followed by V or v") String nic) {
        log.info("DELETE /api/v1/participants/{}", nic);
        participantService.deleteParticipant(nic);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{nic}")
    public ResponseEntity<ParticipantResponseDTO> getParticipant(
            @PathVariable @Pattern(regexp = NIC_REGEXP, message = "NIC must be 9 digits followed by V or v") String nic) {
        log.info("GET /api/v1/participants/{}", nic);
        ParticipantResponseDTO response = participantService.getParticipant(nic);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ParticipantResponseDTO>> getAllParticipants() {
        log.info("GET /api/v1/participants");
        List<ParticipantResponseDTO> participants = participantService.getAllParticipants();
        return ResponseEntity.ok(participants);
    }

    @GetMapping("/{nic}/picture")
    public ResponseEntity<byte[]> getParticipantPicture(
            @PathVariable @Pattern(regexp = NIC_REGEXP, message = "NIC must be 9 digits followed by V or v") String nic) {
        log.info("GET /api/v1/participants/{}/picture", nic);
        byte[] picture = participantService.getParticipantPicture(nic);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(picture);
    }
}
