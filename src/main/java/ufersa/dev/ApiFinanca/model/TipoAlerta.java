package ufersa.dev.ApiFinanca.model;

public enum TipoAlerta {
    // Alertas ativados por padrão
    GASTO_ACIMA_MEDIA("Gasto acima da média", "Notifica quando o gasto em uma categoria está significativamente acima da média histórica", true),
    META_PROXIMA_VENCIMENTO("Meta próxima do vencimento", "Notifica quando uma meta está próxima da data alvo (7 dias ou menos)", true),
    PROJECAO_SALDO_NEGATIVO("Projeção de saldo negativo", "Notifica quando a projeção indica saldo negativo ao final do mês", true),
    PROGRESSO_META_LENTO("Progresso de meta abaixo do esperado", "Notifica quando o progresso de uma meta está abaixo do esperado em relação ao tempo decorrido", true),
    META_ALCANCADA("Meta alcançada", "Notifica quando uma meta foi alcançada com sucesso", true),
    
    // Alertas desativados por padrão
    GASTO_CATEGORIA_ELEVADO("Gasto elevado em categoria", "Notifica quando o gasto em uma categoria excede um percentual significativo da renda", false),
    ECONOMIA_POSITIVA("Economia acima do esperado", "Notifica quando você economizou mais do que o esperado no período", false);

    private final String nome;
    private final String descricao;
    private final boolean ativoPorPadrao;

    TipoAlerta(String nome, String descricao, boolean ativoPorPadrao) {
        this.nome = nome;
        this.descricao = descricao;
        this.ativoPorPadrao = ativoPorPadrao;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isAtivoPorPadrao() {
        return ativoPorPadrao;
    }
}

