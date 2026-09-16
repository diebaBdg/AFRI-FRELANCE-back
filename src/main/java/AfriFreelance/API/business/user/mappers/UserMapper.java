package AfriFreelance.API.business.user.mappers;

import org.mapstruct.Mapper;
import AfriFreelance.API.business.user.User;
import AfriFreelance.API.business.user.dtos.UserDTO;
import AfriFreelance.API.config.EntityMapper;

@Mapper(componentModel = "spring")
public interface UserMapper extends EntityMapper<UserDTO, User> {

}
