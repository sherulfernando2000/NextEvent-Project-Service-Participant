package lk.ijse.eca.participantservice.service.impl;

import lk.ijse.eca.participantservice.dto.ParticipantRequestDTO;
import lk.ijse.eca.participantservice.dto.ParticipantResponseDTO;
import lk.ijse.eca.participantservice.entity.Participant;
import lk.ijse.eca.participantservice.mapper.ParticipantMapper;
import lk.ijse.eca.participantservice.exception.DuplicateParticipantException;
import lk.ijse.eca.participantservice.exception.FileOperationException;
import lk.ijse.eca.participantservice.exception.ParticipantNotFoundException;
import lk.ijse.eca.participantservice.repository.ParticipantRepository;
import lk.ijse.eca.participantservice.service.ParticipantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParticipantServiceImpl implements ParticipantService {

    private final ParticipantRepository participantRepository;
    private final ParticipantMapper participantMapper;

    @Value("${app.storage.path}")
    private String storagePathStr;

    private Path storagePath;

    /**
     * Creates a new participant.
     *
     * Transaction strategy:
     *  1. Persist participant record to DB (JPA defers the INSERT until flush/commit).
     *  2. Write picture file to disk (immediate).
     *  3. If the file write fails an exception is thrown, which causes
     *     @Transactional to roll back the DB INSERT — no orphaned record.
     *  4. If the file write succeeds the method returns normally and
     *     @Transactional commits both the record and the file atomically.
     */
    @Override
    @Transactional
    public ParticipantResponseDTO createParticipant(ParticipantRequestDTO dto) {
        log.debug("Creating participant with NIC: {}", dto.getNic());

        if (participantRepository.existsById(dto.getNic())) {
            log.warn("Duplicate NIC detected: {}", dto.getNic());
            throw new DuplicateParticipantException(dto.getNic());
        }

        String pictureId = UUID.randomUUID().toString();

        Participant participant = participantMapper.toEntity(dto);
        participant.setPicture(pictureId);

        // DB operation first (deferred) — rolls back if file save below throws
        participantRepository.save(participant);
        log.debug("Participant persisted to DB: {}", dto.getNic());

        // Immediate file operation — failure triggers @Transactional rollback
        savePicture(pictureId, dto.getPicture());

        log.info("Participant created successfully: {}", dto.getNic());
        return participantMapper.toResponseDto(participant);
    }

    /**
     * Updates an existing participant.
     *
     * Transaction strategy:
     *  - If a new picture is supplied:
     *    1. Update DB record with new picture UUID (deferred).
     *    2. Write the new picture file (immediate).
     *    3. Failure at step 2 rolls back step 1 — old picture UUID stays in DB.
     *    4. On success, the old picture file is deleted (best-effort: a warning is
     *       logged on failure, but the transaction is NOT rolled back because DB and
     *       new file are already consistent).
     *  - If no new picture is supplied, only DB fields are updated.
     */
    @Override
    @Transactional
    public ParticipantResponseDTO updateParticipant(String nic, ParticipantRequestDTO dto) {
        log.debug("Updating participant with NIC: {}", nic);

        Participant participant = participantRepository.findById(nic)
                .orElseThrow(() -> {
                    log.warn("Participant not found for update: {}", nic);
                    return new ParticipantNotFoundException(nic);
                });

        String oldPictureId = participant.getPicture();
        boolean pictureChanged = dto.getPicture() != null && !dto.getPicture().isEmpty();
        String newPictureId = pictureChanged ? UUID.randomUUID().toString() : oldPictureId;

        participantMapper.updateEntity(dto, participant);
        participant.setPicture(newPictureId);

        // DB update (deferred) — rolls back if new file save below throws
        participantRepository.save(participant);
        log.debug("Participant updated in DB: {}", nic);

        if (pictureChanged) {
            // Save new picture — failure triggers @Transactional rollback
            savePicture(newPictureId, dto.getPicture());
            // Remove old picture — best-effort; DB and new file are already consistent
            tryDeletePicture(oldPictureId);
        }

        log.info("Participant updated successfully: {}", nic);
        return participantMapper.toResponseDto(participant);
    }

    /**
     * Deletes a participant.
     *
     * Transaction strategy:
     *  1. Remove participant record from DB (JPA defers the DELETE until flush/commit).
     *  2. Delete picture file from disk (immediate).
     *  3. If the file delete fails an exception is thrown, which causes
     *     @Transactional to roll back the DB DELETE — neither the record
     *     nor the file is removed.
     *  4. If the file delete succeeds the method returns normally and
     *     @Transactional commits, removing the record from the DB.
     */
    @Override
    @Transactional
    public void deleteParticipant(String nic) {
        log.debug("Deleting participant with NIC: {}", nic);

        Participant participant = participantRepository.findById(nic)
                .orElseThrow(() -> {
                    log.warn("Participant not found for deletion: {}", nic);
                    return new ParticipantNotFoundException(nic);
                });

        String pictureId = participant.getPicture();

        // DB deletion (deferred) — rolls back if file delete below throws
        participantRepository.delete(participant);
        log.debug("Participant marked for deletion in DB: {}", nic);

        // Immediate file deletion — failure triggers @Transactional rollback
        deletePicture(pictureId);

        log.info("Participant deleted successfully: {}", nic);
    }

    @Override
    @Transactional(readOnly = true)
    public ParticipantResponseDTO getParticipant(String nic) {
        log.debug("Fetching participant with NIC: {}", nic);
        return participantRepository.findById(nic)
                .map(participantMapper::toResponseDto)
                .orElseThrow(() -> {
                    log.warn("Participant not found: {}", nic);
                    return new ParticipantNotFoundException(nic);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipantResponseDTO> getAllParticipants() {
        log.debug("Fetching all participants");
        List<ParticipantResponseDTO> participants = participantRepository.findAll()
                .stream()
                .map(participantMapper::toResponseDto)
                .collect(Collectors.toList());
        log.debug("Fetched {} participants", participants.size());
        return participants;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getParticipantPicture(String nic) {
        log.debug("Fetching picture for participant NIC: {}", nic);
        Participant participant = participantRepository.findById(nic)
                .orElseThrow(() -> {
                    log.warn("Participant not found: {}", nic);
                    return new ParticipantNotFoundException(nic);
                });
        Path filePath = storagePath().resolve(participant.getPicture());
        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("Failed to read picture for participant: {}", nic, e);
            throw new FileOperationException("Failed to read picture for participant: " + nic, e);
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Path storagePath() {
        if (storagePath == null) {
            storagePath = Paths.get(storagePathStr);
        }
        try {
            Files.createDirectories(storagePath);
        } catch (IOException e) {
            throw new FileOperationException(
                    "Failed to create storage directory: " + storagePath.toAbsolutePath(), e);
        }
        return storagePath;
    }

    private void savePicture(String pictureId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileOperationException("Picture file must not be empty");
        }
        Path filePath = storagePath().resolve(pictureId);
        try {
            Files.write(filePath, file.getBytes());
            log.debug("Picture saved: {}", filePath);
        } catch (IOException e) {
            log.error("Failed to save picture: {}", filePath, e);
            throw new FileOperationException("Failed to save picture file: " + pictureId, e);
        }
    }

    private void deletePicture(String pictureId) {
        Path filePath = storagePath().resolve(pictureId);
        try {
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.debug("Picture deleted: {}", filePath);
            } else {
                log.warn("Picture file not found on disk (already removed?): {}", filePath);
            }
        } catch (IOException e) {
            log.error("Failed to delete picture: {}", filePath, e);
            throw new FileOperationException("Failed to delete picture file: " + pictureId, e);
        }
    }

    private void tryDeletePicture(String pictureId) {
        try {
            deletePicture(pictureId);
        } catch (FileOperationException e) {
            log.warn("Could not delete old picture file '{}'. Manual cleanup may be required.", pictureId);
        }
    }

}
