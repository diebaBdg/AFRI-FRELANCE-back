package AfriFreelance.API.business.user.mappers;

import org.mapstruct.Mapper;
import sn.gainde2000.senegallicenseplatformbackend.business.user.User;
import sn.gainde2000.senegallicenseplatformbackend.business.user.dtos.UserDTO;
import sn.gainde2000.senegallicenseplatformbackend.config.EntityMapper;

@Mapper(componentModel = "spring")
public interface UserMapper extends EntityMapper<UserDTO, User> {

}