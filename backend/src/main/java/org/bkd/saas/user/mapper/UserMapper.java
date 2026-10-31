package org.bkd.saas.user.mapper;

import org.bkd.saas.user.db.AppUserEntity;
import org.bkd.saas.user.dto.UserDto;
import org.bkd.saas.user.dto.UserWithPasswordDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

  UserDto toUserDto(AppUserEntity appUserEntity);

  UserWithPasswordDto toUserWithPasswordDto(AppUserEntity appUserEntity);
}
