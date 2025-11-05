package ufersa.dev.ApiFinanca.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ufersa.dev.ApiFinanca.model.Categoria;
import ufersa.dev.ApiFinanca.model.TipoTransacao;
import ufersa.dev.ApiFinanca.repository.CategoriaRepository;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CategoriaRepository categoriaRepository;

    public DataSeeder(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        seedCategoriasPadrao();
    }

    private void seedCategoriasPadrao() {
        List<Categoria> categoriasPadrao = new ArrayList<>();

        // Categorias de DESPESA
        adicionarSeNaoExiste(categoriasPadrao, "Alimentação", TipoTransacao.DESPESA);
        adicionarSeNaoExiste(categoriasPadrao, "Moradia", TipoTransacao.DESPESA);
        adicionarSeNaoExiste(categoriasPadrao, "Transporte", TipoTransacao.DESPESA);
        adicionarSeNaoExiste(categoriasPadrao, "Lazer", TipoTransacao.DESPESA);
        adicionarSeNaoExiste(categoriasPadrao, "Saúde", TipoTransacao.DESPESA);
        adicionarSeNaoExiste(categoriasPadrao, "Educação", TipoTransacao.DESPESA);
        adicionarSeNaoExiste(categoriasPadrao, "Vestuário", TipoTransacao.DESPESA);
        adicionarSeNaoExiste(categoriasPadrao, "Contas e Serviços", TipoTransacao.DESPESA);

        // Categorias de RECEITA
        adicionarSeNaoExiste(categoriasPadrao, "Salário", TipoTransacao.RECEITA);
        adicionarSeNaoExiste(categoriasPadrao, "Investimentos", TipoTransacao.RECEITA);
        adicionarSeNaoExiste(categoriasPadrao, "Freelance", TipoTransacao.RECEITA);
        adicionarSeNaoExiste(categoriasPadrao, "Outros", TipoTransacao.RECEITA);

        if (!categoriasPadrao.isEmpty()) {
            categoriaRepository.saveAll(categoriasPadrao);
            System.out.println("✓ Categorias padrão criadas com sucesso: " + categoriasPadrao.size() + " categorias");
        } else {
            System.out.println("✓ Categorias padrão já existem no banco de dados");
        }
    }

    private void adicionarSeNaoExiste(List<Categoria> lista, String nome, TipoTransacao tipo) {
        if (!categoriaRepository.existsByNomeAndTipoAndUserIsNull(nome, tipo)) {
            Categoria categoria = new Categoria();
            categoria.setNome(nome);
            categoria.setTipo(tipo);
            categoria.setUser(null); // Categoria padrão do sistema
            lista.add(categoria);
        }
    }

}

