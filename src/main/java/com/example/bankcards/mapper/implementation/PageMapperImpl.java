package com.example.bankcards.mapper.implementation;

import com.example.bankcards.dto.PagedResponseDTO;
import com.example.bankcards.mapper.PageMapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
public class PageMapperImpl implements PageMapper {
  @Override
  public <T, R> PagedResponseDTO<R> mapToPagedResponseDTO(Page<T> page, Function<T, R> mapper) {
    List<R> content = page.getContent().stream().map(mapper).toList();

    return new PagedResponseDTO<>(
      content,
      page.getNumber(),
      page.getSize(),
      page.getTotalElements(),
      page.getTotalPages(),
      page.isLast(),
      page.isFirst()
    );
  }
}
