package com.minimarket.service.impl;

import com.minimarket.entity.Producto;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.service.ProductoService;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Service
public class ProductoServiceImpl implements ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Override
    public List<Producto> findAll() {
        return productoRepository.findAll();
    }

    @Override
    public Producto findById(Long id) {
        return productoRepository.findById(id).orElse(null);
    }

    @Override
    public Producto save(Producto producto) {
        if (producto.getNombre() != null) {
            producto.setNombre(Jsoup.clean(producto.getNombre(), Safelist.none()));
        }
        log.info("Guardando/Actualizando producto: {}", producto.getNombre());
        return productoRepository.save(producto);
    }

    @Override
    public void deleteById(Long id) {
        log.info("Eliminando producto con ID: {}", id);
        productoRepository.deleteById(id);
    }

    @Override
    public List<Producto> findByCategoriaId(Long categoriaId) {
        return productoRepository.findByCategoriaId(categoriaId);
    }
}
