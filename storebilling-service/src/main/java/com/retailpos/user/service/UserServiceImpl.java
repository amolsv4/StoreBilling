package com.retailpos.user.service;

import com.retailpos.common.exception.DuplicateResourceException;
import com.retailpos.common.exception.ResourceNotFoundException;
import com.retailpos.organization.entity.Organization;
import com.retailpos.organization.repository.OrganizationRepository;
import com.retailpos.role.entity.Role;
import com.retailpos.role.entity.UserRole;
import com.retailpos.role.repository.RoleRepository;
import com.retailpos.role.repository.UserRoleRepository;
import com.retailpos.store.entity.Store;
import com.retailpos.store.repository.StoreRepository;
import com.retailpos.user.dto.CreateUserRequest;
import com.retailpos.user.dto.UpdateUserRequest;
import com.retailpos.user.dto.UserResponse;
import com.retailpos.user.entity.User;
import com.retailpos.user.entity.UserStoreAccess;
import com.retailpos.user.mapper.UserMapper;
import com.retailpos.user.repository.UserRepository;
import com.retailpos.user.repository.UserStoreAccessRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final StoreRepository storeRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserStoreAccessRepository userStoreAccessRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, OrganizationRepository organizationRepository,
                            StoreRepository storeRepository, RoleRepository roleRepository,
                            UserRoleRepository userRoleRepository,
                            UserStoreAccessRepository userStoreAccessRepository,
                            UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.storeRepository = storeRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.userStoreAccessRepository = userStoreAccessRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("A user with username '" + request.username() + "' already exists.");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("A user with email '" + request.email() + "' already exists.");
        }

        User user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        if (request.organizationId() != null) {
            Organization organization = organizationRepository.findById(request.organizationId())
                    .orElseThrow(() -> ResourceNotFoundException.forEntity("Organization", request.organizationId()));
            user.setOrganization(organization);
        }
        User saved = userRepository.save(user);

        assignRoles(saved, request.roleCodes());
        assignStoreAccess(saved, request.storeIds());

        return toResponseWithAssignments(saved);
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request) {
        User user = getEntity(id);
        userMapper.updateEntityFromRequest(request, user);
        user.setAllStoreAccess(request.allStoreAccess());
        user.setActive(request.active());
        user.setAccountLocked(request.accountLocked());

        if (request.organizationId() != null) {
            Organization organization = organizationRepository.findById(request.organizationId())
                    .orElseThrow(() -> ResourceNotFoundException.forEntity("Organization", request.organizationId()));
            user.setOrganization(organization);
        } else {
            user.setOrganization(null);
        }
        User saved = userRepository.save(user);

        assignRoles(saved, request.roleCodes());
        assignStoreAccess(saved, request.storeIds());

        return toResponseWithAssignments(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return toResponseWithAssignments(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> list(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toResponseWithAssignments);
    }

    /**
     * Resyncs user_role rows to exactly {@code roleCodes}. Like role_permission, user_role is a
     * pure join/association table, so replacing rows on reassignment is the standard approach and
     * does not conflict with the soft-delete-only policy for primary entities.
     */
    private void assignRoles(User user, List<String> roleCodes) {
        if (roleCodes == null) {
            return;
        }
        List<UserRole> existing = userRoleRepository.findAllByUserId(user.getId());
        if (!existing.isEmpty()) {
            userRoleRepository.deleteAll(existing);
        }
        for (String roleCode : roleCodes) {
            Role role = roleRepository.findByCode(roleCode)
                    .orElseThrow(() -> ResourceNotFoundException.forEntity("Role", roleCode));
            UserRole userRole = new UserRole();
            userRole.setUser(user);
            userRole.setRole(role);
            userRoleRepository.save(userRole);
        }
    }

    private void assignStoreAccess(User user, List<Long> storeIds) {
        if (storeIds == null) {
            return;
        }
        List<UserStoreAccess> existing = userStoreAccessRepository.findAllByUserId(user.getId());
        if (!existing.isEmpty()) {
            userStoreAccessRepository.deleteAll(existing);
        }
        for (Long storeId : storeIds) {
            Store store = storeRepository.findById(storeId)
                    .orElseThrow(() -> ResourceNotFoundException.forEntity("Store", storeId));
            UserStoreAccess access = new UserStoreAccess();
            access.setUser(user);
            access.setStore(store);
            userStoreAccessRepository.save(access);
        }
    }

    private UserResponse toResponseWithAssignments(User user) {
        List<String> roleCodes = userRoleRepository.findAllByUserId(user.getId()).stream()
                .map(ur -> ur.getRole().getCode())
                .toList();
        List<Long> storeIds = user.isAllStoreAccess()
                ? Collections.emptyList()
                : userStoreAccessRepository.findAllByUserId(user.getId()).stream()
                        .map(access -> access.getStore().getId())
                        .toList();
        UserResponse base = userMapper.toResponse(user);
        return new UserResponse(base.id(), base.username(), base.email(), base.firstName(), base.lastName(),
                base.phone(), base.organizationId(), base.allStoreAccess(), base.active(), base.accountLocked(),
                base.lastLoginDate(), roleCodes, storeIds, base.createdDate(), base.updatedDate());
    }

    private User getEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("User", id));
    }
}
