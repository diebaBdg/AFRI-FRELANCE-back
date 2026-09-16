package AfriFreelance.API.auth;

import AfriFreelance.API.auth.dtos.LoginRequest;
import AfriFreelance.API.auth.dtos.LoginResponse;

public interface IAuthService {
    LoginResponse login(LoginRequest loginRequest);
}