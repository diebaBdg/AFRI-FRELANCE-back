package AfriFreelance.API.auth.dtos;

import java.util.Set;
import java.util.UUID;

public record LoginResponse(
        UUID id,
        String username,
        String email,
        String fullName,
        String token,
        Set<String> roles
) {}