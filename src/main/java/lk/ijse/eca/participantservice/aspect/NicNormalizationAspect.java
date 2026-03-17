package lk.ijse.eca.participantservice.aspect;

import lk.ijse.eca.participantservice.dto.ParticipantRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class NicNormalizationAspect {

    private static final String NIC_PATTERN = "^\\d{9}[vV]$";

    @Around("execution(* lk.ijse.eca.participantservice.service.ParticipantService.*(..))")
    public Object normalizeNicArguments(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof String str && str.matches(NIC_PATTERN)) {
                args[i] = normalize(str);
                log.debug("Normalized NIC argument: {} -> {}", str, args[i]);
            } else if (args[i] instanceof ParticipantRequestDTO dto && dto.getNic() != null) {
                dto.setNic(normalize(dto.getNic()));
                log.debug("Normalized NIC in DTO: {}", dto.getNic());
            }
        }

        return joinPoint.proceed(args);
    }

    private String normalize(String nic) {
        return nic.substring(0, 9) + Character.toUpperCase(nic.charAt(9));
    }
}
