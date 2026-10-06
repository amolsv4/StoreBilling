package com.retailpos.terminal.service;

import com.retailpos.common.exception.DuplicateResourceException;
import com.retailpos.common.exception.ResourceNotFoundException;
import com.retailpos.store.entity.Store;
import com.retailpos.store.repository.StoreRepository;
import com.retailpos.terminal.dto.CreateTerminalRequest;
import com.retailpos.terminal.dto.TerminalResponse;
import com.retailpos.terminal.dto.UpdateTerminalRequest;
import com.retailpos.terminal.entity.Terminal;
import com.retailpos.terminal.mapper.TerminalMapper;
import com.retailpos.terminal.repository.TerminalRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TerminalServiceImpl implements TerminalService {

    private final TerminalRepository terminalRepository;
    private final StoreRepository storeRepository;
    private final TerminalMapper terminalMapper;

    public TerminalServiceImpl(TerminalRepository terminalRepository, StoreRepository storeRepository,
                                TerminalMapper terminalMapper) {
        this.terminalRepository = terminalRepository;
        this.storeRepository = storeRepository;
        this.terminalMapper = terminalMapper;
    }

    @Override
    @Transactional
    public TerminalResponse create(CreateTerminalRequest request) {
        if (terminalRepository.existsByStoreIdAndCode(request.storeId(), request.code())) {
            throw new DuplicateResourceException(
                    "A terminal with code '" + request.code() + "' already exists for store id " + request.storeId());
        }
        Store store = storeRepository.findById(request.storeId())
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Store", request.storeId()));

        Terminal terminal = terminalMapper.toEntity(request);
        terminal.setStore(store);
        Terminal saved = terminalRepository.save(terminal);
        return terminalMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TerminalResponse update(Long id, UpdateTerminalRequest request) {
        Terminal terminal = getEntity(id);
        terminalMapper.updateEntityFromRequest(request, terminal);
        if (request.active() != null) {
            terminal.setActive(request.active());
        }
        Terminal saved = terminalRepository.save(terminal);
        return terminalMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TerminalResponse getById(Long id) {
        return terminalMapper.toResponse(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TerminalResponse> list(Pageable pageable) {
        return terminalRepository.findAll(pageable).map(terminalMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TerminalResponse> listByStore(Long storeId, Pageable pageable) {
        return terminalRepository.findAllByStoreId(storeId, pageable).map(terminalMapper::toResponse);
    }

    private Terminal getEntity(Long id) {
        return terminalRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Terminal", id));
    }
}
