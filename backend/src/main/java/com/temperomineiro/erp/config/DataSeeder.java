package com.temperomineiro.erp.config;

import com.temperomineiro.erp.model.Categoria;
import com.temperomineiro.erp.model.DomainEnums.MesaStatus;
import com.temperomineiro.erp.model.DomainEnums.RoleName;
import com.temperomineiro.erp.model.DomainEnums.UnitMeasure;
import com.temperomineiro.erp.model.Estoque;
import com.temperomineiro.erp.model.Mesa;
import com.temperomineiro.erp.model.Produto;
import com.temperomineiro.erp.model.ReceitaProduto;
import com.temperomineiro.erp.model.Restaurante;
import com.temperomineiro.erp.model.Role;
import com.temperomineiro.erp.model.User;
import com.temperomineiro.erp.repository.CategoriaRepository;
import com.temperomineiro.erp.repository.EstoqueRepository;
import com.temperomineiro.erp.repository.MesaRepository;
import com.temperomineiro.erp.repository.ProdutoRepository;
import com.temperomineiro.erp.repository.ReceitaProdutoRepository;
import com.temperomineiro.erp.repository.RestauranteRepository;
import com.temperomineiro.erp.repository.RoleRepository;
import com.temperomineiro.erp.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final RestauranteRepository restauranteRepository;
    private final UserRepository userRepository;
    private final MesaRepository mesaRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;
    private final EstoqueRepository estoqueRepository;
    private final ReceitaProdutoRepository receitaProdutoRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled}")
    private boolean enabled;

    @Override
    public void run(String... args) {
        if (!enabled) {
            return;
        }

        seedRoles();
        if (restauranteRepository.count() > 0) {
            return;
        }

        Restaurante restaurante = restauranteRepository.save(Restaurante.builder()
                .nome("Tempero Mineiro - Demo")
                .slug("tempero-mineiro")
                .ativo(true)
                .build());

        Map<RoleName, Role> roles = roleRepository.findAll().stream().collect(java.util.stream.Collectors.toMap(Role::getName, role -> role));
        userRepository.saveAll(List.of(
                createUser(restaurante, "Administrador", "admin@temperomineiro.com", Set.of(roles.get(RoleName.ADMIN), roles.get(RoleName.GERENTE))),
                createUser(restaurante, "Gerente", "gerente@temperomineiro.com", Set.of(roles.get(RoleName.GERENTE))),
                createUser(restaurante, "Garçom", "garcom@temperomineiro.com", Set.of(roles.get(RoleName.GARCOM))),
                createUser(restaurante, "Cozinha", "cozinha@temperomineiro.com", Set.of(roles.get(RoleName.COZINHA))),
                createUser(restaurante, "Caixa", "caixa@temperomineiro.com", Set.of(roles.get(RoleName.CAIXA)))
        ));

        for (int i = 1; i <= 10; i++) {
            mesaRepository.save(Mesa.builder()
                    .restaurante(restaurante)
                    .nome("Mesa " + i)
                    .capacidade(i <= 4 ? 4 : 6)
                    .status(MesaStatus.LIVRE)
                    .publicToken(UUID.randomUUID().toString().replace("-", ""))
                    .ativa(true)
                    .build());
        }

        Categoria bebidas = categoriaRepository.save(Categoria.builder()
                .restaurante(restaurante)
                .nome("Bebidas")
                .descricao("Sucos, refrigerantes e drinks")
                .ordemExibicao(2)
                .ativa(true)
                .build());
        Categoria pratos = categoriaRepository.save(Categoria.builder()
                .restaurante(restaurante)
                .nome("Pratos")
                .descricao("Pratos principais da casa")
                .ordemExibicao(1)
                .ativa(true)
                .build());
        Categoria sobremesas = categoriaRepository.save(Categoria.builder()
                .restaurante(restaurante)
                .nome("Sobremesas")
                .descricao("Doces e sobremesas mineiras")
                .ordemExibicao(3)
                .ativa(true)
                .build());

        Estoque feijao = estoqueRepository.save(stock(restaurante, "Feijão tropeiro", UnitMeasure.KG, "25.000", "5.000", "18.00"));
        Estoque arroz = estoqueRepository.save(stock(restaurante, "Arroz branco", UnitMeasure.KG, "30.000", "5.000", "9.00"));
        Estoque carne = estoqueRepository.save(stock(restaurante, "Carne de panela", UnitMeasure.KG, "18.000", "4.000", "32.00"));
        Estoque queijo = estoqueRepository.save(stock(restaurante, "Queijo minas", UnitMeasure.KG, "10.000", "2.000", "28.00"));
        Estoque refrigerante = estoqueRepository.save(stock(restaurante, "Refrigerante lata", UnitMeasure.UNIDADE, "120.000", "20.000", "4.50"));
        Estoque doceLeite = estoqueRepository.save(stock(restaurante, "Doce de leite", UnitMeasure.KG, "8.000", "1.500", "22.00"));

        Produto tropeiro = produtoRepository.save(product(restaurante, pratos, "Feijão Tropeiro Completo", "Clássico mineiro com torresmo e couve.", "39.90"));
        Produto panelada = produtoRepository.save(product(restaurante, pratos, "Carne de Panela", "Acompanha arroz e purê artesanal.", "44.90"));
        Produto pudim = produtoRepository.save(product(restaurante, sobremesas, "Pudim da Vó", "Pudim cremoso com calda de caramelo.", "16.90"));
        Produto guarana = produtoRepository.save(product(restaurante, bebidas, "Guaraná Lata", "350ml gelado.", "7.50"));
        Produto limonada = produtoRepository.save(product(restaurante, bebidas, "Limonada da Serra", "Natural com toque de hortelã.", "9.90"));

        receitaProdutoRepository.saveAll(List.of(
                recipe(tropeiro, feijao, "0.350"),
                recipe(tropeiro, arroz, "0.200"),
                recipe(panelada, carne, "0.350"),
                recipe(panelada, arroz, "0.180"),
                recipe(panelada, queijo, "0.050"),
                recipe(pudim, doceLeite, "0.120"),
                recipe(guarana, refrigerante, "1.000"),
                recipe(limonada, doceLeite, "0.010")
        ));
    }

    private void seedRoles() {
        for (RoleName roleName : RoleName.values()) {
            roleRepository.findByName(roleName).orElseGet(() -> roleRepository.save(Role.builder()
                    .name(roleName)
                    .description("Perfil " + roleName.name())
                    .build()));
        }
    }

    private User createUser(Restaurante restaurante, String nome, String email, Set<Role> roles) {
        return User.builder()
                .restaurante(restaurante)
                .nome(nome)
                .email(email)
                .password(passwordEncoder.encode("123456"))
                .ativo(true)
                .roles(roles)
                .build();
    }

    private Estoque stock(Restaurante restaurante, String nome, UnitMeasure unitMeasure, String amount, String min, String cost) {
        return Estoque.builder()
                .restaurante(restaurante)
                .nome(nome)
                .unidadeMedida(unitMeasure)
                .quantidadeAtual(new BigDecimal(amount))
                .quantidadeMinima(new BigDecimal(min))
                .custoUnitario(new BigDecimal(cost))
                .ativo(true)
                .build();
    }

    private Produto product(Restaurante restaurante, Categoria categoria, String nome, String descricao, String preco) {
        String imageUrl = switch (categoria.getNome()) {
            case "Pratos" -> "https://images.unsplash.com/photo-1544025162-d76694265947?auto=format&fit=crop&w=1200&q=80";
            case "Bebidas" -> "https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?auto=format&fit=crop&w=1200&q=80";
            case "Sobremesas" -> "https://images.unsplash.com/photo-1551024601-bec78aea704b?auto=format&fit=crop&w=1200&q=80";
            default -> null;
        };

        return Produto.builder()
                .restaurante(restaurante)
                .categoria(categoria)
                .nome(nome)
                .descricao(descricao)
                .preco(new BigDecimal(preco))
                .disponivel(true)
                .sku("TM-DEMO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .imagemUrl(imageUrl)
                .build();
    }

    private ReceitaProduto recipe(Produto produto, Estoque estoque, String quantity) {
        return ReceitaProduto.builder()
                .produto(produto)
                .estoque(estoque)
                .quantidadeConsumida(new BigDecimal(quantity))
                .build();
    }
}
