package com.temperomineiro.erp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.temperomineiro.erp.dto.CatalogDto;
import com.temperomineiro.erp.dto.MesaDto;
import com.temperomineiro.erp.model.DomainEnums.MesaStatus;
import com.temperomineiro.erp.model.DomainEnums.RoleName;
import com.temperomineiro.erp.model.Restaurante;
import com.temperomineiro.erp.model.Role;
import com.temperomineiro.erp.model.User;
import com.temperomineiro.erp.repository.RestauranteRepository;
import com.temperomineiro.erp.repository.RoleRepository;
import com.temperomineiro.erp.repository.UserRepository;
import com.temperomineiro.erp.security.CustomUserDetails;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PublicMenuServiceIntegrationTest {

    private static final String SAMPLE_IMAGE = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO9W6h8AAAAASUVORK5CYII=";

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RestauranteRepository restauranteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private MesaService mesaService;

    @Autowired
    private PublicMenuService publicMenuService;

    private Restaurante restaurante;

    @BeforeEach
    void setUp() {
        for (RoleName roleName : RoleName.values()) {
            roleRepository.findByName(roleName).orElseGet(() -> roleRepository.save(Role.builder()
                    .name(roleName)
                    .description("Perfil " + roleName.name())
                    .build()));
        }

        restaurante = restauranteRepository.save(Restaurante.builder()
                .nome("Tempero Menu")
                .slug("tempero-menu")
                .ativo(true)
                .build());

        User user = userRepository.save(User.builder()
                .restaurante(restaurante)
                .nome("Admin Menu")
                .email("admin.menu@temperomineiro.com")
                .password(passwordEncoder.encode("Senha@2026"))
                .ativo(true)
                .roles(Set.of(roleRepository.findByName(RoleName.ADMIN).orElseThrow()))
                .build());

        CustomUserDetails userDetails = new CustomUserDetails(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
    }

    @Test
    void shouldPrioritizePlatesThenDrinksThenDessertsOnPublicMenu() {
        var bebidas = categoriaService.create(new CatalogDto.CategoriaRequest("Bebidas", "Bebidas da casa", 1, true));
        var sobremesas = categoriaService.create(new CatalogDto.CategoriaRequest("Sobremesas", "Doces da casa", 2, true));
        var pratos = categoriaService.create(new CatalogDto.CategoriaRequest("Pratos", "Pratos principais", 3, true));

        produtoService.create(new CatalogDto.ProdutoRequest(
                bebidas.id(),
                "Suco da Casa",
                "Natural",
                new BigDecimal("9.90"),
                null,
                true,
                List.of()
        ));
        produtoService.create(new CatalogDto.ProdutoRequest(
                pratos.id(),
                "Feijao Tropeiro",
                "Completo",
                new BigDecimal("39.90"),
                SAMPLE_IMAGE,
                true,
                List.of()
        ));
        produtoService.create(new CatalogDto.ProdutoRequest(
                sobremesas.id(),
                "Pudim",
                "Cremoso",
                new BigDecimal("16.90"),
                null,
                true,
                List.of()
        ));

        MesaDto.MesaResponse mesa = mesaService.create(new MesaDto.MesaRequest("Mesa 1", 4, MesaStatus.LIVRE, true));
        var publicMenu = publicMenuService.getMenu(restaurante.getSlug(), mesa.publicToken());

        assertEquals(List.of("Pratos", "Bebidas", "Sobremesas"), publicMenu.categorias().stream().map(category -> category.nome()).toList());
        assertEquals(SAMPLE_IMAGE, publicMenu.categorias().get(0).produtos().get(0).imagemUrl());
    }
}
