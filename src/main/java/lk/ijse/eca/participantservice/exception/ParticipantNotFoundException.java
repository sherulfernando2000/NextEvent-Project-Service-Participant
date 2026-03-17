package lk.ijse.eca.participantservice.exception;

public class ParticipantNotFoundException extends RuntimeException {

    public ParticipantNotFoundException(String nic) {
        super("Participant with NIC '" + nic + "' not found");
    }
}
