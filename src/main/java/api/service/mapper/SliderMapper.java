package api.service.mapper;

import api.domain.Slider;
import api.domain.User;
import api.service.dto.SliderDTO;
import api.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Slider} and its DTO {@link SliderDTO}.
 */
@Mapper(componentModel = "spring")
public interface SliderMapper extends EntityMapper<SliderDTO, Slider> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    SliderDTO toDto(Slider s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
