package org.dam.fcojavier.ecometrica.dao;

import org.dam.fcojavier.ecometrica.entities.Categoria;

/**
 * DAO para gestionar las categorias registrables.
 */
public class CategoriaDAO extends GenericDAO<Categoria>{

   public CategoriaDAO() {
        super(Categoria.class);
    }
}
