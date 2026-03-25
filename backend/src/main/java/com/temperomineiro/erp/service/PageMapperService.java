package com.temperomineiro.erp.service;

import com.temperomineiro.erp.dto.CommonDto;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class PageMapperService {

    public <T> CommonDto.PageResponse<T> toPageResponse(Page<T> page) {
        return new CommonDto.PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
