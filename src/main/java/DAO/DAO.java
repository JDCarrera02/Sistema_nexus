package DAO;

import java.sql.SQLException;
import java.util.List;

/**
 * Este objeto es una interfaz que será implementada por las clases DAO, todas las clases utilizarán metodos genericos para hacer un CRUD basico
 * Consultar, modificar, eliminar, e insertar
 * */
public interface DAO<T> {
    /**
     * Metodo para insertar un nuevo registro en la base de datos
     * @param object el objeto a insertar
     * @throws SQLException si ocurre un error en la base de datos, o con la conexion o con la insercion
     * */
    void insertar(T object) throws SQLException;

    /**
     * Metodo para actualizar un registro existente
     * @param object el objeto con los datos modificados
     * @throws SQLException si ocurre algún error con la actualizacion, o error de conexion
     * */
    void actualizar(T object) throws SQLException;

    /**
     * Metodo para eliminar un registro de la base de datos por su llave o clave primaria
     * @param id La clave primaria del registro a eliminar
     * @throws SQLException si ocurre un error en la base de datos
     * */
    void eliminar(String id)throws SQLException;

    /**
     * Metodo para buscar un registro por su clave
     * @param id la clave primaria del registro a buscar
     * @return el objeto encontrado, o null si no existe o no lo encontró
     * @throws SQLException si ocurre un error en la base de datos
     * */
    T buscarPorId(String id)throws SQLException;

    /**
     * Metodo para devolver todos los registros de la entidad o tabla
     * @return lista con los registros encontrados, o vacia si no encuentra ninguno
     * @throws SQLException si ocurre un error en la base de datos
     *
     * */
    List<T>listarTodos() throws SQLException;
}
