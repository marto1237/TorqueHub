package torquehub.torquehub.domain.mapper;

import org.mapstruct.Mapper;
import torquehub.torquehub.domain.model.Bookmark;
import torquehub.torquehub.domain.request.BookmarkDtos.BookmarkRequest;
import torquehub.torquehub.domain.response.BookmarkDtos.BookmarkResponse;

@Mapper(componentModel = "spring")
public interface BookmarkMapper {

    Bookmark toEntity(BookmarkRequest bookmarkRequest);
    BookmarkResponse toResponse(Bookmark bookmark);
}
