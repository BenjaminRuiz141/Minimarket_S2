package com.minimarket.service.impl;

import com.minimarket.entity.Categoria;
import com.minimarket.repository.CategoriaRepository;
import com.minimarket.service.CategoriaService;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Service
public class CategoriaServiceImpl implements CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Override
    public List<Categoria> findAll() {
        return categoriaRepository.findAll();
    }

    @Override
    public Categoria findById(Long id) {
        return categoriaRepository.findById(id).orElse(null);
    }

    @Override
    public Categoria save(Categoria categoria) {
        if (categoria.getNombre() != null) {
            categoria.setNombre(Jsoup.clean(categoria.getNombre(), Safelist.none()));
        }
        log.info("Guardando/Actualizando categoría: {}", categoria.getNombre());
        return categoriaRepository.save(categoria);
    }

    @Override
    public void deleteById(Long id) {
        log.info("Eliminando categoría con ID: {}", id);
        categoriaRepository.deleteById(id);
    }
}
