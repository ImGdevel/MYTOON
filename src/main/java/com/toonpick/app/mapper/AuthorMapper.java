package com.toonpick.app.mapper;

import com.toonpick.app.dto.AuthorDTO;
import com.toonpick.app.entity.Author;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "spring")
public interface AuthorMapper {
    AuthorMapper INSTANCE = Mappers.getMapper(AuthorMapper.class);

    AuthorDTO authorToAuthorDto(Author author);

    Author authorDtoToAuthor(AuthorDTO authorDTO);
}