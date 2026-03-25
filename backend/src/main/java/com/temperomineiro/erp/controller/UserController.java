package com.temperomineiro.erp.controller;

import com.temperomineiro.erp.dto.AuthDto;
import com.temperomineiro.erp.dto.CommonDto;
import com.temperomineiro.erp.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
@Tag(name = "Usuarios")
public class UserController {

    private final UserService userService;

    @GetMapping
    public CommonDto.PageResponse<AuthDto.UserSummary> list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return userService.listUsers(search, page, size);
    }

    @PostMapping
    public AuthDto.UserSummary create(@Valid @RequestBody AuthDto.CreateUserRequest request) {
        return userService.createUser(request);
    }

    @PutMapping("/{id}")
    public AuthDto.UserSummary update(@PathVariable Long id, @Valid @RequestBody AuthDto.UpdateUserRequest request) {
        return userService.updateUser(id, request);
    }
}
