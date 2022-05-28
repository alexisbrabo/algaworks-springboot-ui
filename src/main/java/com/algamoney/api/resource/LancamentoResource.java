package com.algamoney.api.resource;

import com.algamoney.api.exceptionHandler.AlgamoneyExceptionHandler;
import com.algamoney.api.model.Lancamento;
import com.algamoney.api.repository.LancamentoRepository;
import com.algamoney.api.repository.filter.LancamentoFilter;
import com.algamoney.api.repository.projection.ResumoLancamento;
import com.algamoney.api.service.LancamentoService;
import com.algamoney.api.service.exception.PessoaInexistenteOuInativaException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/lancamentos")
public class LancamentoResource {

    private final LancamentoRepository lancamentoRepository;
    private final LancamentoService lancamentoService;
    private final MessageSource messageSource;

    public LancamentoResource(LancamentoRepository lancamentoRepository, LancamentoService lancamentoService, MessageSource messageSource) {
        this.lancamentoRepository = lancamentoRepository;
        this.lancamentoService = lancamentoService;
        this.messageSource = messageSource;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_PESQUISAR_LANCAMENTO') and hasAuthority('SCOPE_read')" )
    public Page<Lancamento> listar(LancamentoFilter lancamentoFilter, Pageable pageable) {
        return lancamentoRepository.filtrar(lancamentoFilter, pageable);
    }

    @GetMapping(params = "resumo")
    @PreAuthorize("hasAuthority('ROLE_PESQUISAR_LANCAMENTO') and hasAuthority('SCOPE_read')" )
    public Page<ResumoLancamento> resumir(LancamentoFilter lancamentoFilter, Pageable pageable) {
        return lancamentoRepository.resumir(lancamentoFilter, pageable);
    }

    @GetMapping("/{codigo}")
    @PreAuthorize("hasAuthority('ROLE_PESQUISAR_LANCAMENTO') and hasAuthority('SCOPE_read')" )
    public ResponseEntity<Lancamento> buscarPeloCodigo(@PathVariable Long codigo) {
        return lancamentoRepository.findById(codigo).isPresent() ? ResponseEntity.ok(lancamentoRepository.findById(codigo).get()) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{codigo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_REMOVER_LANCAMENTO') and hasAuthority('SCOPE_write')" )
    public void remover(@PathVariable Long codigo) {
        lancamentoRepository.deleteById(codigo);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CADASTRAR_LANCAMENTO') and hasAuthority('SCOPE_write')" )
    public ResponseEntity<Lancamento> criar(@Valid @RequestBody Lancamento lancamento) {
        Lancamento lancamentoSalvo = lancamentoService.salvar(lancamento);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri().path("/{codigo}").buildAndExpand(lancamentoSalvo.getCodigo()).toUri();

        return ResponseEntity.created(uri).body(lancamentoSalvo);
    }

    @ExceptionHandler({PessoaInexistenteOuInativaException.class})
    public ResponseEntity<Object> handlePessoaInexistenteOuInativaException(PessoaInexistenteOuInativaException ex) {
        String mensagemUsuario = messageSource.getMessage("pessoa.inexistente-ou-inativa", null, LocaleContextHolder.getLocale());
        String mensagemDesenvolvedor = ex.toString();
        List<AlgamoneyExceptionHandler.Erro> erros = List.of(new AlgamoneyExceptionHandler.Erro(mensagemUsuario, mensagemDesenvolvedor));
        return ResponseEntity.badRequest().body(erros);
    }

}
