package Controller;

import DAO.InstalacionDAO;
import Model.Instalacion;
import Model.TipoInstalacion;
import Util.Login;
import Util.MensajeSQL;
import Util.Validator;
import View.VistaCliente;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/*
 * Clase IntalacionController
 * Esta clase gestionará las acciones que se realicen en la vista sobre alguna instalacion, de momento solamente un usuario
 * puede gestionar instalaciones, consultarlas, borrarlas, desactivarlas y crear nuevas.
 *
 * Llamará los metodos respectivos del DAO para las instalaciones (CRUD) basico, hará validaciones a las entradas tomando en cuenta
 * el modelo relacional de la base de datos, es decir si hay campos obligatorios "NOT NULL" se validará el contenido (formato) por medio de la clase Validator
 * */
public class InstalacionController {

    private final InstalacionDAO instalacionDAO;
    private final VistaCliente vista;

    // Constructor para inicializar los objetos de la clase
    public InstalacionController(VistaCliente vista) {
        this.instalacionDAO = new InstalacionDAO();
        this.vista = vista;
    }

    /**
     * Metodo para insertar o crear una instalacion, llama al metodo de la clase InstalacionDAO para insertar instalaciones
     * Valida que las entradas sean correctas antes de la insercion.
     *
     * @param nombre       el nombre de la instalacion
     * @param tipo         valor Enum que indica el tipo de instalacion
     * @param txtCapacidad la capacidad o aforo de una instalacion
     * @param txtPrecio    el precio calculado que tiene una instalacion
     * @return "true" si se logra insertar, o "false" si no se puede
     */
    public boolean insertar(String nombre, TipoInstalacion tipo, String txtCapacidad, String txtPrecio) {
        BigDecimal precio;
        Integer capacidad;
        // Validacion de entradas
        try {

            Validator.validarNombreInstalacion(nombre);

            if (tipo == null) {
                throw new IllegalArgumentException("El tipo de instalación no puede estar vacío");
            }

            // Comprobar si capacidad es numerica
            capacidad = parsearEntero(txtCapacidad, "La capacidad");
            // Validar formato
            Validator.validarCapacidad(capacidad);

            // Comprobar si precio es numerico decimal
            precio = parsearDecimal(txtPrecio,"El precio por hora");
            // Validar formato
            Validator.validarPrecio(precio, "Precio por hora");

        } catch (IllegalArgumentException e) {
            vista.mostrarError(e.getMessage());
            return false; // No se puede insertar una instalacion si hay errores con los parametros de entrada
        }

        // Construccion del objeto Instalacion a partir de los parametros de entrada validos
        try {
            Instalacion instalacion = new Instalacion(nombre, tipo, capacidad, precio);
            // Llamar al metodo del DAO para insertar el nuevo objeto
            instalacionDAO.insertar(instalacion);
            // Escribir en el log que se ha realizado correctamente la operacion
            Login.log("Instalacion registrada correctamente: "+nombre);
            // Mostrar mensaje de informacion al usuario
            vista.mostrarMensaje("Instalación registrada correctamente.");
            return true; // Instalacion añadida exitosamente

        } catch (SQLException e) {
            // Registrar error en el fichero de errores
            Login.error("Error al inserttar instalacion: "+e.getMessage());
            // Si existe algun problema con la base de datos, se captura la excepcion SQL y se muestra el error despues de traducirlo con el metodo privado
            vista.mostrarError(MensajeSQL.traducir(e));
            return false; // No se puede añadir una instalacion si hay algun error con la base de datos
        }
    }

    /**
     * Metodo para actualizar los datos de una instalacion, llama al metodo de InstalacionDAO = actualizar().
     * Valida entradas y verifica que una instalacion existe para poder actualizarla correctamente por medio del metodo buscarPorId()
     *
     * @param idInstalacion el id de la instalacion a actualizar
     * @param nombre        el nombre actualizado
     * @param tipo          el valor Enum actualizado (indica el tipo de instalacion que es)
     * @param txtCapacidad  el aforo o capacidad actualizada de la instalacion
     * @param txtPrecio    el precio hora calculado actualizado de la instalacion
     * @param activa        para indicar si una instalacion se encuentra activa o no, se actualiza tambien
     * @return "true" si se actualiza correctamente, o "false" si no.
     */
    public boolean actualizar(Integer idInstalacion, String nombre, TipoInstalacion tipo, String txtCapacidad, String txtPrecio, boolean activa) {
        BigDecimal precio;
        Integer capacidad;
        // Validacion de entradas
        try {

            if (idInstalacion == null) {
                throw new IllegalArgumentException("El id de la instalación no puede estar vacío");
            }

            Validator.validarNombreInstalacion(nombre);

            if (tipo == null) {
                throw new IllegalArgumentException("El tipo de instalación no puede estar vacío");
            }

            // Verificar si la capacidad es numerica
            capacidad = parsearEntero(txtCapacidad, "La capacidad");
            // Validar formato
            Validator.validarCapacidad(capacidad);

            // Verificar si el precio ingresado es numerico decimal
            precio = parsearDecimal(txtPrecio, "Precio por hora");
            // Validar formato
            Validator.validarPrecio(precio, "Precio por hora");

        } catch (IllegalArgumentException e) {
            // Si hay algun parametro que no cumple con los requisitos
            vista.mostrarError(e.getMessage()); // Mostrar mensaje al usuario
            return false; // Algun parametro erroneo, no se puede actualizar
        }

        // Verificar si existe la instalacion antes de actualizar
        try {
            // Construir objeto a partir del resultado retornado a traves del metodo del DAO buscarInstalacionPorId()
            Instalacion instalacion = instalacionDAO.buscarInstalacionPorId(idInstalacion);

            // Si el objeto es null (No lo ha encontrado)
            if (instalacion == null) {
                // Mostrar mensaje de error al usuario
                vista.mostrarError("No existe ninguna instalación con id: " + idInstalacion);
                return false; // No se puede actualizar una instalacion si no existe.
            }

            // Si todo esta correcto, actualizar instalacion por medio de los setter
            instalacion.setNombreInstalacion(nombre);
            instalacion.setTipoInstalacion(tipo);
            instalacion.setCapacidad(capacidad);
            instalacion.setPrecioHora(precio);
            instalacion.setActiva(activa);

            // Pasar objeto modificado al metodo del DAO, para actualizar el registro de la base de datos
            instalacionDAO.actualizar(instalacion);
            // Registrar que la accion se ha realizado correctamente en el fichero log
            Login.log("Instalacion actializada - id: "+idInstalacion);
            vista.mostrarMensaje("Instalación actualizada correctamente."); // Mostrar mensaje informativo al usuario
            return true; // Instalacion actualizada exitosamente

        } catch (SQLException e) {
            // Registrar error en el fichero de errores
            Login.error("Error al actualizar instalacion: "+e.getMessage());
            vista.mostrarError(MensajeSQL.traducir(e)); // Si hay algun error con la base de datos, capturar excepcion SQL y traducirlo para mostrarlo legible al usuario en la vista
            return false; // No se puede actualizar si hay algun error con la base de datos.
        }
    }

    /**
     * Metodo para eliminar una instalacion, llama al metodo del DAO eliminar().
     * Antes de eliminar, se comprueba si la instalacion a eliminar se encuentra en la base de datos. Luego de eso, se elimina si existe, si no, no se hace nada
     *
     * @param idInstalacion el id de la instalacion a eliminar
     * @return "true" si se logra eliminar, o "false" si no.
     */
    public boolean eliminar(Integer idInstalacion) {
        // Validacion de entrada
        try {

            if (idInstalacion == null) {
                throw new IllegalArgumentException("El id de la instalación no puede estar vacío");
            }

        } catch (IllegalArgumentException e) {
            // Si el parametro no cumple con los requisitos, se captura la excepcion y se muestra el mensaje de error al usuario en la vista
            vista.mostrarError(e.getMessage());
            return false;
        }

        // Verificacion si la instalacion existe antes de eliminar
        try {
            // Construir objeto a traves del resultado retornado por el metodo del DAO buscarInstalacionPorId()
            Instalacion instalacion = instalacionDAO.buscarInstalacionPorId(idInstalacion);

            // Si el objeto es null (No lo ha encontrado)
            if (instalacion == null) {
                vista.mostrarError("No existe ninguna instalación con id: " + idInstalacion); // Mostrar mensaje de error al usuario
                return false;
            }

            // Si todo es correcto, se elimina la instalacion a traves del metodo del DAO
            instalacionDAO.eliminarPorId(idInstalacion);
            // Registrar accion exitosa en el fichero log
            Login.log("Instalacion eliminada - id: "+idInstalacion);
            vista.mostrarMensaje("Instalación eliminada correctamente."); // Mostrar mensaje al usuario
            return true;

        } catch (SQLException e) {
            // Registrar error en el fichero de errores
            Login.error("Error al eliminar instalacion: "+e.getMessage());
            vista.mostrarError(MensajeSQL.traducir(e)); // Si hay algun error con la base de datos, se captura la excepcion SQL y se muestra el error en la vista al usuario, traducido por medio del metodo privado, para que sea legible el error
            return false;
        }
    }

    /**
     * Metodo para recuperar una instalacion por su id (internamente), llama al metodo del DAO
     *
     * @param idInstalacion El id de la instalacion a eliminar
     * @return El objeto Instalacion encontrado, o null si: el id no cumple con los requisitos de formato, o si ocurre algun error con la base de datos
     */
    public Instalacion buscarPorId(Integer idInstalacion) {
        try {
            if (idInstalacion == null) {
                throw new IllegalArgumentException("El id de la instalación no puede estar vacio");
            }
        } catch (IllegalArgumentException e) {
            vista.mostrarError(e.getMessage()); // Si el parametro no cumple con el formato o es null, se captura la excepcion y se muestra el mensaje al usuario
            return null; // Retornar null
        }

        try {
            // Construir objeto Instalacion a partir del resultado obtenido por el metodo del DAO
            Instalacion instalacion = instalacionDAO.buscarInstalacionPorId(idInstalacion);

            // Si el objeto es null (No hay resultados)
            if (instalacion == null) {
                vista.mostrarError("No existe ninguna instalación con id: " + idInstalacion); // Mostrar mensaje de error al usuario, informandole que la instalacion no se encuentra
            }

            return instalacion; // Si no es null, se retorna el objeto con la informacion respectiva

        } catch (SQLException e) {
            // Registrar error en el fichero de errores
            Login.error("Error al buscar instalacion: "+e.getMessage());
            vista.mostrarError(MensajeSQL.traducir(e)); // Si ocurre algun error con la base de datos, se captura la excepcion SQL y se pasa el mensaje traducido por medio del metodo privado, para que sea legible por el usuario
            return null; // Retornar null
        }
    }

    /**
     * Metodo para listar todas las instalaciones de la base de datos (Registradas), llama al metodo del DAO
     *
     * @return La lista con los resultados o null si hay algun error con la base de datos
     */
    public List<Instalacion> listarTodos() {
        try {
            return instalacionDAO.listarTodos();
        } catch (SQLException e) {
            // Registrar error en el fichero de errores
            Login.error("Error al listar instalaciones: "+e.getMessage());
            vista.mostrarError(MensajeSQL.traducir(e));
            return null;
        }
    }

    /**
     * Metodo controlador para listar instalaciones por tipo (Enum), que llama al metodo del DAO
     *
     * @param tipo el parametro Enum utilizado como criterio para mostrar instalaciones
     * @return La lista con las instalaciones, o null si hay algun error con la base de datos o si el Enum recibido no es valido
     */

    public List<Instalacion> listarPorTipo(TipoInstalacion tipo) {
        try {
            // Comprobar entrada
            if (tipo == null) {
                throw new IllegalArgumentException("El tipo de instalacion no puede estar vacio");
            }
        } catch (IllegalArgumentException e) {
            vista.mostrarError(e.getMessage());
            return null;
        }

        try {
            // Llamar al metodo y retornar una lista
            return instalacionDAO.listarPorTipo(tipo);
        } catch (SQLException e) {
            // Registrar error en el fichero de errores
            Login.error("Error al listar instalaciones por tipo: "+e.getMessage());
            vista.mostrarError(MensajeSQL.traducir(e));
            return null;
        }
    }

    /**
     * Metodo controlador que llama al metodo del dao, buscarPorNombre(), que retorna una lista de instalaciones filtradas por el nombre
     *
     * @param termino el criterio de busqueda
     * @return La lista con los resultados encontrados, o null si no encuentra nada o si hay algun error con la base de datos
     */
    public List<Instalacion> buscarPornombre(String termino) {
        if (termino == null || termino.isBlank()) {
            return listarTodos();
        }

        try {
            List<Instalacion> instalaciones = instalacionDAO.buscarPorNombre(termino);

            if (instalaciones.isEmpty()) {
                vista.mostrarMensaje("No se encontraron instalaciones con ese termino de busqueda: " + termino);
            }

            return instalaciones;
        } catch (SQLException e) {
            Login.error("Error al buscar instalaciones " + e.getMessage());
            vista.mostrarError(MensajeSQL.traducir(e));
            return null;
        }
    }

    // Metodos privados para convertir tipos de datos desde la vista

    /**
     * Convierte un String a Integer.
     * Lanza IllegalArgumentException con mensaje claro si el formato es invalido
     *
     * @param texto       el texto a convertir
     * @param nombreCampo el nombre del campo para el mensaje de error
     * @return el Integer resultante
     */
    private Integer parsearEntero(String texto, String nombreCampo) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(nombreCampo + " | no puede estar vacio");
        }

        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(nombreCampo + " | debe ser un numero entero. Ejemplo: 10");
        }
    }

    /**
     * Metodo que convierte un String a BigDecimal.
     * Lanza IllegalArgumentException con mensaje claro si el formato es inválido.
     *
     * @param texto       el texto a convertir
     * @param nombreCampo el nombre del campo para el mensaje de error
     * @return el BigDecimal resultante
     */
    private BigDecimal parsearDecimal(String texto, String nombreCampo) {
        if (texto == null || texto.isBlank())
            throw new IllegalArgumentException(
                    nombreCampo + " no puede estar vacío"
            );
        try {
            return new BigDecimal(texto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    nombreCampo + " debe ser un número válido. Ejemplo: 12.50"
            );
        }
    }
}