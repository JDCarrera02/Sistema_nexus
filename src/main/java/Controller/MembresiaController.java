package Controller;

import DAO.MembresiaDAO;
import Model.Membresia;
import Model.TipoMembresia;
import Util.Login;
import Util.Validator;
import View.VistaCliente;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Clase MembresiaController, llamara los metodos de la clase DAO MembresiaDAO, para hacer el CRUD basico de una membresia.
 * Esta clase utiliza dos objetos "MembresiaDAO" para utilizar los metodos respectivos de manipulacion a la base de datos; y "VistaCliente"
 * para llamar a sus metodos de mostrar dialogos al usuario.
 */
public class MembresiaController {
    private final MembresiaDAO membresiaDAO;
    private final VistaCliente vista;

    // Constructor que inicializa los objetos de la clase
    public MembresiaController(VistaCliente vista) {
        this.membresiaDAO = new MembresiaDAO();
        this.vista = vista;
    }

    /*
     * Nota aclaratoria, un administrador solamente puede modificar las membresias existentes, para respetar el modelo de negocio,
     * Cualquier cambio, se tendrá que realizar manualmente por el equipo de desarrollo
     *
     * En cada metodo se utilizaran los metodos de la vistaCliente, para mostrar mensajes de informacion o mensajes de error según corresponda,
     * y el uso de la clase Login para guardar errores o para guardar informacion durante los procesos que realice el usuario.
     * */

    /**
     * Metodo para actualizar una membresia existente, modificar detalles
     *
     * @param idMembresia    el id de la membresia a actualizar
     * @param nombre         el tipo de membresia, al tratarse de un Enum, se condiciona que solamente puede tener ciertos valores definidos
     * @param precioTxt      el precio de la membresia, capturada en la vista a traves de los JTextField
     * @param maxReservasTxt la cantidad o el maximo de reservas que tiene un socio con determinada membresia, valor capturado en la vista a traves del JTextField
     * @param descripcion    la descripcion y otros detalles de una membresia
     */
    public boolean actualizar(Integer idMembresia, TipoMembresia nombre, String precioTxt, String maxReservasTxt, String descripcion) {
        BigDecimal precio;
        int maxReservas;

        // Validacion de entradas
        try {
            if (idMembresia == null) {
                throw new IllegalArgumentException("El id de la membresia no puede estar vacio");
            }

            if (nombre == null) {
                throw new IllegalArgumentException("El tipo de membresia no puede estar vacia");
            }

            // Conversion del parametro precio
            try {
                precio = new BigDecimal(precioTxt);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("El precio debe ser un numero valido. Ejemplo: 29.99");
            }

            Validator.validarPrecio(precio, "Precio mensual");

            // Conversion del parametro maxReservas
            try {
                maxReservas = Integer.parseInt(maxReservasTxt);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("El maximo de reservas debe ser un numero entero ");
            }

            Validator.validarMaxReservas(maxReservas);
            Validator.validarDescripcion(descripcion);

        } catch (IllegalArgumentException e) {
            vista.mostrarError(e.getMessage());
            return false; // No se puede modificar una membresia si hay errores en los parametros de entrada
        }

        // Recuperar membresia existente para actualizar
        try {
            Membresia membresia = membresiaDAO.busquedaMembresiaPorId(idMembresia);

            if (membresia == null) {
                vista.mostrarError("No existe una membresia con id: " + idMembresia);
                return false; // No se puede actualizar una membresia que no existe.
            }

            // Actualizar membresia a traves de los setter
            membresia.setPrecioMensual(precio);
            membresia.setMaxreservas(maxReservas);
            membresia.setDescripcion(descripcion.isBlank() ? null : descripcion);

            membresiaDAO.actualizar(membresia);
            Login.log("Membresia actualizada -- " + nombre.getNombreMembresia());
            vista.mostrarMensaje("Membresia actualizada correctamente. ");
            return true; // Membresia modificada exitosamente
        } catch (SQLException e) {
            Login.error("Error al actualizar membresia: " + e.getMessage());
            vista.mostrarError(gestionarErrorSQL(e));
            return false; // No se puede actualizar si hay algun error con la base de datos
        }
    }

    /**
     * Metodo que llama al buscarPorId del controlador de membresia
     *
     * @param idMembresia el id de la membresia a buscar
     * @return el objeto membresia si lo encuentra, o null si no lo encuentra
     */
    public Membresia buscarPorId(Integer idMembresia) {
        try {
            if (idMembresia == null) {
                throw new IllegalArgumentException("El id de la membresia no puede estar vacio");
            }
        } catch (IllegalArgumentException e) {
            vista.mostrarError(e.getMessage());
            return null;
        }

        try {
            Membresia membresia = membresiaDAO.busquedaMembresiaPorId(idMembresia);
            if (membresia == null) {
                vista.mostrarError("No existe ninguna membresia con id: " + idMembresia);
            }
            return membresia; // Si la encuentra, se retorna el objeto membresia
        } catch (SQLException e) {
            Login.error("Error al buscar membresia: " + e.getMessage());
            vista.mostrarError(gestionarErrorSQL(e));
            return null;
        }
    }

    /**
     * Metodo que llama a la funcion del controlador de membresia, consultarPorTipo()
     *
     * @param tipo el nombre de la membresia, de tipo Enum
     * @return El objeto membresia encontrado por tipo, o null si no lo encuentra o hay alguna excepcion
     */
    public Membresia buscarPorTipo(TipoMembresia tipo) {
        try {
            if (tipo == null) {
                throw new IllegalArgumentException("El tipo de membresia no puede estar vacio");
            }
        } catch (IllegalArgumentException e) {
            vista.mostrarError(e.getMessage());
            return null;
        }

        try {
            Membresia membresia = membresiaDAO.consultarPorTipo(tipo);
            if (membresia == null) {
                vista.mostrarError("No existe ninguna membresia de tipo: " + tipo.getNombreMembresia());
            }
            return membresia;
        } catch (SQLException e) {
            Login.error("Error al buscar membresia por tipo: " + e.getMessage());
            vista.mostrarError(gestionarErrorSQL(e));
            return null;
        }
    }

    /**
     * Metodo que llama a la funcion del controlador membresia, listarTodos, que retorna una lista de membresias
     *
     * @return La lista de membresias registradas en la base de datos, o null si hay algun error.
     * Se mostrará mensaje de error cuando exista algun problema con la base de datos o el DAO.
     */
    public List<Membresia> listarTodos() {
        try {
            return membresiaDAO.listarTodos();
        } catch (SQLException e) {
            Login.error("Error al listar membresias: " + e.getMessage());
            vista.mostrarError(gestionarErrorSQL(e));
            return null;
        }
    }

    /**
     * Metodo privado para gestionar errores SQL y mostrar mensajes legibles en la vista para el usuario
     *
     * @param e la excepcion capturada de tipo SQLException
     * @return el mensaje traducido para el usuario
     */
    private String gestionarErrorSQL(SQLException e) {
        return switch (e.getErrorCode()) {
            case 1451 -> "No se puede eliminar la membresía porque tiene socios asignados.";
            case 1452 -> "Error de integridad: algún dato referenciado no existe.";
            default -> "Error en la base de datos: " + e.getMessage();
        };
    }


}
