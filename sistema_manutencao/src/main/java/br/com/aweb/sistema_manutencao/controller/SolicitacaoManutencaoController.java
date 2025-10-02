package br.com.aweb.sistema_manutencao.controller;

import br.com.aweb.sistema_manutencao.model.SolicitacaoManutencao;
import br.com.aweb.sistema_manutencao.model.StatusManutencao;
import br.com.aweb.sistema_manutencao.repository.SolicitacaoManutencaoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequestMapping("/solicitacao")
public class SolicitacaoManutencaoController {

    @Autowired
    private SolicitacaoManutencaoRepository solicitacaoRepository;

    @GetMapping
    public ModelAndView list() {
        return new ModelAndView("list", Map.of("solicitacoes", 
                solicitacaoRepository.findByOrderByDataSolicitacaoDesc()));
    }

    @GetMapping("/create")
    public ModelAndView create() {
        return new ModelAndView("form", Map.of("solicitacao", new SolicitacaoManutencao()));
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("solicitacao") SolicitacaoManutencao solicitacao, 
                        BindingResult result) {
        
        if (solicitacao.getDeadline() != null && 
            solicitacao.getDeadline().isBefore(LocalDate.now())) {
            result.rejectValue("deadline", "error.deadline", "O prazo deve ser hoje ou futura");
        }
        
        if (result.hasErrors()) {
            return "form";
        }
        
        // Se o status for FINALIZADA, define a data de finalização
        if (solicitacao.getStatus() == StatusManutencao.FINALIZADA) {
            solicitacao.setDataFinalizacao(LocalDateTime.now());
        }
        
        solicitacaoRepository.save(solicitacao);
        return "redirect:/solicitacao";
    }

    @GetMapping("/edit/{id}")
public ModelAndView edit(@PathVariable Long id) {
    var optionalSolicitacao = solicitacaoRepository.findById(id);
    
    if (optionalSolicitacao.isPresent()) {
        var solicitacao = optionalSolicitacao.get();
        
        // IMPEDE EDIÇÃO SE ESTIVER FINALIZADA
        if (solicitacao.getStatus() != StatusManutencao.FINALIZADA) {
            return new ModelAndView("form", Map.of("solicitacao", solicitacao));
        } else {
            System.out.println("Tentativa de editar solicitação finalizada ID: " + id);
            return new ModelAndView("redirect:/solicitacao");
        }
    }
    
    System.out.println("Solicitação não encontrada para edição ID: " + id);
    return new ModelAndView("redirect:/solicitacao");
}

    @PostMapping("/edit/{id}")
public String edit(@PathVariable Long id,
                  @Valid @ModelAttribute("solicitacao") SolicitacaoManutencao solicitacao, 
                  BindingResult result) {
    
    if (solicitacao.getDeadline() != null && 
        solicitacao.getDeadline().isBefore(LocalDate.now())) {
        result.rejectValue("deadline", "error.deadline", "O prazo deve ser hoje ou futura");
    }
    
    if (result.hasErrors()) {
        return "form";
    }
    
    var existingSolicitacao = solicitacaoRepository.findById(id);
    // IMPEDE EDIÇÃO SE ESTIVER FINALIZADA
    if (existingSolicitacao.isPresent() && 
        existingSolicitacao.get().getStatus() != StatusManutencao.FINALIZADA) {
            
            // Preserva algumas informações que não devem ser alteradas
            solicitacao.setDataSolicitacao(existingSolicitacao.get().getDataSolicitacao());
            solicitacao.setCreatedAt(existingSolicitacao.get().getCreatedAt());
            
            // Lógica para data de finalização baseada no status
            if (solicitacao.getStatus() == StatusManutencao.FINALIZADA) {
                // Se está mudando para FINALIZADA, define a data de finalização
                if (existingSolicitacao.get().getStatus() != StatusManutencao.FINALIZADA) {
                    solicitacao.setDataFinalizacao(LocalDateTime.now());
                } else {
                    // Se já estava FINALIZADA, mantém a data existente
                    solicitacao.setDataFinalizacao(existingSolicitacao.get().getDataFinalizacao());
                }
            } else {
                // Se está mudando para outro status, remove a data de finalização
                solicitacao.setDataFinalizacao(null);
            }
            
               solicitacaoRepository.save(solicitacao);
        return "redirect:/solicitacao";
    }
    
    return "redirect:/solicitacao";
}

    @GetMapping("/delete/{id}")
    public ModelAndView delete(@PathVariable Long id) {
        var solicitacao = solicitacaoRepository.findById(id);
        if (solicitacao.isPresent()) {
            return new ModelAndView("delete", Map.of("solicitacao", solicitacao.get()));
        }
        return new ModelAndView("redirect:/solicitacao");
    }

    @PostMapping("/delete/{id}")
    public String delete(@ModelAttribute SolicitacaoManutencao solicitacao) {
        solicitacaoRepository.delete(solicitacao);
        return "redirect:/solicitacao";
    }

    @PostMapping("/finalizar/{id}")
    public String finalizar(@PathVariable Long id) {
        var optionalSolicitacao = solicitacaoRepository.findById(id);
        
        if (optionalSolicitacao.isPresent()) {
            var solicitacao = optionalSolicitacao.get();
            
            if (solicitacao.getStatus() != StatusManutencao.FINALIZADA) {
                solicitacao.finalizar();
                solicitacaoRepository.save(solicitacao);
                System.out.println("Solicitação finalizada ID: " + id);
            }
            
            return "redirect:/solicitacao";
        }
        
        System.out.println("Solicitação não encontrada para finalizar ID: " + id);
        return "redirect:/solicitacao";
    }
}