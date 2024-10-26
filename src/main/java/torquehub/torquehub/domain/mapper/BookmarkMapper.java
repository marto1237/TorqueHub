package torquehub.torquehub.domain.mapper;

import org.mapstruct.Mapper;
import torquehub.torquehub.domain.model.jpa_models.JpaBookmark;
import torquehub.torquehub.domain.request.bookmark_dtos.BookmarkQuestionRequest;
import torquehub.torquehub.domain.request.bookmark_dtos.BookmarkRequest;
import torquehub.torquehub.domain.response.bookmark_dtos.BookmarkResponse;

@Mapper(componentModel = "spring")
public interface BookmarkMapper {

    JpaBookmark toEntity(BookmarkRequest bookmarkRequest);
    JpaBookmark toEntity(BookmarkQuestionRequest bookmarkResponse);

    BookmarkResponse toResponse(JpaBookmark jpaBookmark);
}
