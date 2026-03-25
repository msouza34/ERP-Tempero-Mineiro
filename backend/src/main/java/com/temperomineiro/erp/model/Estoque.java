package com.temperomineiro.erp.model;

import com.temperomineiro.erp.model.DomainEnums.UnitMeasure;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "estoques")
public class Estoque extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurante_id")
    private Restaurante restaurante;

    @Column(nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UnitMeasure unidadeMedida;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantidadeAtual;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantidadeMinima;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal custoUnitario;

    @Column(nullable = false)
    private boolean ativo;
}

