package lk.ijse.eca.participantservice.exception;

public class DuplicateParticipantException extends RuntimeException {

    public DuplicateParticipantException(String nic) {
        super("Participant with NIC '" + nic + "' already exists");
    }
}
