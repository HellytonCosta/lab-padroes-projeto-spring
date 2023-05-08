package one.digitalinnovation.gof.service;

import feign.Client;
import one.digitalinnovation.gof.model.Cliente;
import one.digitalinnovation.gof.model.ClienteRepository;
import one.digitalinnovation.gof.model.Endereco;
import one.digitalinnovation.gof.model.EnderecoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClienteServiceImpl implements ClienteService{

    //  Singleton: Injetar os componentes do Spring com @Autowired
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private EnderecoRepository enderecoRepository;
    @Autowired
    private ViaCepService viaCepService;
    //  Strategy: Implementar os métodos definidos na interface
    //  Facade: Abstrair integrações com subsistemas, provendo interface simples

    @Override
    public Iterable<Cliente> buscarTodos() {
        return clienteRepository.findAll();
    }
    @Override
    public Cliente buscarPorId(Long id) {
        // buscar Cliente por ID
        Optional<Cliente> cliente = clienteRepository.findById(id);
        return cliente.get();
    }

    @Override
    public void inserir(Cliente cliente) {
        salvarClienteComCep(cliente);
    }

    @Override
    public void atualizar(Long id, Cliente cliente) {
        // buscar cliente por ID, caso exista:
        Optional<Cliente> clienteBd = clienteRepository.findById(id);
        if(clienteBd.isPresent()){
            salvarClienteComCep(cliente);
        }
    }

    @Override
    public void deletar(Long id) {
        // Deletar cliente por ID
        clienteRepository.deleteById(id);
    }



    private void salvarClienteComCep(Cliente cliente) {
        // verificar se o endereço do cliente já existe pelo CEP
        String cep = cliente.getEndereco().getCep();
        Endereco endereco = enderecoRepository.findById(cep).orElseGet(() -> {
            //  Caso não, integrar com o viaCEP e persistir retorno
            Endereco novoEndereco = viaCepService.consultarCep(cep);
            enderecoRepository.save(novoEndereco);
            return novoEndereco;
        });
        cliente.setEndereco(endereco);
        // Inserir Cliente, vinculando o endereço (novo ou exist)
        clienteRepository.save(cliente);
    }
}
