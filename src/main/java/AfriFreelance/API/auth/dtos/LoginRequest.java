package AfriFreelance.API.auth.dtos;

public record LoginRequest(
        String login,
        String password
) {}