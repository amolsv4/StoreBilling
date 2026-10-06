package com.retailpos.terminal.service;

import com.retailpos.terminal.dto.CreateTerminalRequest;
import com.retailpos.terminal.dto.TerminalResponse;
import com.retailpos.terminal.dto.UpdateTerminalRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TerminalService {

    TerminalResponse create(CreateTerminalRequest request);

    TerminalResponse update(Long id, UpdateTerminalRequest request);

    TerminalResponse getById(Long id);

    Page<TerminalResponse> list(Pageable pageable);

    Page<TerminalResponse> listByStore(Long storeId, Pageable pageable);
}
