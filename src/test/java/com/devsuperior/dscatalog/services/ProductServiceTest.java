package com.devsuperior.dscatalog.services;

import com.devsuperior.dscatalog.Factory;
import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repositories.CategoryRepository;
import com.devsuperior.dscatalog.repositories.ProductRepository;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class ProductServiceTest {

    @InjectMocks
    private ProductService service;
    /*
     * Usar quando a classe de teste não carrega o contexto da aplicação.
     *  É mais rápido e enxuto.@ExtendWith
     */

    @Mock
    private ProductRepository repository;

    @Mock
    private CategoryRepository categoryRepository;

    private long existingId;
    private long nonExistingId;
    private long dependentId;
    private PageImpl<Product> page; //Tipo concreto que representa uma pagina de dados
    private Product product;
    private Category category;
    private ProductDTO productDTO;


    @BeforeEach
    void setUp() throws Exception {
        existingId = 1L;
        nonExistingId = 2L;
        dependentId = 3L;
        category = Factory.createCategory();
        product = Factory.createProduct();
        productDTO = Factory.createProductDTO();
        page = new PageImpl<>(List.of(product));

        // Ao criar um mock e necessário configurar o comportamento simulado dele.
        //Simular o comportamento de uma pagina
        Mockito.when(repository.findAll((Pageable)ArgumentMatchers.any())).thenReturn(page);

        //Simular o comportamento de save
        Mockito.when(repository.save(ArgumentMatchers.any())).thenReturn(product);

        //Simular a busca por id existente e inexistente
        Mockito.when(repository.findById(existingId)).thenReturn(Optional.of(product)); // .of recebe um objeto
        Mockito.when(repository.findById(nonExistingId)).thenReturn(Optional.empty()); // empty instancia um Optional vazio

        //Simular o comportamento do update com id existente e inexistente realizando GetOne
        Mockito.when(repository.getOne(existingId)).thenReturn(product);
        Mockito.when(repository.getOne(nonExistingId)).thenThrow(EntityNotFoundException.class);

        Mockito.when(categoryRepository.getOne(existingId)).thenReturn(category);
        Mockito.when(categoryRepository.getOne(nonExistingId)).thenThrow(EntityNotFoundException.class);

        doNothing().when(repository).deleteById(existingId);
        doThrow(EmptyResultDataAccessException.class).when(repository).deleteById(nonExistingId);
        doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentId);
    }

    //Verificar erro de id
    @Test
    public void updateShouldReturnProductDTOWhenIdExists() {
        //retornar um ProductDTO quando o id existir
        //	ProductDTO productDTO = Factory.createProductDTO(); //Já esta instanciado

        //Ação
        ProductDTO result = service.update(existingId, productDTO);

        // Verificação
        Assertions.assertNotNull(result);

    }

    @Test
    public void updateShouThrowlResourceNotFoundExceptionWhenIdDoesNotExist() {
        //lançar ResourceNotFoundException quando o id não existir
        //ProductDTO productDTO = Factory.createProductDTO(); //Já esta instanciado

        //Verificação
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            //Ação
            service.update(nonExistingId, productDTO);
        });
    }

    @Test
    public void findByIdShouThrowlResourceNotFoundExceptionWhenIdDoesNotExist() {
        //lançar ResourceNotFoundException quando o id não existir

        //Verificação
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            //Ação
            ProductDTO result = service.findById(nonExistingId);
        });
    }


    @Test
    public void findByIdShouldReturnProductDTOWhenIdExists() {
        //retornar um ProductDTO quando o id existir

        ProductDTO result = service.findById(existingId);

        Assertions.assertNotNull(result);

    }

    @Test
    public void findAllPagedShouldReturnPage() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<ProductDTO> result = service.findAllPaged(pageable);
        assertNotNull(result);
        verify(repository, Mockito.times(1)).findAll(pageable);

    }

    @Test
    public void deleteShouldThrowDatabaseExceptionWhenIdDoesNotExists() {


        //Verificação
        Assertions.assertThrows(DatabaseException.class, () -> {
            //Ação
            service.delete(dependentId);
        });

        //Verificação
        verify(repository, times(1)).deleteById(dependentId);

    }

    @Test
    public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExists() {


        //Verificação
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            //Ação
            service.delete(nonExistingId);
        });

        //Verificação
        verify(repository, times(1)).deleteById(nonExistingId);

    }

    @Test
    public void deleteShouldDoNothingWhenIdExists() {


        //Verificação
        Assertions.assertDoesNotThrow(() -> {
            //Ação
            service.delete(existingId);
        });

        //Verificação
        verify(repository, times(1)).deleteById(existingId);

    }

}