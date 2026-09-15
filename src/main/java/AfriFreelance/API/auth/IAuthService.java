package AfriFreelance.API.auth;

import sn.gainde2000.senegallicenseplatformbackend.auth.dtos.LoginRequest;
import sn.gainde2000.senegallicenseplatformbackend.auth.dtos.LoginResponse;

public interface IAuthService {
    LoginResponse login(LoginRequest loginRequest);
}