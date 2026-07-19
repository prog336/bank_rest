package com.example.bankcards.mapper;

import com.example.bankcards.dto.PagedResponseDTO;
import org.springframework.data.domain.Page;

import java.util.function.Function;

public interface PageMapper {
  <T, R> PagedResponseDTO<R> mapToPagedResponseDTO(Page<T> page, Function<T, R> mapper);
}
