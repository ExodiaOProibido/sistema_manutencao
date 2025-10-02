package br.com.aweb.sistema_manutencao.repository;

import br.com.aweb.sistema_manutencao.model.SolicitacaoManutencao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SolicitacaoManutencaoRepository extends JpaRepository<SolicitacaoManutencao, Long> {
    
    List<SolicitacaoManutencao> findByOrderByDataSolicitacaoDesc();
    
    List<SolicitacaoManutencao> findByStatusOrderByDataSolicitacaoDesc(String status);
}